package org.odfi.indesign.module.measurement

import org.odfi.indesign.core.harvest.HarvestedResource
import org.odfi.indesign.core.harvest.Harvester

trait Device extends HarvestedResource {
  
  
  def open
  def close
}

trait DeviceHarvester extends Harvester {
  
}