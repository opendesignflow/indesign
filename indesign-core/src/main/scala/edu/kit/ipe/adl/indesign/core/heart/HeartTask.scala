package edu.kit.ipe.adl.indesign.core.heart

import java.util.concurrent.TimeUnit
import java.util.concurrent.FutureTask
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.Callable
import edu.kit.ipe.adl.indesign.core.harvest.HarvestedResource
import edu.kit.ipe.adl.indesign.core.brain.LFCDefinition
import java.util.concurrent.Semaphore

/**
 * Spec for a Heart Task
 */
trait HeartTask[PT] extends Callable[PT] with Runnable with HarvestedResource with HeartTimingSupport {

  // id
  //-----------

  var scheduleId: String = ""

  // Schedule Parameters
  //-----------------

  var timeUnit = TimeUnit.MILLISECONDS

  /**
   * First Delay
   */
  var scheduleDelay = 0L

  /**
   * Schedule the task to run at every ms
   */
  var scheduleEvery: Option[Long] = None

  /**
   * Schedule the task to reschedule after the specified delay
   */
  var scheduleAfter: Option[Long] = None

  // Scheduled Interface
  //---------------
  var scheduleFuture: Option[ScheduledFuture[PT]] = None

  /*def isDone = scheduleFuture match {
    case Some(f) =>
      f.isDone()
    case None if (lastRun==0) => false
    case _ => true
  }*/

  // Activity to help clever scheduling
  //-----------------
  //var state: HeartTask.State = HeartTask.NONE
  var activity = 0
  var lastRun = 0

  /* def isDone = state match {
    case HeartTask.RUNNING => false
    case _ => true
  }
  
  def isRunning = state match {
    case HeartTask.RUNNING => true
    case _ => false
  }*/

  def isDone = currentState match {
    case Some(HeartTask.DONE.name) => true
    case _ => false
  }

  def waitForDone = this.waitForState(HeartTask.DONE.name)

  def isRunning = currentState match {
    case Some(HeartTask.RUNNING.name) => true
    case _ => false
  }

  def waitForRunning = this.waitForState(HeartTask.RUNNING.name)

  var stopSignal = new Semaphore(0)
  
  def kill = {
    Heart.killTask(this)
  }

  // Timings
  //----------------

  // Run
  //--------------
  def run = call
  def call = {
    catchErrorsOn(this) {

      HeartTask.moveToState(this, HeartTask.RUNNING.name)
      time("run") {
        try {

          // Run Task
          logFine[HeartTask[_]]("Running Task")
          this.stopSignal.tryAcquire() match {
            case false =>
              logFine[HeartTask[_]]("No Stop")
              doTask
            case true =>
              logFine[HeartTask[_]]("Stop")
              taskStopped
          }

        } 
        catch {
          case e : InterruptedException => 
            taskStopped
        }
        finally {

          // Close Task: Either clean or let got if it is periodical
          (scheduleAfter, scheduleEvery) match {
            case (None, None) =>
              taskStopped
            case _ =>
          }
        }
      }.asInstanceOf[PT]
    }
  }

  /**
   * Used to cleanup state and make sure task is in stop state
   */
  private def taskStopped = {

    HeartTask.moveToState(this, HeartTask.DONE.name)
    (scheduleAfter, scheduleEvery) match {
      case (None, None) =>
        this.@->("clean", this.originalHarvester)
      case _ =>
    }
    this.scheduleFuture = None
  }

  def doTask: PT

}

trait DefaultHeartTask extends HeartTask[Unit] {

  def getId = hashCode.toString
}



object HeartTask extends LFCDefinition {

  sealed trait State { def name: String }
  case object NONE extends State { val name = "NONE" } //etc.
  case object READY extends State { val name = "READY" } //etc.
  case object RUNNING extends State { val name = "RUNNING" } //etc.
  case object DONE extends State { val name = "DONE" } //etc.

  defineState(NONE.name)
  defineState(READY.name)
  defineState(RUNNING.name)
  defineState(DONE.name)

}