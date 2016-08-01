package edu.kit.ipe.adl.indesign.fastbuild

import edu.kit.ipe.adl.indesign.core.module.IndesignModule
import edu.kit.ipe.adl.indesign.core.harvest.Harvest
import edu.kit.ipe.adl.indesign.core.harvest.fs.FileSystemHarvester
import edu.kit.ipe.adl.indesign.module.scala.ScalaProjectHarvester
import edu.kit.ipe.adl.indesign.core.module.ui.www.WWWViewHarvester
import edu.kit.ipe.adl.indesign.fastbuild.ui.FastBuildOverview

object FBModule extends IndesignModule {
  
  this.onLoad {
    
    
    
    
  }
  
  this.onStart {
    println("Starting FB")
    Harvest.registerAutoHarvesterClass(classOf[FBProjectHarvester] -> classOf[FileSystemHarvester])
    Harvest.registerAutoHarvesterClass(classOf[ScalaProjectHarvester] -> classOf[FBProjectHarvester])
  
    //-- Deliver view
    Harvest.deliverToHarvesters[WWWViewHarvester](new FastBuildOverview) 
    
  }
  
}