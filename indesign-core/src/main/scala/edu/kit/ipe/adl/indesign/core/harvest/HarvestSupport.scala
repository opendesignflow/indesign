package edu.kit.ipe.adl.indesign.core.harvest

trait HarvestSupport[RT <: HarvestedResource] {
  
  // Filters
  //---------------
  
  // Children Harvester
  //--------
  var childHarvesters = List[Harvester[RT]]() 
  
  def addChildHarvester(h:Harvester[RT]) = this.childHarvesters = this.childHarvesters :+ h
}