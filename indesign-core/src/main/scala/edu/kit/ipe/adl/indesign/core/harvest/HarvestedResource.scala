package edu.kit.ipe.adl.indesign.core.harvest

import com.idyria.osi.tea.listeners.ListeningSupport
import edu.kit.ipe.adl.indesign.core.brain.LFCDefinition
import edu.kit.ipe.adl.indesign.core.brain.LFCSupport
import com.idyria.osi.tea.errors.ErrorSupport
import scala.reflect.ClassTag
import com.idyria.osi.tea.logging.TLogSource

trait HarvestedResource extends ListeningSupport with LFCSupport with ErrorSupport with TLogSource {
  
  
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
  
  /**
   * If local, a resource won't be propagated to children
   */
  var local = false
  
  // Parenting
  //-------------------
  
  var parentResource : Option[HarvestedResource] = None
  
  /**
   * If derived from a resource, it becomes this resources parent
   */
  def deriveFrom(r:HarvestedResource) = {
    this.parentResource match {
      case Some(r) => 
      case None => 
        this.parentResource = Some(r)
    }
    this
  } 
  
  def findUpchainResource[CT <: HarvestedResource](implicit tag:ClassTag[CT]) : Option[CT] = {
    
    var currentParent = this.parentResource
    var stop = false
    while(!stop && currentParent.isDefined) {
      
      tag.runtimeClass.isInstance(currentParent.get) match {
        case true => 
          stop = true
        case false => 
          currentParent = currentParent.get.parentResource
      }
    
    }
    
    
    currentParent match {
      case Some(res) => Some(res.asInstanceOf[CT]) 
      case None => None
    }
    
    
  }
  
  // Lifecycle management
  //-------------------
  def onAdded(cl: PartialFunction[Harvester,Unit]) = {
    this.onWith("added") {
      h : Harvester => 
      cl.isDefinedAt(h) match {
        case true => cl(h)
        case false => 
      }
    }
  }
  def onGathered(cl: PartialFunction[Harvester,Unit]) = {
    this.onWith("gathered") {
      h : Harvester => 
      cl.isDefinedAt(h) match {
        case true => cl(h)
        case false => 
      }
    }
    
  }
  
  def onCleaned(cl:PartialFunction[Harvester,Unit]) = {
    this.onWith("cleaned") {
      h : Harvester => 
      cl.isDefinedAt(h) match {
        case true => cl(h)
        case false => 
      }
    }
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