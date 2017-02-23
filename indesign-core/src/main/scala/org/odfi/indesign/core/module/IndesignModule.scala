package org.odfi.indesign.core.module

import org.odfi.indesign.core.brain.BrainRegion
import org.odfi.indesign.core.brain.Brain
import org.odfi.indesign.core.config.ConfigSupport


trait IndesignModule extends BrainRegion with ConfigSupport  {
  
  def requireModule(obj:IndesignModule) = {
    Brain.gatherPermanent(obj) match {
      case true => 
        obj.moveToLoad
      case false => 
    }
    
  }
  
}