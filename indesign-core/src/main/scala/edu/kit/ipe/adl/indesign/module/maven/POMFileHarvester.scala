package edu.kit.ipe.adl.indesign.module.maven

import java.nio.file.Path

import edu.kit.ipe.adl.indesign.core.harvest.Harvester
import edu.kit.ipe.adl.indesign.core.harvest.fs.HarvestedFile

class POMFileHarvester extends Harvester[HarvestedFile,POMFileResource] {
  
  def doHarvest = {
    
  }
  
  /**
   * Reacts on pom.xml file
   */
  override def deliver (r: HarvestedFile) = {
    
    //if (r.
    r.path.endsWith("pom.xml") match {
      case true => 
        println(s"Delivered POM FILE: "+r.path.toUri())
        gather(new POMFileResource(r.path))
        true
      case false =>
        false
    }
    
  }
}

class POMFileResource(p:Path) extends HarvestedFile(p) {
  
}