package edu.kit.ipe.adl.indesign.module.maven

import edu.kit.ipe.adl.indesign.core.harvest.Harvester
import edu.kit.ipe.adl.indesign.core.harvest.fs.HarvestedFile

class POMFileHarvester extends Harvester[HarvestedFile] {
  
  def harvest = {
    
  }
  
  /**
   * Reacts on pom.xml file
   */
  override def deliver (r: HarvestedFile) = {
    
    //if (r.
    r.path.endsWith("pom.xml") match {
      case true => 
        println(s"Delivered POM FILE: "+r.path.toUri())
      case false => 
    }
    
  }
}