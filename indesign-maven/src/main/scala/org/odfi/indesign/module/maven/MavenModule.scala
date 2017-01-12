package org.odfi.indesign.module.maven

import org.odfi.indesign.core.brain.BrainRegion
import org.odfi.indesign.core.harvest.Harvest
import org.odfi.indesign.core.harvest.Harvester
import org.odfi.indesign.core.harvest.fs.HarvestedFile
import org.odfi.indesign.core.module.IndesignModule
import org.odfi.indesign.core.harvest.fs.FileSystemHarvester
import org.odfi.indesign.core.brain.ExternalBrainRegion
import org.odfi.indesign.core.module.ui.www.WWWViewHarvester
import org.odfi.indesign.module.maven.ui.MavenOverview
import org.odfi.indesign.core.brain.Brain
import org.odfi.indesign.core.artifactresolver.AetherResolver
import org.odfi.indesign.module.maven.resolver.MavenProjectIndesignWorkspaceReader
import com.idyria.osi.tea.logging.TLog
import org.odfi.indesign.module.maven.region.MavenExternalBrainRegion
import org.odfi.indesign.module.maven.region.MavenExternalBrainRegionBuilder

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

