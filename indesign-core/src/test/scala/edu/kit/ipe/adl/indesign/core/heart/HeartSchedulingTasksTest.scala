package org.odfi.indesign.core.heart

import org.scalatest.FunSuite
import java.util.concurrent.Semaphore

import org.odfi.indesign.core.brain.LFCDefinition
import org.odfi.tea.logging.TLog

class HeartSchedulingTasksTest extends FunSuite {
  
  
  test("Start Simple task and check cleanning") {
    
    
    //-- Create semaphore
    var receiveS = new Semaphore(0)
    
    //-- Task
    var t = new DefaultHeartTask {
      
      def doTask = {
        println(s"Task release")
        receiveS.release()
      }
    }
    //-- Start
    Heart.pump(t)
    
    //-- Wait for running, do this after task has started to resolve sync issue
    //Thread.sleep(1000)
    t.waitForRunning
    t.waitForDone
    println(s"--> Finished")

    //-- this check will be done before the Heart Thread had time to cleanup, so wait a bit
    Thread.sleep(500)
    assertResult(0)(Heart.tasks.size)
    assertResult(1)(receiveS.availablePermits())
    
    
  }
  
  test("Start a repetitive task and stop it with Step synchronisation") {
    
    TLog.setLevel(classOf[HeartTask[_]], TLog.Level.FULL)
    TLog.setLevel(classOf[LFCDefinition], TLog.Level.FULL)
    
    //-- Create semaphore
    var receiveS = new Semaphore(0)
    var advanceS = new Semaphore(0)
    
    //-- Task
    var t = new DefaultHeartTask {
      
      this.scheduleEvery = Some(1000)
      def doTask = {
        println(s"Task wait")
        advanceS.acquire()
        println(s"Task release")
        receiveS.release()
      }
    }
    //-- Start
    Heart.pump(t)
    
    //-- Wait for running, do this after task has started to resolve sync issue
    //Thread.sleep(1000)
    t.waitForRunning
    println(s"--> Running detected")
    
    //-- Wait for two credits
    t.waitForRunning
    println(s"--> Advance")
    advanceS.release
    println(s"---> Wait")
    receiveS.acquire()
    
     println(s"--> Advance")
    advanceS.release
    println(s"---> Wait")
    receiveS.acquire()
    
    //-- Kill Task
    //-- !! Warning, if system is too slow, next schedule may happen before kill task signal and ruing the test
    println(s"Release and kill")
    advanceS.release
    Heart.killTask(t)
    
    //-- Wait for done
    println(s"Waiting for task to be marked done")
    t.waitForDone
    println(s"Task killed and marked as done")
    
    //-- Should be no pending credits in receive semaphore
    assertResult(0, "No Remaning releases in task")(receiveS.availablePermits())
    
  }
  
  test("Start a repetitive task stop and restart it") {
    
    TLog.setLevel(classOf[HeartTask[_]], TLog.Level.FULL)
    
    //-- Create semaphore
    var receiveS = new Semaphore(0)
    var advanceS = new Semaphore(0)
    
    //-- Task
    var t = new DefaultHeartTask {
      
      override def getId = "Test Task"
      
      this.scheduleEvery = Some(1000)
      def doTask = {
        println(s"Run Task, acquire advance")
        advanceS.acquire()
        receiveS.release()
      }
    }
    //-- Start
    Heart.pump(t)
    
    //-- Wait for running, do this after task has started to resolve sync issue
    t.waitForRunning
    
    //-- Release 5 credits and acquire 5
    advanceS.release(5)
    receiveS.acquire(5)
    
    //-- Repump
    Heart.killTask(t)
    println(s"Waiting for done")
    t.waitForDone
     println(s"Task done")
    Heart.pump(t)
    
    //-- Release 5 credits and acquire 5
    advanceS.release(5)
    receiveS.acquire(5)
    Heart.killTask(t)
     t.waitForDone
   
    
    //-- Should be no pending credits in receive semaphore
    assertResult(0, "No Remaning releases in task")(receiveS.availablePermits())
    
  }
  
}