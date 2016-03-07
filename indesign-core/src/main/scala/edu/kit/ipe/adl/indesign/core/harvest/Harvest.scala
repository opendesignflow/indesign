package edu.kit.ipe.adl.indesign.core.harvest

object Harvest {
  
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