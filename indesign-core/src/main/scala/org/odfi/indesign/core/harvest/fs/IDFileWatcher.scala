package org.odfi.indesign.core.harvest.fs

import java.nio.file._

import com.idyria.osi.tea.files.FileWatcherAdvanced
import com.idyria.osi.tea.logging.TLogSource
import com.idyria.osi.tea.thread.ThreadLanguage
import java.io.File
import java.lang.ref.WeakReference

import org.odfi.indesign.core.heart.HeartTask

import scala.collection.convert.DecorateAsScala
import com.sun.xml.internal.bind.v2.runtime.reflect.Lister
import org.odfi.indesign.core.harvest.HarvestedResource
import org.odfi.indesign.core.heart.Heart

trait IDFileEvent {

  val f: File
  val k: IDWatchKey
}

class IDAddedEvent(val f: File,val k:IDWatchKey) extends IDFileEvent {

}

class IDDeletedEvent(val f: File,val k:IDWatchKey) extends IDFileEvent {

}

class IDModifiedEvent(val f: File,val k:IDWatchKey) extends IDFileEvent {

}

object IDAdded {

  def unapply(arg: IDFileEvent): Option[File] = {
    arg match {
      case added: IDAddedEvent => Some(added.f)
      case other => None
    }
  }

}

object IDModified {
  def unapply(arg: IDFileEvent): Option[File] = {
    arg match {
      case modified: IDModifiedEvent => Some(modified.f)
      case other => None
    }
  }
}

object IDDeleted {
  def unapply(arg: IDFileEvent): Option[File] = {
    arg match {
      case deleted: IDDeletedEvent => Some(deleted.f)
      case other => None
    }
  }
}

// Interface Sub classes
//------------------
class IDWatcher(val fileAccept: String, val listener: WeakReference[Any], var closure: Function1[IDFileEvent, Unit]) extends HarvestedResource {

  var watchKey: Option[IDWatchKey] = None

  def getId = s"IDFileWatcher:${hashCode()}:${listener.hashCode()}"

  /*  def doTask = {
      closure(ev)
    }*/

  override def clean = {
    super.clean
    listener.clear()

    closure = {
      ev =>
    }
  }

  /**
    * Clean if the listener is not available anymore
    */
  def check = {
    listener.get match {
      case null =>
        // Clean
        watchKey.get.removeWatcher(this)
        false
      case other =>
        true
    }
  }

  def dispatch(ev: IDFileEvent) = {

    var taskId = s"IDFileWatcher:${hashCode()}:${listener.hashCode()}:${ev.getClass.getName}:${ev.f}"
    Heart.running(taskId) match {
      case Some(task) =>
        logFine[IDFileWatcher](s"[W] Task ${taskId} is running")
      case None =>
        logFine[IDFileWatcher](s"[V] Task ${taskId} starting")
        val task = new HeartTask[Any] {
          def getId = taskId

          def doTask = {
            closure(ev)
          }
        }
        Heart.pump(task)
    }


    //closure(ev)
  }

  def accept(path: String) = fileAccept match {
    case "*" => true
    case fileAccept => path == fileAccept
  }

}

class IDWatchKey(val directory: File, val key: WatchKey) extends HarvestedResource {

  def getId = s"WatchKey:${hashCode()}:directory:$key"

  override def clean = {
    super.clean
    key.cancel()
    cleanupWatchers
  }

  //-- Watchers
  var watchers = List[IDWatcher]()

  def addWatcher(w: IDWatcher) = {
    watchers.contains(w) match {
      case false =>
        watchers = watchers :+ w
        w.watchKey = Some(this)
      case true =>
    }
  }

  def removeWatcher(w: IDWatcher) = watchers.contains(w) match {
    case false =>

    case true =>
      w.watchKey = None
      watchers = watchers.filter(_ != w)
  }

  def cleanupWatchers = {

    watchers.foreach {
      w =>
        w.clean
        removeWatcher(w)
    }
  }

  /**
    * Returns file instance resolved from this event
    * Don't return canonical path because file may have been deleted
    */
  def resolveFile(e: WatchEvent[Path]) = {
    directory.toPath().resolve(e.context()).toFile()
  }

