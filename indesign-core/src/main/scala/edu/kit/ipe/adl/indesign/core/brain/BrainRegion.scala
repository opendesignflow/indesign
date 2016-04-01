package edu.kit.ipe.adl.indesign.core.brain

import edu.kit.ipe.adl.indesign.core.brain.errors.ErrorSupport

trait BrainRegion extends BrainLifecycle  with ErrorSupport {
  
  
  def name = getClass.getSimpleName.replace("$","")
  
}