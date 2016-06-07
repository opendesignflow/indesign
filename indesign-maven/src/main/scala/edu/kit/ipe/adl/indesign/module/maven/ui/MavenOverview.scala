package edu.kit.ipe.adl.indesign.module.maven.ui

import edu.kit.ipe.adl.indesign.core.module.ui.www.IndesignUIView
import edu.kit.ipe.adl.indesign.core.brain.Brain
import edu.kit.ipe.adl.indesign.core.brain.MavenExternalBrainRegion
import edu.kit.ipe.adl.indesign.core.harvest.Harvest
import edu.kit.ipe.adl.indesign.module.maven.MavenProjectHarvester
import org.apache.maven.project.MavenProject
import edu.kit.ipe.adl.indesign.module.maven.MavenProjectResource

class MavenOverview extends IndesignUIView {
  
  this.root
  
  this.viewContent {

    div {
      h1("Maven Projects Overview 32") {

      }
      
     // Harvest.onHarvesters[MavenProjectHarvester
      
      var mavenRegions = Brain.getResourcesOfType[MavenExternalBrainRegion]
      mavenRegions.size match {
        case 0 => 
        case _ => 
          h2("Maven External Regions") {
            
          }
      }
      
      //-- Get Projects
      var projects = mavenRegions.map(r=>r.asInstanceOf[MavenProjectResource])
      Harvest.onHarvesters[MavenProjectHarvester] {
        case h => 
          projects = projects ++ h.getResourcesOfType[MavenProjectResource]
      }
      
    }
  }
  
  
  
}