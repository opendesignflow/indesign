package edu.kit.ipe.adl.indesign.core.harvest

import java.nio.file.Path
import edu.kit.ipe.adl.indesign.core.brain.LFCDefinition
import edu.kit.ipe.adl.indesign.core.brain.LFCSupport
import com.idyria.osi.tea.errors.ErrorSupport
import scala.reflect.ClassTag
import com.idyria.osi.tea.logging.TLogSource
import edu.kit.ipe.adl.indesign.core.config.Config
import edu.kit.ipe.adl.indesign.core.config.ConfigSupport
import com.idyria.osi.tea.compile.ClassDomain

/**
 * A harvester will look for resources, and call upon its child harvesters to match them
 *
 */
trait Harvester extends LFCSupport with ErrorSupport with TLogSource with ConfigSupport {

  override def getId = "default"

  // Config And Statistics
  //--------------
  var lastRun: Long = 0

  // Focused Mode
  // -- Don't run harvester unless explicitely
  //---------------
  var focused = false
  def isFocused = focused

  // Taint Check
  def isTainted = this.getClass.getClassLoader.isInstanceOf[ClassDomain] && this.getClass.getClassLoader.asInstanceOf[ClassDomain].tainted

  // Child Stuff
  //----------------------------
  var childHarvesters = List[Harvester]()
  var parentHarvester: Option[Harvester] = None

  def addChildHarvester(h: Harvester): Harvester = {
    this.childHarvesters = this.childHarvesters :+ h.asInstanceOf[Harvester]
    h.parentHarvester = Some(this)
    h
  }

  def addChildHarvester(h: Class[Harvester]): Harvester = {
    this.addChildHarvester(h.newInstance().asInstanceOf[Harvester])

  }
  def addChildHarvesterForce(h: Harvester): Harvester = {
    this.addChildHarvester(h.asInstanceOf[Harvester])

  }

  def removeChildHarvester(child: Harvester): Harvester = {

    // Remove from list
    this.childHarvesters = this.childHarvesters.filter(_ != child)

    // If we are the parent, remove
    child.parentHarvester match {
      case Some(parent) if (parent == this) => child.parentHarvester = None
      case _ =>
    }
    child
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

  def getResource[CT <: HarvestedResource](implicit cl: ClassTag[CT]): Option[CT] = {

    this.getResources.find { r => cl.runtimeClass.isInstance(r) } match {
      case Some(r) => Some(r.asInstanceOf[CT])
      case None => None
    }
  }

  /**
   * Do Exact Type check
   */
  def getResourcesOfType[CT <: HarvestedResource](implicit cl: ClassTag[CT]): List[CT] = {

    this.getResources.collect { case r if (cl.runtimeClass.isInstance(r)) => r.asInstanceOf[CT] }
  }
  
  /**
   * Type Check is done but no class casting will be involed
   * @warning: Useful to find objects matching a type whose definition may be outdated due to classloading reload
   */
  def getResourcesOfLazyType[CT <: HarvestedResource](implicit cl: ClassTag[CT]): List[HarvestedResource] = {

    this.getResources.collect { case r if (cl.runtimeClass.isInstance(r) || cl.runtimeClass.getCanonicalName==r.getClass.getCanonicalName) => r }
  }

  /**
   * Harvest Gather ressources, wich are added to gathered resources
   * implementations should override this method, then call super.harvest at the end
   */
  def harvest = {

    logFine[Harvester](s"----------- Starting Harvest on " + this.getClass.getCanonicalName)

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
      //finishGather
      try {
        doHarvest
        finishGather
      } catch {
        case e: NothingGatheredException =>
        case e: Throwable => throw e
      }

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
        c.finishGather
    }
    //finishHarvest(autoCleanResources)
  }

  /**
   * To be overriden by implementations
   */
  def doHarvest: Unit = {
    throw new NothingGatheredException
  }

  /**
   * Finish harvest by reloading harvested and gathered resources
   */
  def finishGather: Unit = {

    logFine[Harvester](s"----------- Starting finish Harvest on " + this.getClass.getCanonicalName + " with : " + this.harvestedResources.toList + " and " + this.availableResources.size + " available")

    // Clean Tainted resources
    //--------------
    this.availableResources.toList.foreach {
      case r if (r.isTainted) =>
        println(s"****** Resource $r is tainted; removing")
        this.availableResources -= r
        r.@->("clean", this)
      case _ =>
    }

    // Go through available resources
    //  -> Remove the ones which are not in the gathered, if autoclean is set to true
    //  -> Remove from gathered the one already available
    //  -> Add Remaining gathered
    this.availableResources.toList.foreach {
      case r =>

        var harvestedWithSameId = this.harvestedResources.filter {
          hr => (hr.getId == r.getId)
        }
        harvestedWithSameId.size match {
          // Not harvested and not rooted -> dissapear
          case 0 if (!r.rooted) =>
            logFine[Harvester](s"Resource ${r.getId} not rooted and not in harvested, removing")
            this.availableResources -= r
            r.@->("clean", this)
          // Remove from harvested, because already existing
          case size if (size>0) => 
            logFine[Harvester](s"Resource ${r.getId} already present, remove from harvested")
            this.harvestedResources --= harvestedWithSameId

            // Otherwise, keep in harvested resources for adding
          case _ =>

        }

       /* this.harvestedResources.find { hr => hr.getId == r.getId } match {

          // Remove non gathered  and non rooted
          case None if (!r.rooted) =>

          // Keep, then remove from gathered
          case Some(matchingHarvested) =>
            

          case _ =>
        }*/

    }

    // Add resources to available unless one resource with same ID exists
    this.harvestedResources.foreach {
      r =>
        this.availableResources += r
        r.originalHarvester match {
          case None => r.originalHarvester = Some(this)
          case _ => 
        }
        r.@->("added", this)
        r.@->("gathered", this)
    }

    // Reject
    this.harvestedResources.foreach {
      r =>
        r.@->("rejected", this)
    }
    this.harvestedResources.clear()

    logFine[Harvester](s"----------- Finish Harvest on " + this.getClass.getCanonicalName + " with : " + this.harvestedResources.toList + " and " + this.availableResources.size + " available")

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

  def deliverDirect(r: HarvestedResource): Boolean = {
    this.deliver(r) match {
      case true =>
        r.root
        this.finishGather
        true
      case false => false
    }

  }

  /**
   * Force clean this resource instance or equivalent ID
   */
  def cleanResource(r: HarvestedResource) = {

    this.availableResources.synchronized {
      this.availableResources = this.availableResources.filter {
        case fr if(fr == r || (fr.getId == r.getId))=> 
          fr.@->("clean",this)
          false
        case _ => true
          
      }
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