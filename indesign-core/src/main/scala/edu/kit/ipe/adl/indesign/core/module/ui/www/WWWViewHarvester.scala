package edu.kit.ipe.adl.indesign.core.module.ui.www

import edu.kit.ipe.adl.indesign.core.harvest.Harvester

object WWWViewHarvester  extends Harvester[IndesignUIView,IndesignUIView] {
  
  this.autoCleanResources = false
  
  override def deliver(r:IndesignUIView) = {
    gather(r)
    println(s"Got a view delivered, size now: "+this.getResources.size)
    true
  }
  
  def doHarvest = {
    
  }
}