package edu.kit.ipe.adl.indesign.core.module.ui.www

import edu.kit.ipe.adl.indesign.core.harvest.Harvester

object WWWViewHarvester  extends Harvester[IndesignUIView] {
  
  override def deliver(r:IndesignUIView) = {
    gather(r)
    true
  }
  
  def harvest = {
    
  }
}