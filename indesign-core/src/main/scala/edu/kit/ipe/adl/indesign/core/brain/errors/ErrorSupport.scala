package edu.kit.ipe.adl.indesign.core.brain.errors

import scala.collection.mutable.Stack

trait ErrorSupport {

  var errors = Stack[Throwable]()

  // Error Add/Remove
  //-----------
  def addError(e: Throwable) = {
    this.errors.push(e)
  }
  
  def resetErrors = this.errors.clear()

  // Errors get
  //---------------
  def hasErrors = this.errors.size match {
    case 0 => false
    case _ => true
  }

  def getLastError = this.errors.headOption

  // Comsume etc..
  //------------------

  /**
   * Run somethign and catch error on target object
   * Error is transmitted
   */
  def catchErrorsOn[RT](target: ErrorSupport)(cl: => RT): RT = {

    try {
      cl
    } catch {
      case e: Throwable =>
        target.addError(e)
        throw e
    }
  }

  /**
   * Catch errors but don't transmit them
   * Closure returns None in that case
   */
  def keepErrorsOn[RT](target: ErrorSupport)(cl: => RT): Option[RT] = {
    try {
      Some(cl)
    } catch {
      case e: Throwable =>
        target.addError(e)
        None
    }
  }

}