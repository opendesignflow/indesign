package edu.kit.ipe.adl.indesign.core.brain

trait BrainLifecycle extends LFCSupport {
  
  def onLoad(cl: => Unit) = {
    this.registerStateHandler("init"){cl}
  }
  
  def onInit(cl: => Unit) = {
    this.registerStateHandler("init"){cl}
  }
  def onStart(cl: => Unit) = {
    this.registerStateHandler("init"){cl}
  }
  def onStop(cl: => Unit) = {
    this.registerStateHandler("init"){cl}
  }
  
}