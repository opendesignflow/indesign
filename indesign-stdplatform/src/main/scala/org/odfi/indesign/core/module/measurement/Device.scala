package org.odfi.indesign.core.module.measurement

import org.odfi.indesign.core.harvest.HarvestedResource
import org.odfi.indesign.core.harvest.Harvester

trait Device extends HarvestedResource {
  
  
  def open : Unit
  def close : Unit
}

trait DeviceHarvester extends Harvester {
  
}