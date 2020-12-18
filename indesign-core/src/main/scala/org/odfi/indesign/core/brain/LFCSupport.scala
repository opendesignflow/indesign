package org.odfi.indesign.core.brain

import org.odfi.tea.listeners.ListeningSupport
import java.util.concurrent.Phaser
import java.util.concurrent.Semaphore

import org.odfi.tea.compile.ClassDomainSupport
import org.odfi.tea.logging.TLogSource

/**
 * @author zm4632
 */
trait LFCSupport extends ListeningSupport with TLogSource {

  var statesHandlers = Map[String, List[() => Unit]]()
  var currentState: Option[String] = None
  val lfcSemaphore = new Semaphore(0)

  /*val syncLock

  def syncState(cl: => Unit) = {

  }*/

  def isCurrentState(str: String) = {
    currentState match {
      case Some(s) if (s == str) => true
      case other => false
    }
  }

  def applyResetState = {

    statesHandlers.get("##reset##") match {
      case Some(handlers) =>
        handlers.foreach {
          h => h()
        }
      case None =>
      //throw new RuntimeException(s"Cannot apply state $str to ${getClass.getName}, no handlers defined")
    }

    this.currentState = None
    lfcSemaphore.release(lfcSemaphore.getQueueLength)
    this.@->("state.updated")

  }

  def applyState(str: String) = {


    //-- Update state
    this.synchronized {
      this.currentState = Some(str)
    }

    //-- Make sure waiters advance
    lfcSemaphore.getQueueLength match {
      case 0 => lfcSemaphore.release()
      case other => lfcSemaphore.release(other)
    }

    //-- Execute state closures
    statesHandlers.get(str) match {
      case Some(handlers) =>

        logFine[LFCDefinition](s"Applying $str ${handlers.size} handlers onto ${getClass.getCanonicalName}")

        handlers.foreach {
          h =>
            logFine[LFCDefinition](s"Running ${getClass.getCanonicalName} closure...." + h.hashCode())
            try {
              h()
            } catch {
              case e: Throwable =>
                e.printStackTrace()
                throw e
            }
        }
      case None =>
      //throw new RuntimeException(s"Cannot apply state $str to ${getClass.getName}, no handlers defined")
    }



    //this.lfcPhaser.register()
    //this.lfcPhaser.arriveAndDeregister()
    this.@->("state.updated")
  }

  def registerStateHandler(state: String)(h: => Unit) = {

    val cl = {
      () => h
    }
    logFine[LFCDefinition](s"Registering $state handler on ${getClass.getCanonicalName}...." + cl.hashCode())

    this.statesHandlers.get(state) match {
      case Some(handlers) =>

        this.statesHandlers = this.statesHandlers.updated(state, cl :: handlers)

      case None =>
        this.statesHandlers = this.statesHandlers.updated(state, List(cl))

    }

    /*var handlers = this.statesHandlers.getOrElseUpdate(str, new scala.collection.mutable.ListBuffer[() => Unit])
    handlers += { () => h }*/
  }

  def onState(state: String)(h: => Unit) = {

    registerStateHandler(state) {
      h
    }

    /*  var handlers = this.statesHandlers.getOrElseUpdate(str, new scala.collection.mutable.ListBuffer[() => Unit])
      handlers += { () => h }*/
  }

  /**
   * Blocks until the provided state has been reached
   */
  def waitForState(expectedState: String) = {

    var stateVal = this.synchronized {
      currentState
    }
    //println(s"Waiting for $expectedState , current = $stateVal")

    stateVal match {
      case Some(st) if (st == expectedState) =>
      case _ =>
        var wait = true
        //var phase = lfcPhaser.register()
        while (wait) {

          stateVal = this.synchronized {
            currentState
          }
          stateVal match {
            case Some(st) if (st == expectedState) =>
              wait = false
            case _ =>
              lfcSemaphore.acquire()

          }

          //phase = lfcPhaser.awaitAdvance(phase+1)

          // println(s"Arrived: $currentState")

        }

    }

  }

}

trait LFCDefinition extends ClassDomainSupport with TLogSource {

  var states = List[String]()


  def resetLFCState(lifecyclable: LFCSupport) = {
    logFine[LFCDefinition](s"Resetting state of " + lifecyclable)
    lifecyclable.applyResetState

    //lifecyclable.@->("resetState")
  }

  def defineState(name: String) = {
    states = states :+ name
  }

  def moveToState(lifecyclable: LFCSupport, targetState: String): Unit = {


    assert(states.contains(targetState), s"Cannot move to non defined state $targetState")

    logFine[LFCDefinition](s"Updateing state of " + lifecyclable + " to " + targetState + s"(${this.states.indexOf(targetState)})")
    //var origin = new RuntimeException("")
    //origin.printStackTrace(System.out)
    withClassLoaderFor[Unit](lifecyclable.getClass) {

      //println(s"Moving to state $targetState")
      // Get index of target state and current State
      var targetStateIndex = this.states.indexOf(targetState)
      var currentStateIndex = lifecyclable.currentState match {
        case Some(current) => this.states.indexOf(current)
        case None => -1
      }
      logFine[LFCDefinition](s"Moving ${lifecyclable} from $currentStateIndex to $targetStateIndex")
      // println(s"Current: $currentStateIndex")
      // If target is before current, just jump to it
      (targetStateIndex - currentStateIndex) match {

        // Stay
        case 0 =>

        // Go Back to first state, then move again
        case r if (r <= 0) =>
        //lifecyclable.applyState(states(0))
        // moveToState(lifecyclable, states(targetStateIndex))
        //lifecyclable.applyState(states(targetStateIndex))

        // Go to
        case r =>

          // Scroll to target state, and execute all the states which are higher than current state
          (0 to targetStateIndex) foreach {
            case i if (i > currentStateIndex) =>
              lifecyclable.applyState(states(i))
              currentStateIndex = i
            case _ =>
          }
      }
    }
    logFine[LFCDefinition](s"Done state update")

  }

  def moveToStateWithLoop(lifecyclable: LFCSupport, targetState: String): Unit = {


    assert(states.contains(targetState), s"Cannot move to non defined state $targetState")

    logFine[LFCDefinition](s"Updateing state of " + lifecyclable + " to " + targetState + s"(${this.states.indexOf(targetState)})")
    //var origin = new RuntimeException("")
    //origin.printStackTrace(System.out)
    withClassLoaderFor[Unit](lifecyclable.getClass) {

      //println(s"Moving to state $targetState")
      // Get index of target state and current State
      var targetStateIndex = this.states.indexOf(targetState)
      var currentStateIndex = lifecyclable.currentState match {
        case Some(current) => this.states.indexOf(current)
        case None => -1
      }
      logFine[LFCDefinition](s"Moving ${lifecyclable} from $currentStateIndex to $targetStateIndex")
      // println(s"Current: $currentStateIndex")
      // If target is before current, just jump to it
      (targetStateIndex - currentStateIndex) match {

        // Stay
        case 0 =>
          logFine[LFCDefinition](s"Not Changing State, already correct")

        // Go Back to first state, then move again
        case r if (r <= 0) =>
          lifecyclable.applyState(states(0))
          moveToState(lifecyclable, states(targetStateIndex))

        //lifecyclable.applyState(states(targetStateIndex))

        // Go to
        case r =>

          // Scroll to target state, and execute all the states which are higher than current state
          ((currentStateIndex + 1) to targetStateIndex) foreach {
            case i if (i > currentStateIndex) =>
              lifecyclable.applyState(states(i))
              currentStateIndex += 1
            case _ =>
          }
      }
    }
    logFine[LFCDefinition](s"Done state update")

  }

}