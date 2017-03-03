package org.odfi.indesign.core.harvest.fs

import java.io.File
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes

import scala.language.implicitConversions

import org.odfi.indesign.core.harvest.HarvestedResource
import org.odfi.indesign.core.harvest.Harvester

trait FileSystemIgnoreProvider extends HarvestedResource {

  def fileIgnore(r: File): Boolean

}

trait FileSystemHarvester extends Harvester {

  //var searchPaths = List[Path]()

  def addPath(f: File): Boolean = addPath(f.toPath())
  def addPath(p: Path): Boolean = {
    var f = new HarvestedFile(p)
    f.root
    f.local = true
    gatherDirect(f)
    true
    //this.searchPaths = this.searchPaths :+ p.toFile.getAbsoluteFile.toPath()
  }

  /*this.onConfigUpdated {
    this.config match {
      case Some(conf) =>
        conf.values.keys.foreach {
          case key if (key.keyType === "file") =>
            key.values.foreach {
              v =>
                 println("Found File: "+v)
                this.addPath(new File(v).getCanonicalFile.toPath)

            }

          case key =>
          // println("Key in config: "+key.keyType)
        }
      case None =>
    }
  }*/

  //def createResourceFromPath(p:Path) : RT

  /*this.onDeliver {
    case r: HarvestedFile =>
      (r.path.toFile().exists(), r.path.toFile().isDirectory()) match {
        case (true, true) =>
          gather(r)
          true

        case _ => false

      }

  }*/

  override def doHarvest = {

    // Use Config to find base paths
    this.config match {
      case Some(conf) =>
        conf.values.keys.foreach {
          case key if (key.keyType === "file") =>
            key.values.foreach {
              v =>
                logFine[FileSystemHarvester]("***** FS Found File: " + v)
                this.addPath(new File(v).getCanonicalFile.toPath)

            }

          case key =>
          // println("Key in config: "+key.keyType)
        }
      case None =>
    }

    logFine[FileSystemHarvester](s"Harvesting on : ${this} -> ${this.childHarvesters.size}")

    /**
     * For each child harvester, a list of paths to stop at during walk
     */
    var stopList = scala.collection.mutable.Map[Harvester, scala.collection.mutable.ListBuffer[Path]]()
    this.childHarvesters.foreach {
      ch =>

        stopList.update(ch, new scala.collection.mutable.ListBuffer[Path])
      // stopList += (ch.asInstanceOf[Harvester[_, _]] -> new scala.collection.mutable.ListBuffer[Path])
    }

    this.onResources[HarvestedFile] {

      case resource =>

        logFine[FileSystemHarvester](s"---- Starting on: ${resource}")

        // Readd Resource to Gathered to make sure it won't dissapear
        // If rooted, don't readd
        resource.rooted match {
          case true =>
          case false =>
            gather(resource)
        }

        // Walk Through the files, stop if a child harvester gathered a directory, and set current resource as parent to all created resources

        var basePath = resource.path

        //println(s"-- Starting harvest on: $resource")

        // Delevier files to child harvester
        //stream.forEach(FileSystemHarvester.childHarvesters.foreach(h => h.deliver(_)))
        //stream.forEach(p => FileSystemHarvester.childHarvesters.foreach(h => h.deliver(p))) 
        //stream.forEach { p => }

        // This is just the closure applyed to all paths, 
        // set as a def otherwise the scala compiler doesn'T work with Java8 Closure style
        def doF(inputPath: Path) = {
          var r = new HarvestedFile(inputPath)
          r.deriveFrom(resource)
          //r.parentResource = Some(resource)

          // Retain only child harvesters for which the current path has no parent in stop list
          var validChildren = this.childHarvesters.filter {
            ch => stopList(ch).find(stopPath => inputPath.startsWith(stopPath)) == None
          }

          // Deliver, if gathered with true, and it is a folder, then add path to stop path for child
          validChildren.foreach {
            ch =>
              ch.deliver(r) match {
                // Add to stop list
                case true if (inputPath.toFile().isDirectory()) =>
                  //println(s"Gathered Directory")
                  stopList.keys.foreach {
                    ch => stopList(ch) += inputPath
                  }

                case _ =>
              }

          }
        }

        var visitor = new SimpleFileVisitor[Path] {

          override def visitFile(f: Path, attr: BasicFileAttributes) = {

            resource match {
              
              // Don't process base path (not a good idea to do that)
              //case sameAsResource if (resource.sameAs(sameAsResource)) => 
              //  FileVisitResult.CONTINUE
              case filterProvider: FileSystemIgnoreProvider =>

                attr.isDirectory() match {
                  case true if (filterProvider.fileIgnore(f.toFile)) =>
                    FileVisitResult.SKIP_SUBTREE
                  case false if (filterProvider.fileIgnore(f.toFile)) => 
                    FileVisitResult.CONTINUE
                  case other =>
                    doF(f)
                    FileVisitResult.CONTINUE
                }

              case other =>
                doF(f)
                FileVisitResult.CONTINUE
            }

          }
        }

        Files.walkFileTree(basePath, visitor)

        //-- Deliver Base Path as doF
        /*doF(basePath)

        //-- Walk Base Path
        var stream = Files.walk(basePath)
        stream.forEach {
          inputPath =>
            logFine[FileSystemHarvester](s"---- Processing: $inputPath")
            // println(s"---- Processing: $inputPath")
            doF(inputPath)

        }*/
        
        
      /*basePath.toFile().listFiles().foreach {
          f =>
            var stream = Files.walk(f.toPath())
            stream.forEach {
              inputPath =>
                logFine[FileSystemHarvester](s"---- Processing: $inputPath")
                // println(s"---- Processing: $inputPath")
                doF(inputPath)

            }
        }*/

    }
    // Loop resources

  }

  def processFile(inputPath: Path): Unit = {

  }
}

object FileSystemHarvester extends FileSystemHarvester {

  implicit def pathToResource(p: Path) = new HarvestedFile(p)
}