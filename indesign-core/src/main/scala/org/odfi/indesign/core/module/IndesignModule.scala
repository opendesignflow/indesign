package org.odfi.indesign.core.module

import org.odfi.indesign.core.brain.BrainRegion
import org.odfi.indesign.core.brain.Brain


trait IndesignModule extends BrainRegion  {
  
  def requireModule(obj:IndesignModule) = {
    println(s"Delivering module $obj...")
    Brain.deliverDirect(obj)
  }
  
}