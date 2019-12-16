package edu.kit.ipe.adl.indesign.core.harvest.fs

import org.scalatest.FunSuite
import org.scalatest.BeforeAndAfterEach
import org.odfi.tea.file.DirectoryUtilities
import java.io.File
import java.util.concurrent.{Semaphore, TimeUnit}

import org.odfi.tea.io.TeaIOUtils
import org.odfi.tea.logging.{TLog, TeaLogging}
import org.odfi.indesign.core.harvest.fs.{IDAdded, IDDeleted, IDFileWatcher, IDModified}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.Suite


class IDFileWatcherTest extends FunSuite with BeforeAndAfterEach with BeforeAndAfterAll {

  val baseFolder = new File("target/test-data/idfilewatcher")
  baseFolder.mkdirs

  val IDWatcher = new IDFileWatcher


  // Utils
  //--------------------
  override def beforeAll = {
    println(s"Starting")
    TLog.setLevel(classOf[IDFileWatcher], TLog.Level.FULL)
    IDWatcher.start
  }

  override def afterAll = {
    IDWatcher.stop

  }

  override def beforeEach = {
    IDWatcher.clean
    progressSemaphore.drainPermits()
    progressTotal = 0
    DirectoryUtilities.deleteDirectoryContent(baseFolder)
  }

  def cleanupAndCheck = {
    IDWatcher.clean
    assertResult(0)(IDWatcher.watchedDirectories.size)
  }

  // Waiting utils
  //----------

  var progressSemaphore = new Semaphore(0)
  var progressTotal = 0

  def signalProgress = {
    progressSemaphore.synchronized {

      progressSemaphore.release()
      progressTotal += 1
      println("Released: " + progressTotal)
    }

  }

  def waitForProgress = {
    progressSemaphore.tryAcquire(10, TimeUnit.SECONDS) match {
      case true =>

      case false =>
    }
     Thread.sleep(200)
  }

  // tests
  //---------------


  test("Folder - Add File in folder") {

    //println("Starting ")

    IDWatcher.onDirectoryChange(this, baseFolder) {
      case IDAdded(_) =>
        //println("Change detected")
        signalProgress
      case other =>
    }

    //-- Write file
    TeaIOUtils.writeToFile(new File(baseFolder, "text.txt"), "test")
    waitForProgress

    //-- Check
    assertResult(1)(progressTotal)

    //Thread.sleep(1000)
    cleanupAndCheck

  }

  test("Folder - Add File and Remove File") {

    IDWatcher.onDirectoryChange(this, baseFolder) {
      case IDAdded(_) =>
        //println("Change detected")
        signalProgress
      case IDDeleted(_) =>
        signalProgress
      case other =>
    }

    //-- Write file
    var written = TeaIOUtils.writeToFile(new File(baseFolder, "text.txt"), "test")
    waitForProgress

    //-- Delete File
    written.delete()
    waitForProgress
    assertResult(2)(progressTotal)
    cleanupAndCheck
  }


  test("Folder - Folder Remove") {

    //-- Create Folder
    var folder = new File(baseFolder, "subfolder")
    folder.mkdirs()

    //-- Watch
    IDWatcher.onDirectoryChange(this, folder) {
      case IDDeleted(_) =>
        signalProgress
      case other =>
        println(s"Got other")
    }

    //-- Remove
    DirectoryUtilities.deleteDirectory(folder)
    waitForProgress

    //-- One event on subfolder parent for remove
    assertResult(1)(progressTotal)

    //-- Check watcher
    cleanupAndCheck

  }


  test("File - File Change") {

    //-- Write file
    var written = TeaIOUtils.writeToFile(new File(baseFolder, "text.txt"), "test")

    //-- Watch
    IDWatcher.onFileChange(this, written) {
      case IDModified(_) =>
        signalProgress
      case other =>
        println(s"Got other")
    }

    //-- Modfify and watch progress
    println(s"Writing...")
    TeaIOUtils.writeToFile(written, "test" + System.currentTimeMillis())
    waitForProgress
    Thread.sleep(1000)
    assertResult(1)(progressTotal)

  }


