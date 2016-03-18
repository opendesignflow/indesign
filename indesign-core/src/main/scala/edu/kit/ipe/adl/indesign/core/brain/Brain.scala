package edu.kit.ipe.adl.indesign.core.brain

object Brain extends BrainLifecyleDefinition {
  
  
  
  // Regions
  //--------------------
  var regions = List[BrainRegion]()
  
  def +=(r:BrainRegion*) = this.regions = this.regions ++ r
  
  
  // Lifecylce
  //------------------
  
  def load = {
    this.regions.foreach {
      r => Brain.moveToState(r, "load")
    }
  }
  
  def init = {
    this.regions.foreach {
      r => Brain.moveToState(r, "init")
    }
  }
  
}