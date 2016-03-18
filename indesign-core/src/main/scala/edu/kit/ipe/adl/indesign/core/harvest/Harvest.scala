package edu.kit.ipe.adl.indesign.core.harvest

import edu.kit.ipe.adl.indesign.core.brain.BrainRegion

object Harvest extends BrainRegion {
  
  // Top Harvesters
  //------------------
  
  var harvesters =  List[Harvester[_]]()
  def addHarvester(h:Harvester[_]) = {
    this.harvesters = this.harvesters :+ h
    h
  }
  
  
  def run = {
    
    harvesters.foreach {
      h => 
        h.harvest
    }
  }
}