  def touchFile(str: String) = {
     println("Touching file: "+str)
    var file = new File(baseFolder, str.replace("/", File.separator)).getCanonicalFile
    file.getParentFile.exists() match {
      case true =>

      case false =>
        //println("test file:")
        file.getParentFile.mkdirs
    }

    TeaIOUtils.writeToFile(file, "test" + System.nanoTime().toString)


  }

  def mkFolder(str: String) = {

    var file = new File(baseFolder, str.replace("/", File.separator))
    file.mkdirs()


  }


  def rmFolder(str: String) = {

    var file = new File(baseFolder, str.replace("/", File.separator))
    DirectoryUtilities.deleteDirectory(file)

  }


  test("Folder Recursive - Create Single Folder and Remove") {


    //-- Watch
    val topWatcher = IDWatcher.onRecursiveDirectoryChange(this, baseFolder) {

      case other =>
        signalProgress
    }

    //-- Create Folder: 1 event -> create
    var folder = new File(baseFolder, "subfolder")
    folder.mkdirs()
    waitForProgress

    //-- Remove: // 1 event : delete and modify
    DirectoryUtilities.deleteDirectory(folder)
    waitForProgress

    //-- One event on subfolder parent for remove
    assertResult(2)(progressTotal)

    //-- Check subfolder watching key was removed
    assertResult(1)(IDWatcher.watchedDirectories.size)

    //-- Check watcher
    cleanupAndCheck

  }


  test("Folder Recursive - Single Folder exists, add file to it and Remove") {

    //-- Pre Create Folder
    var folder = new File(baseFolder, "subfolder")
    folder.mkdirs()

    //-- Watch
    val topWatcher = IDWatcher.onRecursiveDirectoryChange(this, baseFolder) {

      case other =>
        println("Signal got for: "+other)
        signalProgress
    }

    //-- Add file: 2 events: create/modify for file
    touchFile("subfolder/test1")
    waitForProgress
    waitForProgress
    
    //-- Touch file: 1 event: modify for file
    touchFile("subfolder/test1")
    waitForProgress

    //Thread.sleep(2000)

    //-- Remove: // total 4 => delete and modify on file is 2, , and delete on parent folder is 1
     rmFolder("subfolder")
     waitForProgress
     waitForProgress
     waitForProgress
    waitForProgress


     //-- One event on subfolder parent for remove
     assertResult(7)(progressTotal)

    //-- Check subfolder watching key was removed
    assertResult(1)(IDWatcher.watchedDirectories.size)


    //-- Check watcher
    cleanupAndCheck

  }




  /*test("Folder - Watch recursive changes") {

    //-- Create Folder
    var folder = new File(baseFolder, "subfolder")
    folder.mkdirs()

    //-- Watch Recursive
    IDWatcher.onRecursiveDirectoryChange(this, folder) {

      case other =>

        signalProgress
        println("Signal progress: " + progressTotal)
    }

    // Process
    //-------------------

    //-- Add File: 3 Events
    touchFile("subfolder/test1")
    waitForProgress


    //-- Add Sub folder A 2 Events
    mkFolder("subfolder/A")
    waitForProgress

    //mkFolder("subfolder/B")
    //waitForProgress

    //------- Add Sub Sub File 3 Events
    touchFile("subfolder/A/test1")
    waitForProgress

    //------- Add Sub Sub File 3 Events
    touchFile("subfolder/A/test2")
    waitForProgress

    //-- Add File 3 Events
    touchFile("subfolder/test")
    waitForProgress

    //-- Add Sub Folder B 2 Events
    mkFolder("subfolder/B")
    waitForProgress

    //-- Delete A, will remove two files and folder (2 * 3 events for file remove + 2 for folder remove) = 8
    Thread.sleep(3000)
    println("-------- Folder delete ------------")
    rmFolder("subfolder/A")
    waitForProgress
    waitForProgress
    waitForProgress

    //-- Recreate A
    mkFolder("subfolder/A") // 2 Events
    waitForProgress
    touchFile("subfolder/A/test1") //3 Events
    waitForProgress

    //-- Result (count number of waitForProgress)
    assertResult(50)(progressTotal)
    cleanupAndCheck


  }
*/
}