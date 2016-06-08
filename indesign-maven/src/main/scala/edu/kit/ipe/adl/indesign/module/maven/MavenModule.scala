package edu.kit.ipe.adl.indesign.module.maven

import edu.kit.ipe.adl.indesign.core.brain.BrainRegion
import edu.kit.ipe.adl.indesign.core.harvest.Harvest
import edu.kit.ipe.adl.indesign.core.harvest.Harvester
import edu.kit.ipe.adl.indesign.core.harvest.fs.HarvestedFile
import edu.kit.ipe.adl.indesign.core.module.IndesignModule
import edu.kit.ipe.adl.indesign.core.harvest.fs.FileSystemHarvester
import edu.kit.ipe.adl.indesign.core.brain.ExternalBrainRegion
import edu.kit.ipe.adl.indesign.core.brain.MavenExternalBrainRegionBuilder
import edu.kit.ipe.adl.indesign.core.module.ui.www.WWWViewHarvester
import edu.kit.ipe.adl.indesign.module.maven.ui.MavenOverview
import edu.kit.ipe.adl.indesign.core.brain.Brain
import edu.kit.ipe.adl.indesign.core.brain.MavenExternalBrainRegion
import edu.kit.ipe.adl.indesign.core.artifactresolver.AetherResolver
import edu.kit.ipe.adl.indesign.module.maven.resolver.MavenProjectIndesignWorkspaceReader
import com.idyria.osi.tea.logging.TLog

 
object MavenModule extends IndesignModule {
  
  //-- Register Maven Region Builder
  ExternalBrainRegion.addBuilder(new MavenExternalBrainRegionBuilder)
  
  /**
   * Harvester of maven proejcts to start building
   */
  /*object ProjectsHarvester extends Harvester[HarvestedFile,MavenProjectResource] {
    
    
    def doHarvest = {
      
    }
    
    override def deliver(f: HarvestedFile) = {
      
      f match {
        case mf:MavenProjectResource => 
          gather(mf)
          println(s"Gathering Subregion")
          
          mf.onAdded {
            case h if (h==ProjectsHarvester.this) => 
              println("Added to harvester: "+h.hashCode())
              MavenModule.this.addSubRegion(new MavenProjectBuilder(mf))
            case _ => 
              println(s"Added to another Harvester")
          }
          
          true
        case _ => false
      }
      
    }
    
  }
  
  
  Harvest.registerAutoHarvesterObject(classOf[MavenProjectHarvester], ProjectsHarvester)*/
  
     TLog.setLevel(classOf[MavenProjectIndesignWorkspaceReader], TLog.Level.FULL)
  
  this.onLoad {
    println("Loading Maven : "+this.getClass.getClassLoader)
    Harvest.registerAutoHarvesterClass(classOf[FileSystemHarvester], classOf[MavenProjectHarvester])
    WWWViewHarvester.deliverDirect(new MavenOverview)
    
  }
  
  
  this.onShutdown {
    println("Shutting down Maven module: "+Brain.getResources)
 
    // Find All Maven Regions in Brain and remove them
    Brain.getResourcesOfLazyType[MavenExternalBrainRegion].drop(1).foreach {
      r => 
        println("Cleaning from brain: "+r)
        Brain.cleanResource(r)
    }
    
  }
  
  def load = {
    //FileSystemHarvester.addChildHarvester(new POMFileHarvester)
    //println("Loading Maven")
    //Harvest.registerAutoHarvesterClass(classOf[FileSystemHarvester], classOf[MavenProjectHarvester])
  }
}

