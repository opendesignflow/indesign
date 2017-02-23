package org.odfi.indesign.core.module

import org.odfi.indesign.core.harvest.Harvester
import org.odfi.indesign.core.harvest.Harvest

trait HarvesterModule extends IndesignModule with Harvester {
   
  override def getId = super.getId
  
  this.onInit {
    Harvest.addHarvester(this)
  }
  
  this.onStop {
    Harvest.removeHarvester(this)
    this.clean
  }
  
}