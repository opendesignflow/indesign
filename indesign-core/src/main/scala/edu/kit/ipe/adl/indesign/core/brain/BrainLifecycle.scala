package edu.kit.ipe.adl.indesign.core.brain

trait BrainLifecycle extends LFCSupport {
  
  def onLoad(cl: => Unit) = {
    this.registerStateHandler("load"){cl}
  }
  
  def onInit(cl: => Unit) = {
    this.registerStateHandler("init"){cl}
  }
  def onStart(cl: => Unit) = {
    this.registerStateHandler("start"){cl}
  }
  def onStop(cl: => Unit) = {
    this.registerStateHandler("stop"){cl}
  }
  
}

trait BrainLifecyleDefinition extends LFCDefinition {
  
  
  this.defineState("load")
  this.defineState("init")
  this.defineState("start")
  this.defineState("stop")
}