  def isWatching(f: File) = directory.getCanonicalPath == f.getCanonicalPath

  def isKey(k: WatchKey) = key.equals(k)

}

class IDFileWatcher extends ThreadLanguage with TLogSource with DecorateAsScala {



  // Lifecycle
  //-----------------
  def start = {
    watcherThread.start
  }

  def stop = {
    clean
    watcher.close()
    ///watcherThread.join
  }

  /**
    * Deregister everyting
    */
  def clean = {
    this.watchedDirectories.foreach {
      idKey => idKey.clean
    }
    this.watchedDirectories = this.watchedDirectories.filter(k => false)
  }

  // Watching Low Level
  //------------
  val watcher = FileSystems.getDefault().newWatchService();

  /**
    * Watching Keys are only defined for folders
    * Watching a file is thus required to watch the folder above
    */
  var watchedDirectories = List[IDWatchKey]()


  def deleteWatchingKey(k:IDWatchKey) = watchedDirectories.contains(k) match {
    case true =>
      watchedDirectories = watchedDirectories.filter(_!=k)
      k.clean
    case false =>
  }

  /**
    * Register provided directory to watch for events inside
    * If the file is not a directory, then use parent
    */
  def registerDirectoryForWatching(directory: File) = {

    var targetFile = directory.exists() match {
      case true if (directory.isDirectory) => directory.getCanonicalFile
      case true if (!directory.isDirectory) => directory.getParentFile.getCanonicalFile
      case false =>
        sys.error("Cannot Watch non existing file/directory..." + directory.getCanonicalPath)
    }

    watchedDirectories.find(_.isWatching(targetFile)) match {

      //-- FOund -> Return
      case Some(key) => key

      //-- Not Found -> add
      case None =>

        logFine[IDFileWatcher](s"Start watching: " + targetFile)
        //-- Register
        var path = targetFile.toPath()
        var key = path.register(watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY)

        //-- Create IDkey
        var IDKey = new IDWatchKey(targetFile, key)
        watchedDirectories = watchedDirectories :+ IDKey

        IDKey
    }

  }

  // Single File Watch
  //--------------------

  /**
    *
    *
    * @param listener
    * @param file
    * @param closure
    */
  def onFileChange(listener: Any, file: File)(closure: IDFileEvent => Unit) = {

    //-- Register for watching
    var registeredKey = registerDirectoryForWatching(file)

    //-- Create a new Watcher
    var watcher = new IDWatcher(file.getName, new WeakReference(listener), closure)
    registeredKey.addWatcher(watcher)

    watcher
  }


  // Single Directory Watch
  //-------------------

  /**
    *
    * @param listener
    * @param file
    * @param closure
    */
  def onDirectoryChange(listener: Any, file: File, modifyOnParent: Boolean = false)(closure: IDFileEvent => Unit) = {

    //-- Register for watching
    var registeredKey = registerDirectoryForWatching(file)

    //-- Create a new Watcher for new sub files
    var watcher = new IDWatcher("*", new WeakReference(listener), closure)
    registeredKey.addWatcher(watcher)

    //-- Watch parent to be able to catch deleting of the folder
    if (modifyOnParent) {
      var parentKey = registerDirectoryForWatching(file.getParentFile)
      var parentWatcher = new IDWatcher(file.getName, new WeakReference(listener), closure)
      parentWatcher.deriveFrom(watcher)
      parentKey.addWatcher(parentWatcher)
    }


    watcher

  }

