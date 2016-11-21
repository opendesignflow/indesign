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
import java.util.concurrent.Semaphore

/**
 * A harvester will look for resources, and call upon its child harvesters to match them
 *
 */
trait Harvester extends LFCSupport with ErrorSupport with TLogSource with ConfigSupport {

  //override def getId = "default"

  // Config And Statistics
  //--------------
  var lastRun: Long = 0

  // Focused Mode
  // -- Don't run harvester unless explicitely
  //---------------
  //var focused = false
  //def isFocused = focused

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

  def -->(h: Harvester): Harvester = {
    addChildHarvester(h)
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

  /**
   * Type Check is done but no class casting will be involed
   * @warning: Useful to find objects matching a type whose definition may be outdated due to classloading reload
   */
  def getChildHarvesters[CT <: Harvester](implicit cl: ClassTag[CT]): Option[List[Harvester]] = {

    this.childHarvesters.collect { case r if (cl.runtimeClass.isInstance(r) || cl.runtimeClass.getCanonicalName == r.getClass.getCanonicalName) => r } match {
      case res if (res.size > 0) => Some(res)
      case res => None
    }
  }
  
  /**
   * Return Top Harvester
   */
  def getTopHarvester = {
    
    var current = this
    while (current.parentHarvester.isDefined) {
      current = current.parentHarvester.get 
    }
    current
    
  }

  /**
   * Process Depth first ordered
   */
  def onChildHarvestersLevelOrder(cl: Harvester => Unit): Unit = {

    var processList = new scala.collection.mutable.Stack[Harvester]()
    processList ++= this.childHarvesters

    while (processList.nonEmpty) {

      var h = processList.pop()

      keepErrorsOn(h) {
        // Process Current harvester
        cl(h)
        // Add children to end of process list to keep level order
        processList.pushAll(h.childHarvesters)
      }
    }

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
  def gatherAll[CT <: HarvestedResource](r: List[CT]) = {
    this.harvestedResources ++= (r)
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
  
  def getResourceExact[CT <: HarvestedResource](implicit cl: ClassTag[CT]): Option[CT] = {

    this.getResources.find { r => cl.runtimeClass.isInstance(r) && cl.runtimeClass==r} match {
      case Some(r) => Some(r.asInstanceOf[CT])
      case None => None
    }
  }

  /**
   * Do Type check, will match all resources of type CT
   */
  def getResourcesOfType[CT <: HarvestedResource](implicit cl: ClassTag[CT]): List[CT] = {

    this.getResources.collect { case r if (cl.runtimeClass.isInstance(r)) => r.asInstanceOf[CT] }
  }
  
   /**
   *  @see getResourcesOfType
   */
  def getResourcesOfTypeClass[CT <: HarvestedResource](cl: Class[CT]): List[CT] = {

    this.getResources.collect { case r if (cl.isInstance(r)) => r.asInstanceOf[CT] }
  }
  
  /**
   * Returns only the resources whose type is exactly CT
   */
  def getResourcesOfExactType[CT <: HarvestedResource](implicit cl: ClassTag[CT]): List[CT] = {

    this.getResources.collect { case r if (cl.runtimeClass.isInstance(r) && cl.runtimeClass.getCanonicalName == r.getClass.getCanonicalName) => r.asInstanceOf[CT] }
  }

 

  /**
   * Type Check is done but no class casting will be involed
   * @warning: Useful to find objects matching a type whose definition may be outdated due to classloading reload
   */
  def getResourcesOfLazyType[CT <: HarvestedResource](implicit cl: ClassTag[CT]): List[HarvestedResource] = {

    this.getResources.collect { case r if (cl.runtimeClass.isInstance(r) || cl.runtimeClass.getCanonicalName == r.getClass.getCanonicalName) => r }
  }

  def hasResources = this.availableResources.size match {
    case 0 => false
    case _ => true
  }
  /**
   * Runs closure if no resources are available
   */
  def noResources(cl: => Any): Option[Any] = hasResources match {
    case true =>
      None
    case false =>
      Some(cl)
  }

  /**
   * Runs closure if resources are available
   */
  def ifResources(cl: => Any): Option[Any] = hasResources match {
    case true =>
      Some(cl)
    case false =>
      None
  }

  def withResources[CT <: HarvestedResource](closure: List[CT] => Any)(implicit cl: ClassTag[CT]): Option[Any] = {
    var resourcesList = this.getResourcesOfType[CT]
    resourcesList.isEmpty match {
      case true =>
        None
      case false =>
        Some(closure(resourcesList))
    }
  }

  /**
   * Harvest Gather ressources, wich are added to gathered resources
   * implementations should override this method, then call super.harvest at the end
   */
  def harvest = {
    this.synchronized {
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
          finishGather()
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
          c.synchronized {
            this.availableResources.filter(r => !r.local).foreach {
              r =>
                c.deliver(r)
            }
            c.finishGather()
          }

      }

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
  def finishGather(dispatchResources: Boolean = false): Unit = {

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
    
    // Clean Gathered Resources: Remove duplicate ID
    /*this.harvestedResources.groupBy { r => r.getId }.foreach {
      case (k,vals) => vals.drop(1).foreach(this.harvestedResources.remove(_))
    }*/

    // Go through available resources
    //  -> Remove the ones which are not in the gathered, if autoclean is set to true
    //  -> Remove from gathered the one already available
    //  -> Add Remaining gathered
    var toclean = List[HarvestedResource]()
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
            toclean = toclean :+ r

          // Remove from harvested, because already existing
          case size if (size > 0) =>
            logFine[Harvester](s"Resource ${r.getId} already present, remove from harvested")
            this.harvestedResources --= harvestedWithSameId

          // Otherwise, keep in harvested resources for adding
          case _ =>

        }

    }

    // Call kept on all kepts resources and children
    //-------------
    this.availableResources.foreach {
      r =>
        r.@->("kept", this)
        r.getDerivedResources[HarvestedResource].foreach {
          dr =>
            dr.@->("kept", this)
        }

    }

    // Add resources to available unless one resource with same ID exists
    //-------------
    this.harvestedResources.foreach {
      r =>
        this.availableResources += r
        r.originalHarvester match {
          case None => r.originalHarvester = Some(this)
          case _ =>
        }
        r.@->("added", this)
    }

    // Clean old resources
    //-----------------
    logFine[Harvester](s"========= CLEAN ------------")
    toclean.foreach {
      r =>
        // println(s"====== $r")
        r.@->("clean", this)
    }
    logFine[Harvester](s"========= EOF CLEAN ==============")

    // Call Gathered
    //--------------------

    //-- Call Gathered on the single resources newly added
    this.harvestedResources.foreach {
      r =>
        logFine[Harvester](s"Gathered on: " + r)
        r.@->("gathered", this)
        this.@->("gathered", r)
    }

    //-- Call GatheredResources on the Harvester itself
    //println("Gathered resources: "+this.harvestedResources)
    this.@->("gatheredResources", this.harvestedResources.toList)

    this.harvestedResources.clear()

    // Utilities updates
    //-----------------------

    //-- Wait for resources
    this.availableResources.size match {

      // NO resources -> Remove permits
      case 0 =>
        this.waitForResourcesAvailableSemaphore.drainPermits()
      // Some resources and no permits, deliver one 
      case _ if (this.waitForResourcesAvailableSemaphore.availablePermits() == 0) =>
        this.waitForResourcesAvailableSemaphore.release
      // Some resources and a permit is already there, do nothing
      case _ =>
    }

    logFine[Harvester](s"----------- Finish Harvest on " + this.getClass.getCanonicalName + " with : " + this.harvestedResources.toList + " and " + this.availableResources.size + " available")

    // Deliber to children
    if (dispatchResources) {
      this.childHarvesters.foreach {
        c =>
          c.synchronized {
            this.availableResources.filter(r => !r.local).foreach {
              r =>
                c.deliver(r)
            }
            c.finishGather(dispatchResources=true)
          }

      }
    }

  }

  //def deliver_=(pf:PartialFunction[HarvestedResource,Boolean]) : Unit = this.deliverClosure = pf
  //def deliver :  PartialFunction[HarvestedResource,Boolean] = this.deliverClosure

  /**
   * If a parent harvester runs, it delivers resources to its child harvesters
   * Deliver is called by parent
   */
  def deliver(r: HarvestedResource): Boolean = {

    deliverClosures.filter { p => p.isDefinedAt(r) }.map { p => p(r) }.find { _ == true }.isDefined
    /* deliverClosure.isDefinedAt(r) match {
      case true => deliverClosure(r)
      case false => false
    }*/
  }

  def deliverDirect(r: HarvestedResource): Boolean = {
    this.deliver(r) match {
      case true =>
        r.root
        this.finishGather()
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
        case fr if (fr == r || (fr.getId == r.getId)) =>
          fr.@->("clean", this)
          false
        case _ => true

      }
    }

  }

  def cleanResources = {
    this.availableResources.clear()
    this.harvestedResources.clear()
  }
  
  def cleanResourcesOfType[RT <: HarvestedResource](implicit tag: ClassTag[RT]) = {
    
    this.getResourcesOfType[RT].foreach(cleanResource(_))
  }

  /**
   * Replace all resources of matching type with new ones
   */
  def replaceResourcesOfType[RT <: HarvestedResource](newR: List[RT])(implicit tag: ClassTag[RT]) = {

    // Clean resources of type
    this.getResourcesOfType[RT].foreach(cleanResource(_))

    this.gatherDirectAll(newR)
  }

  /**
   * Replace single resource of certain type with one
   */
  def replaceResourceOfType[RT <: HarvestedResource](newR: RT)(implicit tag: ClassTag[RT]) = {

    // Clean resources of type
    this.getResource[RT] match {
      case Some(r) => this.cleanResource(r)
      case None =>
    }
    this.gatherDirect(newR)
  }

  // Delivery
  //--------------

  var deliverClosures: List[PartialFunction[HarvestedResource, Boolean]] = List({ case _ => false })

  def onDeliver(pf: PartialFunction[HarvestedResource, Boolean]): Unit = {
    this.deliverClosures = deliverClosures :+ pf
  }

  def onDeliverFor[CT <: HarvestedResource](pf: PartialFunction[CT, Boolean])(implicit cl: ClassTag[CT]): Unit = {

    var realClosure: PartialFunction[HarvestedResource, Boolean] = {
      case r if (cl.runtimeClass.isInstance(r)) =>

        pf.isDefinedAt(r.asInstanceOf[CT]) match {
          case true => pf(r.asInstanceOf[CT])
          case false => false
        }

    }
    this.deliverClosures = deliverClosures :+ realClosure
  }

  /**
   * Gathers a resource and calls finish harvest
   * Resource is not rooted
   */
  def gatherDirect(r: HarvestedResource) = {
    this.synchronized {
      this.gather(r)
      this.finishGather(true)
    }

  }

  def gatherDirectAll(r: List[HarvestedResource]) = {
    this.synchronized {
      this.gatherAll(r)
      this.finishGather(true)
    }

  }

  /**
   * Gathers a resource and calls finish harvest
   * Resource is  rooted
   *
   */
  def gatherPermanent(r: HarvestedResource) = {
    this.synchronized {
      r.root
      this.gather(r)
      this.finishGather(true)
    }

    /*this.availableResources.contains(r) match {
      case true => 
        false
      case false => 
        r.root
        this.availableResources += r
        true
    }*/
  }
  /**
   * Direct gather just adds a resource to the availableResources
   * @returns true if added, false if not
   */
  def saveToAvailableResources(r: HarvestedResource) = {

    this.availableResources.contains(r) match {
      case true =>
        false
      case false =>
        r.root
        this.availableResources += r
        true
    }

  }

  // Resource Process
  //----------------------

  def onGatheredResources(cl: List[HarvestedResource] => Unit) = {
    this.onWith("gatheredResources") {
      rl: List[HarvestedResource] => cl(rl)
    }
  }

  def onGathered[T <: HarvestedResource](cl: T => Unit)(implicit tag: ClassTag[T]) = {
    this.onWith[T]("gathered") {
      rl: T => cl(rl)
    }
  }

  def processResources = {
    this.getResources.foreach {
      r =>
        this.keepErrorsOn(r) {

          HarvestedResource.moveToState(r, "processed")
        }
    }

  }

  // Resources Waiting Utilities
  //-----------------

  /**
   * The semaphore for resources waiting
   */
  val waitForResourcesAvailableSemaphore = new Semaphore(0)

  /**
   * This function is called by a thread which will be blocking until some resources are available on this harvester
   */
  def waitForResourcesAvailable(cl: => Unit) = {

    this.availableResources.size match {
      case 0 =>

        //-- Acquire and release as waiting procedure
        waitForResourcesAvailableSemaphore.acquire()
        waitForResourcesAvailableSemaphore.release()

        //-- Run
        cl

      case _ => cl
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