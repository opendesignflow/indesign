package edu.kit.ipe.adl.indesign.core.heart

import java.util.Timer
import java.util.concurrent.Executors
import java.util.concurrent.FutureTask
import java.util.concurrent.ThreadFactory
import java.util.concurrent.ScheduledFuture

object Heart extends ThreadFactory {

  // Thread creations 
  //---------------------

  //-- Thread group
  var threadGroup = new ThreadGroup("Indesign.Heart")

  def newThread(r: Runnable) = {

    var th = new Thread(threadGroup, r)

    th
  }

  /**
   * Background Executor
   */
  val timedExecutor = Executors.newScheduledThreadPool(8, this)

  /**
   * Execute a Task
   */
  def pump[RT](t: HeartTask[RT]): Unit = {

    (t.scheduleAfter, t.scheduleEvery) match {
      case (Some(after), _) =>
        
        t.scheduleFuture = Some(timedExecutor.scheduleWithFixedDelay(t, t.scheduleDelay, after, t.timeUnit).asInstanceOf[ScheduledFuture[RT]])
        
      case (_,Some(every)) =>
        
        t.scheduleFuture = Some(timedExecutor.scheduleAtFixedRate(t, t.scheduleDelay, every, t.timeUnit).asInstanceOf[ScheduledFuture[RT]])
        
      case (None,None) =>
        
        t.scheduleFuture = Some(timedExecutor.schedule(t, t.scheduleDelay, t.timeUnit))
    }

  }

}