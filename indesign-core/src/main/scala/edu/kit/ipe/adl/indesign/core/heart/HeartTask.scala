package edu.kit.ipe.adl.indesign.core.heart

import java.util.concurrent.TimeUnit
import java.util.concurrent.FutureTask
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.Callable
import edu.kit.ipe.adl.indesign.core.harvest.HarvestedResource

/**
 * Spec for a Heart Task
 */
trait HeartTask[PT] extends  Callable[PT] with Runnable with HarvestedResource with HeartTimingSupport{
  
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
  var scheduleEvery : Option[Long] = None
  
  /**
   * Schedule the task to reschedule after the specified delay
   */
  var scheduleAfter : Option[Long] = None
  
  // Scheduled Interface
  //---------------
  var scheduleFuture : Option[ScheduledFuture[PT]] = None
  
  def isDone = scheduleFuture match {
    case Some(f) => 
      f.isDone()
    case None => false
  }
  
  // Activity to help clever scheduling
  //-----------------
  var activity = 0
  
  // Timings
  //----------------
  
  // Run
  //--------------
  def run = call
  def call = {
    
    time("run") {
      catchErrorsOn(this) {
         doTask
      }
     
    }  
  }
  
  def doTask : PT 
  
  
  
  
}


trait DefaultHeartTask extends HeartTask[Unit] {
  
}