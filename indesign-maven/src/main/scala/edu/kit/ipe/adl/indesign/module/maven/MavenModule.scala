package edu.kit.ipe.adl.indesign.module.maven

import edu.kit.ipe.adl.indesign.core.brain.BrainRegion
import edu.kit.ipe.adl.indesign.core.harvest.Harvest
import edu.kit.ipe.adl.indesign.core.harvest.Harvester
import edu.kit.ipe.adl.indesign.core.harvest.fs.HarvestedFile
import edu.kit.ipe.adl.indesign.core.module.IndesignModule
import edu.kit.ipe.adl.indesign.core.harvest.fs.FileSystemHarvester
import edu.kit.ipe.adl.indesign.core.brain.ExternalBrainRegion
import edu.kit.ipe.adl.indesign.core.module.ui.www.WWWViewHarvester
import edu.kit.ipe.adl.indesign.module.maven.ui.MavenOverview
import edu.kit.ipe.adl.indesign.core.brain.Brain
import edu.kit.ipe.adl.indesign.core.artifactresolver.AetherResolver
import edu.kit.ipe.adl.indesign.module.maven.resolver.MavenProjectIndesignWorkspaceReader
import com.idyria.osi.tea.logging.TLog
import edu.kit.ipe.adl.indesign.module.maven.region.MavenExternalBrainRegion
import edu.kit.ipe.adl.indesign.module.maven.region.MavenExternalBrainRegionBuilder

object MavenModule extends IndesignModule {

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

  //TLog.setLevel(classOf[MavenProjectIndesignWorkspaceReader], TLog.Level.FULL)

  this.onLoad {

    //-- Register Maven Region Builder
    ExternalBrainRegion.addBuilder(new MavenExternalBrainRegionBuilder)

    println("Loading Maven : " + MavenModule.getClass.getClassLoader)
    Harvest.registerAutoHarvesterClass(classOf[FileSystemHarvester], classOf[MavenProjectHarvester])

    // MavenProjectIndesignWorkspaceReader.resetAllProjects
    //AetherResolver.session.setWorkspaceReader(MavenProjectIndesignWorkspaceReader)

    //println("Loading Maven WWW View---------: "+WWWViewHarvester.hashCode())

    // WWWViewHarvester.deliverDirect(new MavenOverview)

  }

  this.onStart {
    println("Start on Maven: " + Harvest.getHarvesters[WWWViewHarvester])
    Harvest.getHarvesters[WWWViewHarvester] match {
      case Some(h) =>
        h.last.deliverDirect(new MavenOverview)
      case _ =>
    }
  }

  this.onShutdown {
    println("Shutting down Maven module: " + Brain.getResources)

    
    //-- Register Maven Region Builder
    ExternalBrainRegion.addBuilder(new MavenExternalBrainRegionBuilder)

    
    // Find All Maven Regions in Brain and remove them
    //------------
    /*Brain.getResourcesOfLazyType[MavenExternalBrainRegion].drop(1).foreach {
      case r if (r.isTainted) =>
        println("Cleaning from brain: " + r.isTainted)
        Brain.cleanResource(r)
      case _ =>
    }*/

  }

  // Ressource Keep
  //-------------------
  this.onKept {
    case h =>
      println("Maven Module kept: " + Harvest.getHarvesters[WWWViewHarvester])
      Harvest.getHarvesters[WWWViewHarvester] match {
        case Some(h) =>
          h.last.deliverDirect(new MavenOverview)
        case _ =>
      }
  }

  def load = {
    //FileSystemHarvester.addChildHarvester(new POMFileHarvester)
    //println("Loading Maven")
    //Harvest.registerAutoHarvesterClass(classOf[FileSystemHarvester], classOf[MavenProjectHarvester])
  }
}

