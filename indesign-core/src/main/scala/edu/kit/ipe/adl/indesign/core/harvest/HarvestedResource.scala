package edu.kit.ipe.adl.indesign.core.harvest

import scala.reflect.ClassTag

import com.idyria.osi.tea.errors.ErrorSupport
import com.idyria.osi.tea.listeners.ListeningSupport
import com.idyria.osi.tea.logging.TLogSource

import edu.kit.ipe.adl.indesign.core.brain.LFCDefinition
import edu.kit.ipe.adl.indesign.core.brain.LFCSupport

trait HarvestedResource extends ListeningSupport with LFCSupport with ErrorSupport with TLogSource {

  /**
   * ID used for cleanup
   */
  def getId: String

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

  var parentResource: Option[HarvestedResource] = None
  var derivedResources = Map[String, HarvestedResource]()

  /**
   * If derived from a resource, it becomes this resources parent
   * returns this
   */
  def deriveFrom(parentResource: HarvestedResource): HarvestedResource = {

    this.parentResource match {
      case Some(p) =>
      case None =>
        this.parentResource = Some(parentResource)
        parentResource.addDerivedResource(this)
      /* this.onAdded {
          case _ =>
            println(s"*** Resource gathered : $this , time to set derived resource to $parentResource *****")
            
        }*/
    }

    this
  }

  /**
   * Returns the child resource
   */
  def addDerivedResource(r: HarvestedResource): HarvestedResource = {

    // Add to local list, but not if already there
    derivedResources.get(r.getId) match {
      case Some(res) => 
        r
      case None =>
        derivedResources = derivedResources + (r.getId -> r)

        // Make sure we are the parent
        r.deriveFrom(this)
        r
    }

  }

  def findUpchainResource[CT <: HarvestedResource](implicit tag: ClassTag[CT]): Option[CT] = {

    var currentParent = this.parentResource
    var stop = false
    while (!stop && currentParent.isDefined) {

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

  def onDerivedResources[CT <: HarvestedResource](cl: PartialFunction[CT, Unit])(implicit tag: ClassTag[CT]): Unit = {
    // println(s"onDerivedResources on $this which has: ${derivedResources.size}")
    this.derivedResources.foreach {
      case (id, r) if (tag.runtimeClass.isInstance(r) && cl.isDefinedAt(r.asInstanceOf[CT])) =>
        cl(r.asInstanceOf[CT])
      case (id, r) =>
        // println(s"Recursino on $r which has: ${r.derivedResources.size}")
        r.onDerivedResources[CT](cl)
    }

  }

  def hasDerivedResourceOfType[CT <: HarvestedResource](implicit tag: ClassTag[CT]): Boolean = {

    this.derivedResources.find {
      case (id, r) if (tag.runtimeClass.isInstance(r)) =>
        true
      case (id, r) =>
        // println(s"Recursino on $r which has: ${r.derivedResources.size}")
        r.hasDerivedResourceOfType[CT]
    }.isDefined

    //this.derivedResources.find{case (id,r) if (tag.runtimeClass.isInstance(r))=>true;case _ => false }.isDefined
  }

  def findDerivedResourceOfType[CT <: HarvestedResource](implicit tag: ClassTag[CT]): Option[CT] = {
    this.derivedResources.collectFirst {
      case (id, r) if (tag.runtimeClass.isInstance(r)) =>
        r.asInstanceOf[CT]
      case (id, r) if (r.hasDerivedResourceOfType[CT]) =>
        // println(s"Recursino on $r which has: ${r.derivedResources.size}")
        r.findDerivedResourceOfType[CT].get
    }
  }

  // Lifecycle management
  //-------------------
  def onAdded(cl: PartialFunction[Harvester, Unit]) = {
    this.onWith("added") {
      h: Harvester =>
        cl.isDefinedAt(h) match {
          case true => cl(h)
          case false =>
        }
    }
  }
  def onGathered(cl: PartialFunction[Harvester, Unit]) = {
    this.onWith("gathered") {
      h: Harvester =>
        cl.isDefinedAt(h) match {
          case true => cl(h)
          case false =>
        }
    }

  }

  def onCleaned(cl: PartialFunction[Harvester, Unit]) = {
    this.onWith("cleaned") {
      h: Harvester =>
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