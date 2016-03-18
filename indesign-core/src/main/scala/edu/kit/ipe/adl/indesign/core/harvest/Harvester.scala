package edu.kit.ipe.adl.indesign.core.harvest

import java.nio.file.Path

/**
 * A harvester will look for resources, and call upon its child harvesters to match them
 * 
 */
trait Harvester[RT <: HarvestedResource] {
  
  
  
  
  
  var lastRun : Long = 0
  
  var harvestedResources = scala.collection.mutable.LinkedHashSet[RT]()
  
  // Child Stuff
  //----------------------------
  var childHarvesters = List[Harvester[RT]]() 
  var parentHarvester : Option[Harvester[RT]] = None
  def addChildHarvester(h:Harvester[RT]) = {
    this.childHarvesters = this.childHarvesters :+ h
    h.parentHarvester = Some(this)
  }
  
  def hierarchyName(sep:String = ".",withoutSelf:Boolean=false) = {
    
    // Get Parent Line
    var parents = withoutSelf match {
      case true =>  List[Harvester[RT]]()
      case false => List[Harvester[RT]](this)
    }
    var currentParent  = this.parentHarvester
    while (currentParent!=None) {
      parents = parents :+ currentParent.get 
      currentParent = currentParent.get.parentHarvester
    }
    
    // Return String
    parents.reverse.map(h => h.getClass.getSimpleName.replace("$","")+"-"+h.hashCode()).mkString(sep)
      
  }
  
  
  def harvest
  
  /**
   * If a parent harvester runs, it delivers resources to its child harvesters
   */
  def deliver (r: RT) : Boolean = {
    false
  }
  
  // Resource Harvesting
  //----------------
  
  /**
   * Store a resource in local gathered resources for later processing
   */
  def gather(r:RT) = this.harvestedResources += r
}