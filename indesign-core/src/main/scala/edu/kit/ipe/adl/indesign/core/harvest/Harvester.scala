package edu.kit.ipe.adl.indesign.core.harvest

import java.nio.file.Path
import edu.kit.ipe.adl.indesign.core.brain.LFCDefinition
import edu.kit.ipe.adl.indesign.core.brain.LFCSupport
import edu.kit.ipe.adl.indesign.core.brain.errors.ErrorSupport

/**
 * A harvester will look for resources, and call upon its child harvesters to match them
 *
 */
trait Harvester[PT <: HarvestedResource, RT <: HarvestedResource] extends LFCSupport with ErrorSupport{

  var lastRun: Long = 0

  // Child Stuff
  //----------------------------
  var childHarvesters = List[Harvester[RT, _]]()
  var parentHarvester: Option[Harvester[_, PT]] = None
  def addChildHarvester(h: Harvester[RT, _]) = {
    this.childHarvesters = this.childHarvesters :+ h.asInstanceOf[Harvester[RT, _]]
    h.parentHarvester = Some(this)
  }

  def hierarchyName(sep: String = ".", withoutSelf: Boolean = false) = {

    // Get Parent Line
    var parents = withoutSelf match {
      case true => List[Harvester[_, _]]()
      case false => List[Harvester[_, _]](this)
    }
    var currentParent: Option[Harvester[_, _]] = this.parentHarvester
    while (currentParent != None) {
      parents = parents :+ currentParent.get
      currentParent = currentParent.get.parentHarvester
    }

    // Return String
    parents.reverse.map(h => h.getClass.getSimpleName.replace("$", "") + "-" + h.hashCode()).mkString(sep)

  }

  // harvest 
  //------------------
  var availableResources = scala.collection.mutable.LinkedHashSet[RT]()
  var harvestedResources = scala.collection.mutable.LinkedHashSet[RT]()

  /**
   * After a Harvest run, clean available Resources which were not present in the gather outcome
   */
  var autoCleanResources = true
  
  /**
   * Store a resource in local gathered resources for later processing
   */
  def gather(r: RT) = {
    this.harvestedResources += r
  }

  /**
   * Return available resources list
   */
  def getResources = this.availableResources.toList

  /**
   * Harvest Gather ressources, wich are added to gathered resources
   * implementations should override this method, then call super.harvest at the end
   */
  def harvest = {
    var oldcl = Thread.currentThread().getContextClassLoader
    try {
      this.lastRun = System.currentTimeMillis()
      Thread.currentThread().setContextClassLoader(this.getClass.getClassLoader)
      doHarvest
    } catch {
      case e: Throwable =>
        e.printStackTrace()
        throw e
    } finally {
      Thread.currentThread().setContextClassLoader(oldcl)
    }
    finishHarvest
  }

  /**
   * To be overriden by implementations
   */
  def doHarvest

  /**
   * Finish harvest by reloading harvested and gathered resources
   */
  def finishHarvest: Unit = {

    // Go through available resources
    //  -> Remove the ones which are not in the gathered, if autoclean is set to true
    //  -> Remove from gathered the one already available
    //  -> Add Remaining gathered
    this.availableResources.toList.foreach {
      r =>

        this.harvestedResources.find { hr => hr.getId == r.getId } match {
          // Remove non gathered  
          case None if(autoCleanResources) =>
            this.availableResources -= r

         // Keep resource if autoclean is not set
         case None if(!autoCleanResources)=> 
            
          // Keep, then remove from gathered
          case Some(matchingHarvested) =>
            this.harvestedResources -= matchingHarvested

        }
    }
    this.harvestedResources.foreach { r => this.availableResources += r }
    this.harvestedResources.clear()

    // Deliver resources to child harvesters, and run a finish harvect on them too
    this.childHarvesters.foreach {
      ch =>
        this.availableResources.foreach {
          r =>
            ch.deliver(r)
        }
        ch.finishHarvest
    }

  }

  /**
   * If a parent harvester runs, it delivers resources to its child harvesters
   * Deliver is called by parent
   */
  def deliver(r: PT): Boolean = {
    false
  }

  def deliverDirect(r:PT): Boolean = {
    this.deliver(r) match {
      case true => 
        this.finishHarvest
        true
      case false => false
    }
    
  }
  
}

object Harvester extends LFCDefinition {

  this.defineState("init")
  this.defineState("clean")

}