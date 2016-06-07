package edu.kit.ipe.adl.indesign.core.brain

trait BrainLifecycle extends LFCSupport {
  
  def moveToSetup = {
    Brain.moveToState(this, "setup")
  }
  
  def onSetup(cl: => Unit) = {
    this.registerStateHandler("setup"){cl}
  }
  
  
  def moveToLoad = {
    Brain.moveToState(this, "load")
  }
  
  def onLoad(cl: => Unit) = {
    this.registerStateHandler("load"){cl}
  }
  
  def moveToInit = {
    Brain.moveToState(this, "init")
  }
  def onInit(cl: => Unit) = {
    this.registerStateHandler("init"){cl}
  }
  
  def moveToStart = {
    Brain.moveToState(this, "start")
  }
  def onStart(cl: => Unit) = {
    this.registerStateHandler("start"){cl}
  }
  
  def moveToStop = {
    Brain.moveToState(this, "stop")
  }
  def onStop(cl: => Unit) = {
    this.registerStateHandler("stop"){cl}
  }
  
  def moveToShutdown = {
    Brain.moveToState(this, "shutdown")
  }
  def onShutdown(cl: => Unit) = {
    this.registerStateHandler("shutdown"){cl}
  }
  
}

trait BrainLifecyleDefinition extends LFCDefinition {
  
  this.defineState("setup")
  this.defineState("load")
  this.defineState("init")
  this.defineState("start")
  this.defineState("stop")
  this.defineState("shutdown")
}

