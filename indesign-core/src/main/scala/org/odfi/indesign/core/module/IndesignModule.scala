package org.odfi.indesign.core.module

import org.odfi.indesign.core.brain.BrainRegion
import org.odfi.indesign.core.brain.Brain
import org.odfi.indesign.core.config.ConfigSupport
import org.odfi.indesign.core.brain.ExternalBrainRegion
import org.odfi.indesign.core.harvest.HarvestedResource
import org.odfi.indesign.core.harvest.Harvester
import org.odfi.indesign.core.harvest.Harvest

trait IndesignModule extends BrainRegion with ConfigSupport {

  def requireHarvester(obj:Harvester) = {
    Harvest.addHarvester(obj)
  }
  
  def requireModule(obj: IndesignModule) = {
    //println("Requiring " + obj)
    //println(s"Parent of ${getDisplayName} is ${parentResource}")
    this.parentResource match {

      // Add to parent region if is external, and both required and local share same classloader
      case Some(pr: ExternalBrainRegion) if (this.getClass.getClassLoader == obj.getClass.getClassLoader) =>

        //println(s"Requiring ${obj} , with parent region: ${pr}")

        pr.addDerivedResource(obj)
        pr.currentState match {
          case Some(state) =>
            Brain.moveToState(obj, state)
          case None =>
        }

      // Add to parent region if is external, and both required and local share same classloader
      case Some(pr: HarvestedResource) if (this.getClass.getClassLoader == obj.getClass.getClassLoader) =>

        //println(s"Requiring ${obj} , with parent region: ${pr}")

        addDerivedResource(obj)
        currentState match {
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

  this.onClean {
    //println(s"Cleaning module: "+this+" of state: "+this.currentState)
    this.moveToShutdown
  }
}