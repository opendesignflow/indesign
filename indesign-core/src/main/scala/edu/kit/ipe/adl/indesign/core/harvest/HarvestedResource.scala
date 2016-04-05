package edu.kit.ipe.adl.indesign.core.harvest

import com.idyria.osi.tea.listeners.ListeningSupport
import edu.kit.ipe.adl.indesign.core.brain.LFCDefinition
import edu.kit.ipe.adl.indesign.core.brain.LFCSupport
import edu.kit.ipe.adl.indesign.core.brain.errors.ErrorSupport

trait HarvestedResource extends ListeningSupport with LFCSupport with ErrorSupport {
  
  
  /**
   * ID used for cleanup
   */
  def getId : String
  
  /**
   * A rooted resource won't be deleted after harvesting and cleaning up the actual resources with the new resources found
   */
  var rooted = false

  def root = {
    rooted = true
    this
  }
  
  def unroot = {
    rooted = false
    true
  }
  
  // Lifecycle management
  //-------------------
  def onAdded(cl: Harvester[_,_] => Unit) = {
    this.onWith("added")(cl)
  }
  
  def onCleaned(cl:Harvester[_,_] => Unit) = {
    this.onWith("cleaned")(cl)
  }
  
  def onProcess(cl: => Unit) = {
    this.registerStateHandler("processed") {
      cl
    }
  }
  
}

object HarvestedResource extends LFCDefinition {
  
  this.defineState("new")
  this.defineState("processed")
  
}