package edu.kit.ipe.adl.indesign.module.maven

import edu.kit.ipe.adl.indesign.core.brain.BrainRegion
import edu.kit.ipe.adl.indesign.core.harvest.Harvest
import edu.kit.ipe.adl.indesign.core.harvest.Harvester
import edu.kit.ipe.adl.indesign.core.harvest.fs.HarvestedFile
import edu.kit.ipe.adl.indesign.core.module.IndesignModule
import edu.kit.ipe.adl.indesign.core.brain.SingleBrainRegion
import edu.kit.ipe.adl.indesign.core.module.IndesignBaseModule
import org.codehaus.plexus.DefaultPlexusContainer

 
object MavenModule extends IndesignBaseModule[MavenProjectResource] {
  
  
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
  
  def load = {
    //FileSystemHarvester.addChildHarvester(new POMFileHarvester)
  }
}

