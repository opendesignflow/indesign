package edu.kit.ipe.adl.indesign.core.module

import edu.kit.ipe.adl.indesign.core.brain.BrainRegion
import edu.kit.ipe.adl.indesign.core.brain.SingleBrainRegion


trait IndesignBaseModule[CT <: BrainRegion[_]] extends BrainRegion[CT]  {
  
  
  def load
  
  this.onLoad {
    load
  }
}

trait IndesignModule extends IndesignBaseModule[BrainRegion[_]]  {
  
  
}