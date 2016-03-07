package edu.kit.ipe.adl.indesign.core.heart

import java.util.concurrent.FutureTask
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors



object Heart {
  
  val executor = Executors.newWorkStealingPool()
  
  /**
   * Execute a Task 
   */
  def pump( t : FutureTask[Any]) : Unit = {
    
  }
  
}