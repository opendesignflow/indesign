package edu.kit.ipe.adl.indesign.core.module

import edu.kit.ipe.adl.indesign.core.brain.BrainRegion
import edu.kit.ipe.adl.indesign.core.brain.Brain


trait IndesignModule extends BrainRegion  {
  
  def requireModule(obj:IndesignModule) = {
    println(s"Delivering module $obj...")
    Brain.deliverDirect(obj)
  }
  
}