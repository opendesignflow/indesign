package org.odfi.indesign.core.harvest

import java.nio.file.Path
import org.odfi.indesign.core.brain.LFCDefinition
import org.odfi.indesign.core.brain.LFCSupport
import com.idyria.osi.tea.errors.ErrorSupport
import scala.reflect.ClassTag
import com.idyria.osi.tea.logging.TLogSource
import org.odfi.indesign.core.config.Config
import org.odfi.indesign.core.config.ConfigSupport
import com.idyria.osi.tea.compile.ClassDomain
import java.util.concurrent.Semaphore
import org.odfi.indesign.core.brain.BrainRegion

/**
 * A harvester will look for resources, and call upon its child harvesters to match them
 *
 */
trait Harvester extends LFCSupport with ErrorSupport with TLogSource with ConfigSupport {

  def getId = getClass.getCanonicalName

  override def getDisplayName = getClass.getSimpleName

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
  override def isTainted = this.getClass.getClassLoader.isInstanceOf[ClassDomain] && this.getClass.getClassLoader.asInstanceOf[ClassDomain].tainted

  // parent Stuff
  //------------
  def addParent(p: Harvester) = p match {
    case same if (same == this) =>
    case already if (parentHarvesters.contains(already)) =>
    case other =>
      this.parentHarvesters = this.parentHarvesters :+ other
      other.addChildHarvester(this)
  }

  def removeParent(p: Harvester) = this.parentHarvesters.contains(p) match {
    case true =>
    case false =>
      this.parentHarvesters = this.parentHarvesters.filter(_ != p)
      p.removeChildHarvester(this)
  }

  // Child Stuff
  //----------------------------
  var childHarvesters = List[Harvester]()
  var parentHarvesters = List[Harvester]()

