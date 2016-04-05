package edu.kit.ipe.adl.indesign.module.scala

import edu.kit.ipe.adl.indesign.core.brain.BrainRegion
import edu.kit.ipe.adl.indesign.core.harvest.Harvest
import edu.kit.ipe.adl.indesign.module.maven.MavenProjectHarvester
import edu.kit.ipe.adl.indesign.core.module.IndesignModule
import edu.kit.ipe.adl.indesign.core.harvest.Harvester
import edu.kit.ipe.adl.indesign.core.harvest.fs.HarvestedFile

object ScalaModule extends IndesignModule {
  
  def load = {
    
  }
  
  this.onInit {
    
    // Register Harvesters
    //---
    Harvest.registerAutoHarvesterClass(classOf[MavenProjectHarvester], classOf[ScalaProjectHarvester])
    
  }
  
}

class ScalaSourceFileHarvester extends  Harvester[HarvestedFile,ScalaSourceFile] {
  
  
  
  
  
  def doHarvest = {
    
  }
  
  
  override def deliver(r:HarvestedFile) : Boolean = {

    r.path.toString.endsWith(".scala") match {
      case true  => 
        
        gather(new ScalaSourceFile(r.path))
        true
      case _ => 
        false
    }
  }
 
  
  
}