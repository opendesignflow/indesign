package org.odfi.indesign.core.module

import org.odfi.indesign.core.brain.BrainRegion
import org.odfi.indesign.core.brain.Brain
import org.odfi.indesign.core.config.ConfigSupport
import org.odfi.indesign.core.brain.ExternalBrainRegion

trait IndesignModule extends BrainRegion with ConfigSupport {

  def requireModule(obj: IndesignModule) = {
    this.parentResource match {

      // Add to parent region if is external, and both required and local share same classloader
      case Some(pr: ExternalBrainRegion) if (this.getClass.getClassLoader == obj.getClass.getClassLoader) =>
        println(s"Requiring ${obj} , with parent region: ${pr}")
        pr.addDerivedResource(obj)
        pr.currentState match {
          case Some(state) =>
            Brain.moveToState(obj, state)
          case None => 
        }
      case other =>
        Brain.gatherPermanent(obj) match {
          case true =>
            obj.moveToLoad
          case false =>
        }
    }

  }

}