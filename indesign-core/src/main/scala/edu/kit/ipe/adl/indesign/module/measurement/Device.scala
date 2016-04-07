package edu.kit.ipe.adl.indesign.module.measurement

import edu.kit.ipe.adl.indesign.core.harvest.HarvestedResource
import edu.kit.ipe.adl.indesign.core.harvest.Harvester

trait Device extends HarvestedResource {
  
  
  def open
  def close
}

trait DeviceHarvester extends Harvester {
  
}