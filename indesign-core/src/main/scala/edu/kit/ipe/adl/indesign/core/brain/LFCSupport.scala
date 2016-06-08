package edu.kit.ipe.adl.indesign.core.brain

import com.idyria.osi.tea.listeners.ListeningSupport
import java.util.concurrent.Phaser
import java.util.concurrent.Semaphore
import com.idyria.osi.tea.compile.ClassDomainSupport
import com.idyria.osi.tea.logging.TLogSource

/**
 * @author zm4632
 */
trait LFCSupport extends ListeningSupport {

  val statesHandlers = scala.collection.mutable.Map[String, scala.collection.mutable.ListBuffer[() => Unit]]()
  var currentState: Option[String] = None
  val lfcSemaphore = new Semaphore(0)

  def applyState(str: String) = {

    this.synchronized {
      this.currentState = Some(str)
    }

    statesHandlers.get(str) match {
      case Some(handlers) =>
        handlers.foreach {
          h => h()
        }
      case None =>
      //throw new RuntimeException(s"Cannot apply state $str to ${getClass.getName}, no handlers defined")
    }

    lfcSemaphore.getQueueLength match {
      case 0 => lfcSemaphore.release()
      case l => lfcSemaphore.release(l)
    }

    //this.lfcPhaser.register()
    //this.lfcPhaser.arriveAndDeregister()
    this.@->("state.updated")
  }

  def registerStateHandler(str: String)(h: => Unit) = {

    var handlers = this.statesHandlers.getOrElseUpdate(str, new scala.collection.mutable.ListBuffer[() => Unit])
    handlers += { () => h }
  }

  def onState(str: String)(h: => Unit) = {

    var handlers = this.statesHandlers.getOrElseUpdate(str, new scala.collection.mutable.ListBuffer[() => Unit])
    handlers += { () => h }
  }

  /**
   * Blocks until the provided state has been reached
   */
  def waitForState(expectedState: String) = {

    var stateVal = this.synchronized {
      currentState
    }

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

  def defineState(name: String) = {
    states = states :+ name
  }

  def moveToState(lifecyclable: LFCSupport, targetState: String): Unit = {

    assert(states.contains(targetState), s"Cannot move to non defined state $targetState")

    logFine[LFCDefinition](s"Updateing state of "+lifecyclable+" to "+targetState+s"(${this.states.indexOf(targetState)})")
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
      logFine[LFCDefinition](s"Moving from $currentStateIndex to $targetStateIndex")
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

    logFine[LFCDefinition](s"Updateing state of "+lifecyclable+" to "+targetState+s"(${this.states.indexOf(targetState)})")
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
      logFine[LFCDefinition](s"Moving from $currentStateIndex to $targetStateIndex")
      // println(s"Current: $currentStateIndex")
      // If target is before current, just jump to it 
      (targetStateIndex - currentStateIndex) match {

        // Stay
        case 0 =>

        // Go Back to first state, then move again
        case r if (r <= 0) =>
          lifecyclable.applyState(states(0))
          moveToState(lifecyclable, states(targetStateIndex))
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

}