package org.odfi.indesign.core.heart

import java.util.Timer
import java.util.concurrent.Executors
import java.util.concurrent.FutureTask
import java.util.concurrent.ThreadFactory
import java.util.concurrent.ScheduledFuture
import org.odfi.indesign.core.harvest.Harvester
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.concurrent.CancellationException
import org.odfi.indesign.core.brain.BrainRegion

object Heart extends ThreadFactory with Harvester with BrainRegion {

  override def isTainted = super.isTainted

  // Thread creations 
  //---------------------

  //-- Thread group
  var threadGroup = new ThreadGroup("heart")


  def newThread(r: Runnable) = {

    var th = new Thread(threadGroup, r)
    th.setDaemon(true)
    th
  }

  /**
   * Background Executor
   */
  var timedExecutor = Executors.newScheduledThreadPool(Runtime.getRuntime.availableProcessors(), this)

  // Task Management
  //------------------------

  var tasks = scala.collection.mutable.Map[String, HeartTask[_]]()

  /**
   * Task is reset if saved
   */
  def saveTask(t: HeartTask[_]) = {
    tasks.synchronized {
      tasks.contains(t.getId) match {
        case true =>
          throw new RuntimeException(s"Cannot submit task ${t.getId}, another or same task with same id is already running")
        case false =>

          // Reset
          t.applyResetState

          // Save Task and remove on clean
          t.onCleaned {
            case h if (h == this) =>
              println("Cleaning Task")
              tasks -= t.getId
          }
          t.originalHarvester = Some(this)
          tasks += (t.getId -> t)
      }

    }
  }

  def removeTask(t: HeartTask[_]) = {


    //-- Make sure executor has no task
    t.scheduleFuture = None
    //t.scheduleEvery = None

    //-- REmove
    tasks.synchronized {
      tasks.contains(t.getId) match {
        case true =>
          tasks.remove(t.getId)
        case false =>

      }
    }
  }

  /**
   * Execute a Task
   *
   * Task is reset before pumping
   */
  def pump[RT](t: HeartTask[RT]): HeartTask[RT] = {


    saveTask(t)
    (t.scheduleAfter, t.scheduleEvery) match {
      case (Some(after), _) =>

        t.scheduleFuture = Some(timedExecutor.scheduleWithFixedDelay(t, t.scheduleDelay, after, t.timeUnit).asInstanceOf[ScheduledFuture[RT]])

      case (_, Some(every)) =>

        t.scheduleFuture = Some(timedExecutor.scheduleAtFixedRate(t, t.scheduleDelay, every, t.timeUnit).asInstanceOf[ScheduledFuture[RT]])

      case (None, None) =>

        t.scheduleFuture = Some(timedExecutor.schedule(t, t.scheduleDelay, t.timeUnit))
    }

    t.onClean {
      this.removeTask(t)
    }

    t
  }

  def repump[RT](t: HeartTask[RT]): HeartTask[RT] = {
    killTask(t)
    // t.waitForDone
    pump(t)
  }

  def pumpDefaultTask(id:String,cl: => Any) : HeartTask[Any] = {
    this.pump(new HeartTask[Any] {
      override def doTask: Unit = {
        cl
      }

      override def getId: String = id
    })
  }

  def getTaskById(id: String) = {
    this.tasks.get(id)
  }

  /**
   * Kill a task by ID if defined
   */
  def killTask(id: String): Unit = {

    this.tasks.get(id) match {
      case Some(t) =>
        killTask(t)
      case None =>
    }

  }

  def killTask(t: HeartTask[_]): Unit = {

    try {
      t.isRunning match {
        case true if (t.scheduleFuture.isEmpty) =>

          t.applyResetState

        case true =>

          var taskFuture = t.scheduleFuture.get

          logFine[HeartTask[_]]("Task is running, wait a bit or cancel")

          //-- If Task is single run, just wait a bit or based otherwise kill
          t.scheduleEvery match {
            case Some(scheduleRate) =>

              logFine[HeartTask[_]]("Task is periodic, Relasing stop signal")
              t.stopSignal.release
              try {
                taskFuture.get(scheduleRate, t.timeUnit)
              } catch {

                // Periodic task cancelled
                case e: CancellationException =>
                  logFine[HeartTask[_]]("Task is cancelled")
                  //t.taskStopped
                  //removeTask(t)
                case e: TimeoutException =>
                  try {
                    logFine[HeartTask[_]]("Task stop timed out, cancelling")
                    taskFuture.cancel(true)
                  } catch {
                    case e: CancellationException =>
                      onLogFine[HeartTask[_]] {
                        e.printStackTrace()
                      }

                  }
                case e: Throwable =>
                  onLogFine[HeartTask[_]] {
                    e.printStackTrace()
                  }

              }
            case None =>
              logFine[HeartTask[_]]("Task not periodic, releasing stop signal")
              t.stopSignal.release
              try {
                taskFuture.get(100, t.timeUnit)
              } catch {
                case e: TimeoutException =>
                  try {
                    logFine[HeartTask[_]]("Task stop timed out, cancelling")
                    taskFuture.cancel(true)
                  } catch {
                    case e: CancellationException =>
                      onLogFine[HeartTask[_]] {
                        e.printStackTrace()
                      }
                  }
                case e: CancellationException =>
                  onLogFine[HeartTask[_]] {
                    e.printStackTrace()
                  }
              }

          }

        // t.scheduleFuture.get.
        case false =>

          //t.scheduleEvery = None
          t.scheduleFuture match {
            case Some(future) =>
              future.cancel(true)
            case None =>

          }

      }
    } catch {
      case e: InterruptedException =>
        throw new InterruptedException("Current Thread interrupted while trying to stop another task")
    } finally {
      logFine[HeartTask[_]]("Remove Task now")
      t.taskStopped
      removeTask(t)
    }
  }

  // Inspection
  //-----------------

  /**
   * Running Returns the task if it in the run loop
   * That means if running or scheduled
   */
  def running(id: String): Option[HeartTask[_]] = {
    this.tasks.synchronized {
      tasks.get(id) match {
        case Some(t) =>
          Some(t)
        case other => None
      }
    }
  }

  // Lifecycles
  //-----------------
  this.onLoad {
    this.threadGroup = new ThreadGroup("heart")
    this.threadGroup.setDaemon(true)
  }
  this.onStop {

    var all = this.tasks
    all.foreach {
      case (id, t) =>
        killTask(t)
    }

    try {
      threadGroup.interrupt()
    } catch {
      case e: Throwable =>
        e.printStackTrace()
    }

  }

}