  def addChildHarvester(h: Harvester): Harvester = {

    this.childHarvesters.contains(h) match {
      case true =>
      case false =>
        this.childHarvesters = this.childHarvesters :+ h.asInstanceOf[Harvester]
        h.addParent(this)
    }

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
    this.childHarvesters.contains(child) match {
      case true =>
        this.childHarvesters = this.childHarvesters.filter(_ != child)
        child.removeParent(this)
      case false =>
    }

    // If we are the parent, remove
    /*child.parentHarvester match {
      case Some(parent) if (parent == this) => child.parentHarvester = None
      case _ =>
    }*/
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
  /*def getTopHarvester = {

    var current = this
    while (current.parentHarvester.isDefined) {
      current = current.parentHarvester.get
    }
    current

  }*/

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

  /**
   * This will go the first hierarchy line
   */
  def hierarchyName(sep: String = ".", withoutSelf: Boolean = false) = {

    // Get Parent Line
    var parents = withoutSelf match {
      case true => List[Harvester]()
      case false => List[Harvester](this)
    }
    var currentParent: Option[Harvester] = this.parentHarvesters.headOption
    while (currentParent != None) {
      parents = parents :+ currentParent.get
      currentParent = currentParent.get.parentHarvesters.headOption
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
    var currentParent: Option[Harvester] = this.parentHarvesters.headOption
    while (currentParent != None) {
      parents = parents :+ currentParent.get
      currentParent = currentParent.get.parentHarvesters.headOption
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

    this.getResources.find { r => cl.runtimeClass.isInstance(r) && cl.runtimeClass == r } match {
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

  def getResourcesByTypeAndUpchainParent[CT <: HarvestedResource, UT <: HarvestedResource: ClassTag](upChain: UT)(implicit cl: ClassTag[CT]): List[CT] = {
    this.getResourcesOfType[CT].filter {
      r =>
        r.findUpchainResource[UT] match {
          case Some(ut) => ut == upChain
          case other => false
        }
    }
  }

  def getResourcesByTypeAndUpchainParents[CT <: HarvestedResource, UT <: HarvestedResource: ClassTag](implicit cl: ClassTag[CT]): List[CT] = {
    this.getResourcesOfType[CT].filter {
      r =>
        r.findUpchainResource[UT] match {
          case Some(ut) => true
          case other => false
        }
    }
  }

  def getResourceById[CT <: HarvestedResource](id: String)(implicit cl: ClassTag[CT]) = {
    this.getResourcesOfType[CT].find(_.getId == id)
  }

  def hasResources = this.availableResources.size match {
    case 0 => false
    case _ => true
  }

  def hasResource(obj: HarvestedResource): Boolean = {
    this.availableResources.contains(obj)
  }

  def withResource(obj: HarvestedResource)(cl: => Unit): Unit = {
    this.hasResource(obj) match {
      case true =>
        cl
      case false =>
    }
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

  def walkResourcesOfType[CT <: HarvestedResource](cl: CT => Any)(implicit clt: ClassTag[CT]): Unit = {
    var resourcesList = this.getResourcesOfType[CT]
    var remaningResources = scala.collection.mutable.Stack[CT]()
    remaningResources ++= resourcesList
    while (!remaningResources.isEmpty) {

      var current = remaningResources.pop()
      cl(current)
      remaningResources ++= current.getDerivedResources[CT]

    }
  }

  /**
   * Harvest Gather ressources, wich are added to gathered resources
   * implementations should override this method, then call super.harvest at the end
   */
  def harvest = {
    this.synchronized {
      logFine[Harvester](s"----------- Starting Harvest on " + this.getClass.getCanonicalName)

      this.isTainted match {
        case true =>
          clean
        case false =>
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
              case e: Throwable =>
                e.printStackTrace()
                throw e
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

    }
    //finishHarvest(autoCleanResources)
  }

  /**
   * To be overriden by implementations
   */
  def doHarvest: Unit = {
    //throw new NothingGatheredException
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
        r.clean
      case _ =>
    }

    // Clean Gathered Resources: Remove duplicate ID
    /*this.harvestedResources.groupBy { r => r.getId }.foreach {
      case (k,vals) => vals.drop(1).foreach(this.harvestedResources.remove(_))
    }*/

    // Go through available resources
    //  -> Remove the ones which are not in the gathered, and are not derived
    //  -> Remove from gathered the one already available
    //  -> Add Remaining gathered
    var toclean = List[HarvestedResource]()
    this.availableResources.toList.foreach {
      case r =>

        var harvestedWithSameId = this.harvestedResources.filter {
          hr => (hr.getId == r.getId)
        }
        harvestedWithSameId.size match {
          // Not harvested and not rooted and not derived -> dissapear
          case 0 if (!r.rooted && r.parentResource.isEmpty) =>
            logFine[Harvester](s"Resource ${r.getId} not rooted and not in harvested, removing")
            this.availableResources -= r
            toclean = toclean :+ r

          // Remove from harvested, because already existing
          case size if (size > 0) =>
            logFine[Harvester](s"Resource ${r.getId} already present, remove from harvested")
            //println(s"Resource ${r.getId} already present, remove from harvested")
            this.harvestedResources --= harvestedWithSameId
            harvestedWithSameId.foreach {
              rejected =>
              //println(s"Rejecting because already present: "+r)
              // rejected.clean
            }

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

        saveToAvailableResources(r)

        keepErrorsOn(r, verbose = true)(r.@->("added", this))
    }

    // Clean old resources
    //-----------------
    logFine[Harvester](s"========= CLEAN ------------")
    toclean.foreach {
      r =>
        // println(s"====== $r")
        keepErrorsOn(r, verbose = true)(r.@->("clean", this))
    }
    this.@->("removedResources", toclean)
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
            c.finishGather(dispatchResources = true)
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
        // Make a dispatch?
        //this.finishGather()
        true
      case false => false
    }

  }

  override def clean = {

    // Remove from parent harvester or global Harvest
    this.parentHarvesters.foreach {
      ph =>
        ph.removeChildHarvester(this)
    }
    /*
    this.parentHarvester match {
      case Some(p) =>
        p.removeChildHarvester(this)
      case None => 
        Harvest.removeHarvester(this)
    }*/

    super.clean
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
      this.saveToAvailableResources(r)
      // Make a dispatch??
    }

  }

  def gatherDirectAll(r: List[HarvestedResource]) = {
    this.synchronized {
      r.foreach(this.saveToAvailableResources(_))
      //this.gatherAll(r)
      //this.finishGather(true)
      // Make a dispatch??
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

      this.saveToAvailableResources(r)

      // Make a dispatch??
      //this.finishGather(true)
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
      case false if (this.availableResources.find(_.getId == r.getId).isDefined) => false
      case false =>
        r.root
        r.originalHarvester = Some(this)
        this.availableResources += r
        true
    }

  }

  // Resource Process, gathering and removing
  //----------------------

  def onRemovedResources(cl: List[HarvestedResource] => Unit) = {
    this.onWith("removedResources") {
      rl: List[HarvestedResource] => cl(rl)
    }
  }

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
      case r: Harvester =>
      case r: BrainRegion =>
      case r =>
        //-- Ignore errors here to ignore if rsource is already being processed
        ignoreErrors {
          r.runSingleTask("process") {
            this.keepErrorsOn(r, verbose = true) {

              HarvestedResource.moveToState(r, "processed")
            }
          }
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