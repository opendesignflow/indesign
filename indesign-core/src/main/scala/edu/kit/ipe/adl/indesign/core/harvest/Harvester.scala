package edu.kit.ipe.adl.indesign.core.harvest

import java.nio.file.Path

/**
 * A harvester will look for resources, and call upon its child harvesters to match them
 * 
 */
trait Harvester[RT <: HarvestedResource] {
  
  var lastRun : Long = 0
  
  var harvestedResources = scala.collection.mutable.LinkedHashSet[RT]()
  
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