  // Recursive Watch
  //------------------------
  def onRecursiveDirectoryChange(listener: Any, directory: File)(closure: IDFileEvent => Unit) = directory match {


    case dir if (dir.exists() && dir.isDirectory) =>

      //-- Watch base directory
      val topWatcher = this.onDirectoryChange(listener, directory)(closure)

      // var idKey = this.registerDirectoryForWatching(directory)

      //-- Prepare the closure to be used on all listeners, to make sure we add new directories to the watching
      def listeningClosure: (IDFileEvent => Unit) = {

        //-- If directory is removed, make sure no watcher for key are present
        case deleted if (IDDeleted.unapply(deleted).isDefined) =>

          //logFine[IDFileWatcher]("Seen delete")

          //-- Delete derived watch key which has a watcher with same name
          this.watchedDirectories.find {
            k =>
              //logFine[IDFileWatcher]("Delete testing k: "+k.directory.getCanonicalPath)
              //logFine[IDFileWatcher]("Deleted "+deleted.f.getCanonicalPath)
              //k.watchers.find { w => w.accept(deleted.f.getName)}.isDefined
              k.directory.getCanonicalPath == deleted.f.getCanonicalPath
          } match {
            case Some(key) =>
              //logFine[IDFileWatcher]("Found key to remove: "+key)
              deleteWatchingKey(key)
            case None =>

          }

          //-- Dispatch
          closure(deleted)

        //-- If a directory is added, add to watch list
        case added if (IDAdded.unapply(added).isDefined && added.f.isDirectory) =>

          logFine[IDFileWatcher]("Created Directory, so add it to watch")
          var newWatcher = onDirectoryChange(listener, added.f)(listeningClosure)
          newWatcher.deriveFrom(topWatcher)
          //-- Make new watcher and key derived from top watcher key
          //newWatcher.watchKey.get.deriveFrom(topWatcher.watchKey.get)

          //topWatcher.dispatch(new IDAddedEvent(newDirectory))
          closure(added)

        //-- Ignore events on directories, already handled
        case other if (other.f.isDirectory) =>

          logFine[IDFileWatcher]("Ignoring directory modify..."+other.f)

        case other =>
          closure(other)
        //topWatcher.dispatch(other)
      }

      topWatcher.closure = listeningClosure

      //-- Now walk the files and register watchers for everything
      var filesStream = Files.walk(directory.toPath)
      filesStream.forEach {
        file =>

          //-- Only listen to directories
          file.toFile.isDirectory match {
            case true if(file.toFile.getCanonicalPath!=directory.getCanonicalPath) =>

              /**
                * When a change happens on a directory, call the same closure
                * Link with top watcher to that top watcher cleans everything
                * When new resources are added, listen to them too
                */
              var dirWatcher = onDirectoryChange(listener,file.toFile)(listeningClosure)
              dirWatcher.deriveFrom(topWatcher)

            case other =>
          }
      }

      topWatcher

    case other =>
      sys.error(s"Cannot Watch sub directory changes if the provided file ${directory.getCanonicalPath} is not a directory")


  }

  // Watcher Thread
  //--------------------
  val watcherThread = createDaemonThread {

    var stop = false
    while (!stop) {

      // Get Key on which and event has happened
      //----------
      try {
        var key = watcher.take()

        // Get IDKey
        //-------------
        this.watchedDirectories.find(_.isKey(key)) match {

          //-- Process
          case Some(idKey) =>

            // Try and always reset the key
            try {

              //-- get events
              key.pollEvents().asScala.filter { ev => ev.kind() != StandardWatchEventKinds.OVERFLOW } foreach {

                case be: WatchEvent[Path] if (be.count() <= 1) =>

                  logFine[IDFileWatcher](s"Change detected on key ${idKey}: " + be.kind() + " -> " + be.context())

                  //-- Create event
                  var event = be.kind() match {
                    case StandardWatchEventKinds.ENTRY_DELETE =>
                      new IDDeletedEvent(idKey.resolveFile(be),idKey)
                    case StandardWatchEventKinds.ENTRY_CREATE =>
                      new IDAddedEvent(idKey.resolveFile(be),idKey)
                    case StandardWatchEventKinds.ENTRY_MODIFY =>
                      new IDModifiedEvent(idKey.resolveFile(be),idKey)
                  }

                  // Dispatch
                  //---------------
                  idKey.watchers.filter { w => w.accept(be.context().toString) } foreach {
                    watcher =>
                      watcher.check match {
                        case true =>
                          logFine[IDFileWatcher]("Watcher acccepted request: " + watcher.fileAccept)
                          watcher.dispatch(event)
                        case false =>

                      }


                  }

                case other =>
              }

            } finally {
              key.reset()
            }

          //-- Deregister
          case None =>
            key.cancel()
        }
      } catch {
        case e: ClosedWatchServiceException =>
          stop = true
      }

    }
  }

}