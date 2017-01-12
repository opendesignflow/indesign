package org.odfi.indesign.module.scala

import org.odfi.indesign.core.brain.BrainRegion
import org.odfi.indesign.core.harvest.Harvest
import org.odfi.indesign.module.maven.MavenProjectHarvester
import org.odfi.indesign.core.module.IndesignModule
import org.odfi.indesign.core.harvest.Harvester
import org.odfi.indesign.core.harvest.fs.HarvestedFile
import org.odfi.indesign.core.module.ui.www.WWWViewHarvester
import org.odfi.indesign.module.scala.ui.ScalaOverview

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