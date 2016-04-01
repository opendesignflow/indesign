package edu.kit.ipe.adl.indesign.core.module.ui.www

import edu.kit.ipe.adl.indesign.core.harvest.Harvester

object WWWViewHarvester  extends Harvester[IndesignUIView,IndesignUIView] {
  
  this.autoCleanResources = false
  
  override def deliver(r:IndesignUIView) = {
    gather(r)
    true
  }
  
  def doHarvest = {
    
  }
}