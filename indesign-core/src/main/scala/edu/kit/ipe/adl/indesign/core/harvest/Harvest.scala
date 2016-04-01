package edu.kit.ipe.adl.indesign.core.harvest

import edu.kit.ipe.adl.indesign.core.brain.BrainRegion
import edu.kit.ipe.adl.indesign.core.brain.errors.ErrorSupport

object Harvest extends BrainRegion {

  // Top Harvesters
  //------------------

  var harvesters = List[Harvester[_, _]]()
  def addHarvester(h: Harvester[_, _]) = {
    this.harvesters = this.harvesters :+ h
    h
  }

  def run = {

    harvesters.foreach {
      h =>
        keepErrorsOn(h) { 
          h.resetErrors
          h.harvest
        }
    }
  }
}