package org.odfi.indesign.core.module.jfx

import javafx.application.Platform
import com.sun.javafx.tk.Toolkit
import javafx.application.Application
import javafx.stage.Stage
import java.util.concurrent.Semaphore

class JFXRun extends Application {

  var cl: () => Unit = { () => }

  def start(stage: Stage) = {
    //Platform.setImplicitExit(false)
    JFXRun.application = this
    stage.close()
    // cl()

    // Select VUI implementation for JFX thread
    //---------------
    // VUIFactory.setImplementationForCurrentThread(new JFXFinalFactory)

    JFXRun.semaphore.release

    //stage.show()
  }

}

object JFXRun {

  var application: Application = null
  var semaphore = new Semaphore(0)
  var started = false
  var applicationThread: Option[Thread] = None
  Platform.setImplicitExit(false)

  def stopAll = {
    Platform.setImplicitExit(true)
    Platform.exit()
  }
  def noImplicitExit = Platform.setImplicitExit(false)

  def waitStarted = started match {
    case true =>

    case false =>

      var fxThread = new Thread(new Runnable() {
        def run = {

          try { Application.launch(classOf[JFXRun]) } finally {}

        }
      })
      fxThread.start()

      semaphore.acquire()
      semaphore.drainPermits()
      started = true;

  }

  def onJavaFXBlock[T](cl: => T): Option[T] = {
    onJavaFX[T](cl, true)
  }
  def onJavaFX[T](cl: => T, block: Boolean = false): Option[T] = {

    //println(s"Running JFX: " + started)
    started match {
      case true =>

        //println(s"Started, trying on JFX Thread")

        var r: Option[T] = None
        var currentSem = new Semaphore(0)
        Platform.runLater(new Runnable() {
          def run = {
            // println(s"Running on JFX Platform")
            try { r = Some(cl) } finally { currentSem.release }

          }
        })
        if (block) {
          currentSem.acquire()
        }
        //println(s"finished on javafx with block: " + block)
        //semaphore.acquire()
        r

      // No grants in semaphore, start application
      case false =>

        // Our Main app does release a credit in the semaphore
        waitStarted

        var r: Option[T] = None
        var currentSem = new Semaphore(0)
        Platform.runLater(new Runnable {

          def run = {
            try { r = Some(cl) } finally { currentSem.release }
          }
        })

        if (block) {
          currentSem.acquire()
        }
        //println(s"finished on javafx with block: " + block)

        // Acquire a semaphore to wait for the end of execution
        //semaphore.acquire()
        //semaphore.drainPermits()
        r

    }

  }

}
