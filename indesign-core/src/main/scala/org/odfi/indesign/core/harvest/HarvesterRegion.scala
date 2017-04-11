package org.odfi.indesign.core.harvest

import org.odfi.indesign.core.brain.BrainRegion

trait HarvesterRegion extends BrainRegion with Harvester{
  
  override def getId = super.getId
}