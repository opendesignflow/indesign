package edu.kit.ipe.adl.indesign.core.brain

object Brain extends BrainLifecyleDefinition with BrainLifecycle {
  
  
  
  // Regions
  //--------------------
  var regions = List[BrainRegion[_]]()
  
  def +=(rs:BrainRegion[_]*) =  {
    this.regions = this.regions ++ rs
    Brain.currentState match {
      case Some(state) => rs.foreach { r => Brain.moveToState(r,state)}
      case None => 
    }
    
  }
  
  
  /**
   * Process Depth first ordered
   */
  def onAllRegions(cl: BrainRegion[_] => Unit) = {

    var processList = new scala.collection.mutable.ListBuffer[BrainRegion[_]]()
    processList ++= this.regions

    while(processList.nonEmpty) {
      var r : BrainRegion[_] = processList.head
      processList -= r
      r.keepErrorsOn(r) {
          cl(r)
          processList ++= r.subRegions.toTraversable.asInstanceOf[Traversable[BrainRegion[_]]]
        }
    }
    

  }
  
  // Lifecylce
  //------------------
  
  def load = {
    Brain.moveToState(this, "load")
    this.regions.foreach {
      r => Brain.moveToState(r, "load")
    }
  }
  
  def init = {
    Brain.moveToState(this, "init")
    this.regions.foreach {
      r => Brain.moveToState(r, "init")
    }
  }
  
}