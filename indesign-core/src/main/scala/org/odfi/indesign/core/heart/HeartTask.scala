package org.odfi.indesign.core.heart

import java.util.concurrent.TimeUnit
import java.util.concurrent.FutureTask
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.Callable
import org.odfi.indesign.core.harvest.HarvestedResource
import org.odfi.indesign.core.brain.LFCDefinition
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
  
  // Period managing
  //------------
  def isPeriodical = (scheduleAfter.isDefined || scheduleEvery.isDefined)

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
          logFine[HeartTask[_]]("Running Task: "+getId)
          this.stopSignal.tryAcquire() match {
            case false =>
              logFine[HeartTask[_]]("No Stop")
              Some(doTask)
            case true =>
              logFine[HeartTask[_]]("Stop")
              taskStopped
              None
          }

        } catch {
          case e: InterruptedException =>
            taskStopped
            None
        } finally {

          // If Future is still present, and task is not periodical, clean
          this.scheduleFuture match {
           
            case Some(f) if (!isPeriodical) => 
               taskStopped
            case other => 
          }
          
          None
          
          // Close Task: Either clean or let got if it is periodical
          /*this.scheduleFuture match {
            case None =>
              //try { this.kill } catch { case e: Throwable => }
              this.stopSignal.release()
              taskStopped
            case Some(f) => 
              //-- Closre
          }*/
          
         /* (scheduleAfter, scheduleEvery) match {
            case (None, None) =>
              taskStopped
            case _ =>
          }*/
        }
      }
    } match {
      case Some(rt) => rt.asInstanceOf[PT]
      case None => null.asInstanceOf[PT]
    }
   
  }

  /**
   * Used to cleanup state and make sure task is in stop state
   */
  private def taskStopped = {

    
    
    //-- Call clean
    (scheduleAfter, scheduleEvery) match {
      case (None, None) =>
        this.@->("clean", this.originalHarvester)
      case _ =>
    }

    //-- Cancel to make sure task gets removed from executor
    this.scheduleFuture.get.cancel(true)
    this.scheduleFuture = None
    
    //-- Clean stop signal
    this.stopSignal.drainPermits()
    
    //-- Remove from Heart
    Heart.removeTask(this)
    
    //-- Move to done state
    HeartTask.moveToState(this, HeartTask.DONE.name)
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