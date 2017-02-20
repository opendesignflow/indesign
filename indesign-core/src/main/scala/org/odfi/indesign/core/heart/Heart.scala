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
  var threadGroup = new ThreadGroup("Indesign.Heart")

  def newThread(r: Runnable) = {

    var th = new Thread(threadGroup, r)
    th.setDaemon(true)
    th
  }

  /**
   * Background Executor
   */
  val timedExecutor = Executors.newScheduledThreadPool(8, this)

  // Task Management
  //------------------------

  var tasks = scala.collection.mutable.Map[String, HeartTask[_]]()

  def saveTask(t: HeartTask[_]) = {
    tasks.synchronized {
      tasks.contains(t.getId) match {
        case true =>
          throw new RuntimeException(s"Cannot submit task ${t.getId}, another or same task with same id is already running")
        case false =>

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
          tasks -= t.getId
        case false =>

      }
    }
  }

  /**
   * Execute a Task
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

    t
  }

  def repump[RT](t: HeartTask[RT]): HeartTask[RT] = {
    killTask(t)
    // t.waitForDone
    pump(t)
  }

  def killTask(t: HeartTask[_]): Unit = {

    try {
      t.isRunning match {
        case true =>
        
          var taskFuture = t.scheduleFuture.get
          //-- If Task is single run, just wait a bit or based otherwise kill
          t.scheduleEvery match {
            case Some(scheduleRate) =>

              t.stopSignal.release
              try {
                taskFuture.get(scheduleRate, TimeUnit.MILLISECONDS)
              } catch {
                case e: TimeoutException =>
                  try {
                    taskFuture.cancel(true)
                  } catch {
                    case e: CancellationException =>
                  }
                case e: CancellationException =>

              }
            case None =>
              t.stopSignal.release
              try {
                taskFuture.get(100, TimeUnit.MILLISECONDS)
              } catch {
                case e: TimeoutException =>
                  try {
                    taskFuture.cancel(true)
                  } catch {
                    case e: CancellationException =>
                  }
                case e: CancellationException =>
              }

          }

        // t.scheduleFuture.get.
        case false =>
          
      }
    } catch {
      case e: InterruptedException =>
        throw new InterruptedException("Current Thread interrupted while trying to stop another task")
    } finally {
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
      tasks.get(id)
    }
  }

  // Lifecycles
  //-----------------
  
  this.onStop {
    var all = this.tasks
    all.foreach {
      case (id, t) =>
        killTask(t)
    }
  }

}