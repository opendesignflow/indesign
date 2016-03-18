package edu.kit.ipe.adl.indesign.core.brain

trait BrainRegion extends BrainLifecycle {
  
  
  def name = getClass.getSimpleName.replace("$","")
  
}