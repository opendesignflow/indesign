package edu.kit.ipe.adl.indesign.core.harvest

import java.nio.file.Path
import edu.kit.ipe.adl.indesign.core.brain.LFCDefinition
import edu.kit.ipe.adl.indesign.core.brain.LFCSupport
import com.idyria.osi.tea.errors.ErrorSupport
import org.scalatest.matchers.HavePropertyMatcher
import scala.reflect.ClassTag

/**
 * A harvester will look for resources, and call upon its child harvesters to match them
 *
 */
trait Harvester extends LFCSupport with ErrorSupport {

  var lastRun: Long = 0

  // Child Stuff
  //----------------------------
  var childHarvesters = List[Harvester]()
  var parentHarvester: Option[Harvester] = None

  def addChildHarvester(h: Harvester): Unit = {
    this.childHarvesters = this.childHarvesters :+ h.asInstanceOf[Harvester]
    h.parentHarvester = Some(this)
  }

  def addChildHarvester(h: Class[Harvester]): Unit = {
    this.addChildHarvester(h.newInstance().asInstanceOf[Harvester])

  }
  def addChildHarvesterForce(h: Harvester): Unit = {
    this.addChildHarvester(h.asInstanceOf[Harvester])

  }

  def hierarchyName(sep: String = ".", withoutSelf: Boolean = false) = {

    // Get Parent Line
    var parents = withoutSelf match {
      case true => List[Harvester]()
      case false => List[Harvester](this)
    }
    var currentParent: Option[Harvester] = this.parentHarvester
    while (currentParent != None) {
      parents = parents :+ currentParent.get
      currentParent = currentParent.get.parentHarvester
    }

    // Return String
    parents.reverse.map(h => h.getClass.getSimpleName.replace("$", "") + "-" + h.hashCode()).mkString(sep)

  }

  def hierarchy(withoutSelf: Boolean = false) = {

    // Get Parent Line
    var parents = withoutSelf match {
      case true => List[Harvester]()
      case false => List[Harvester](this)
    }
    var currentParent: Option[Harvester] = this.parentHarvester
    while (currentParent != None) {
      parents = parents :+ currentParent.get
      currentParent = currentParent.get.parentHarvester
    }

    // Return String
    parents.reverse

  }

  // harvest 
  //------------------
  var availableResources = scala.collection.mutable.LinkedHashSet[HarvestedResource]()
  var harvestedResources = scala.collection.mutable.LinkedHashSet[HarvestedResource]()

  /**
   * After a Harvest run, clean available Resources which were not present in the gather outcome
   */
  var autoCleanResources = true

  /**
   * Store a resource in local gathered resources for later processing
   */
  def gather[CT <: HarvestedResource](r: CT) = {
    this.harvestedResources += r
    r
  }

  /**
   * Return available resources list
   */
  def getResources = this.availableResources.toList

  def onResources[CT <: HarvestedResource](pf: Function[CT, Unit])(implicit cl: ClassTag[CT]) = {

    this.getResources.foreach {
      case r if (cl.runtimeClass.isInstance(r)) => pf(r.asInstanceOf[CT])
      case _ =>
    }
  }

  /**
   * Harvest Gather ressources, wich are added to gathered resources
   * implementations should override this method, then call super.harvest at the end
   */
  def harvest = {

    // Deliver current resources to childnre
    //--------------------
    this.childHarvesters.foreach {
      ch =>
        this.availableResources.foreach {
          r =>
            //ch.deliver(r)
        }
      //ch.finishHarvest(ch.autoCleanResources)
    }

    // Do harvest implementation for extra resources
    //-----------------
    var oldcl = Thread.currentThread().getContextClassLoader
    try {
      this.lastRun = System.currentTimeMillis()
      Thread.currentThread().setContextClassLoader(this.getClass.getClassLoader)
      doHarvest
      finishGather
    } catch {
      case e: Throwable =>
        e.printStackTrace()
        throw e
    } finally {
      Thread.currentThread().setContextClassLoader(oldcl)
    }

    // Finish Gathering on children
    //-------------
    this.childHarvesters.foreach {
      c =>
        this.availableResources.filter(r => !r.local).foreach {
          r =>
            c.deliver(r)
        }
       // c.finishGather
    }
    //finishHarvest(autoCleanResources)
  }

  /**
   * To be overriden by implementations
   */
  def doHarvest = {
    
  }

  /**
   * Finish harvest by reloading harvested and gathered resources
   */
  def finishGather : Unit = {

    //println(s"----------- Starting finish Harvest on "+this.getClass.getCanonicalName)

    // Go through available resources
    //  -> Remove the ones which are not in the gathered, if autoclean is set to true
    //  -> Remove from gathered the one already available
    //  -> Add Remaining gathered
    this.availableResources.toList.foreach {
      case r if (!r.rooted) =>

        this.harvestedResources.find { hr => hr.getId == r.getId } match {

          // Remove non gathered  and non 
          case None =>
            this.availableResources -= r
            r.@->("clean", this)


          // Keep, then remove from gathered
          case Some(matchingHarvested) =>
            this.harvestedResources -= matchingHarvested

        }
      case _ =>
    }

    // Add resources to available
    this.harvestedResources.foreach {
      r =>
        this.availableResources += r
        r.@->("gathered", this)
        r.@->("added", this)
    }
    
    // Reject
    this.harvestedResources.foreach {
      r => 
        r.@->("rejected",this)
    }
    this.harvestedResources.clear()

    // Deliver resources to child harvesters, and run a finish harvect on them too
    //--------------
    /*this.childHarvesters.foreach {
      ch =>
        this.availableResources.foreach {
          r =>
            ch.deliver(r)
        }
       //ch.finishHarvest(ch.autoCleanResources)
    }*/

  }

  var deliverClosure: PartialFunction[HarvestedResource, Boolean] = { case _ => false }

  //def deliver_=(pf:PartialFunction[HarvestedResource,Boolean]) : Unit = this.deliverClosure = pf
  //def deliver :  PartialFunction[HarvestedResource,Boolean] = this.deliverClosure

  /**
   * If a parent harvester runs, it delivers resources to its child harvesters
   * Deliver is called by parent
   */
  def deliver(r: HarvestedResource): Boolean = {
    deliverClosure.isDefinedAt(r) match {
      case true => deliverClosure(r)
      case false => false
    }
  }

  def onDeliver(pf: PartialFunction[HarvestedResource, Boolean]): Unit = {
    this.deliverClosure = pf
  }

  def onDeliverFor[CT <: HarvestedResource](pf: PartialFunction[CT, Boolean])(implicit cl: ClassTag[CT]): Unit = {
    this.deliverClosure = {
      case r if (cl.runtimeClass.isInstance(r)) => 
        
        pf.isDefinedAt(r.asInstanceOf[CT]) match {
          case true => pf(r.asInstanceOf[CT])
          case false => false
        }
        
    }
  }

  def deliverDirect(r: HarvestedResource): Boolean = {
    this.deliver(r) match {
      case true =>
        r.root
        this.finishGather
        true
      case false => false
    }

  }

  // Resource Process
  //----------------------
  def processResources = {
    this.getResources.foreach {
      r =>
        this.keepErrorsOn(r) {

          HarvestedResource.moveToState(r, "processed")
        }
    }

  }

  // Auto Harvest Resolution (ensure at the end)
  //-----------------
  Harvest.updateAutoHarvesterOn(this)

}

object Harvester extends LFCDefinition {

  this.defineState("init")
  this.defineState("ok")
  this.defineState("inactive")

}