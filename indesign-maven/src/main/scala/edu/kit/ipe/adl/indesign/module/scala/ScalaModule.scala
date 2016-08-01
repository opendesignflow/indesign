package edu.kit.ipe.adl.indesign.module.scala

import edu.kit.ipe.adl.indesign.core.brain.BrainRegion
import edu.kit.ipe.adl.indesign.core.harvest.Harvest
import edu.kit.ipe.adl.indesign.module.maven.MavenProjectHarvester
import edu.kit.ipe.adl.indesign.core.module.IndesignModule
import edu.kit.ipe.adl.indesign.core.harvest.Harvester
import edu.kit.ipe.adl.indesign.core.harvest.fs.HarvestedFile
import edu.kit.ipe.adl.indesign.core.module.ui.www.WWWViewHarvester
import edu.kit.ipe.adl.indesign.module.scala.ui.ScalaOverview

object ScalaModule extends IndesignModule {

  this.onLoad {

    println("Loading Scala : " + this.getClass.getClassLoader)
  }
  
  this.onInit {
    println("Init Scala : " + this.getClass.getClassLoader)
    
    // Add UI
    Harvest.getHarvesters[WWWViewHarvester] match {
      case Some(h) => 
        h.last.deliverDirect(new ScalaOverview)
      case _ => 
    }

    
    // Register Harvesters
    //---
    Harvest.registerAutoHarvesterClass(classOf[MavenProjectHarvester], classOf[ScalaProjectHarvester])

  }

}

class ScalaSourceFileHarvester extends Harvester {

  this.onDeliverFor[HarvestedFile] {

    case r =>

      r.path.toString.endsWith(".scala") match {
        case true =>

          gather(new ScalaSourceFile(r.path))
          true
        case _ =>
          false
      }
  }

}