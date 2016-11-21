package edu.kit.ipe.adl.indesign.module.maven.ui

import edu.kit.ipe.adl.indesign.core.module.ui.www.IndesignUIView
import edu.kit.ipe.adl.indesign.core.brain.Brain
import edu.kit.ipe.adl.indesign.core.harvest.Harvest
import edu.kit.ipe.adl.indesign.module.maven.MavenProjectHarvester
import org.apache.maven.project.MavenProject
import edu.kit.ipe.adl.indesign.module.maven.MavenProjectResource
import edu.kit.ipe.adl.indesign.module.maven.region.MavenExternalBrainRegion
import edu.kit.ipe.adl.indesign.module.maven.MavenModule
import com.idyria.osi.tea.compile.ClassDomainContainer
import edu.kit.ipe.adl.indesign.core.brain.artifact.ArtifactRegion

class MavenOverview extends IndesignUIView {

  this.root

  this.viewContent {
    //Thread.currentThread().setContextClassLoader(MavenModule.getClass.getClassLoader)
    div {
      h1("Maven Projects Overview") {

      }

      // Harvest.onHarvesters[MavenProjectHarvester

      //var mavenRegions = Brain.getResourcesOfTypeClass(classOf[MavenExternalBrainRegion])
      var mavenRegions = Brain.getResourcesOfLazyType[MavenExternalBrainRegion]
      mavenRegions.size match {
        case 0 =>
        case _ =>
          "ui raised segment" :: div {

            importHTML(<a class="ui blue ribbon label">Maven External Regions</a>)

            var regionsContainer = mavenRegions.map {
              r => r.asInstanceOf[ArtifactRegion]
            }

            ul {
              regionsContainer.foreach {
                region =>
                  region.rebuildDependencies 
                  li {
                    textContent(region.toString)
                    ul {
                      region.classdomain.get.getURLs.foreach {
                        u =>
                          li {
                            textContent(u.toString)
                          }
                      }
                    }

                  }
              }

            }
          }

      }

      //-- Get Projects
      /*var projects = mavenRegions.map(r=>r.asInstanceOf[MavenProjectResource])
      Harvest.onHarvesters[MavenProjectHarvester] {
        case h => 
          projects = projects ++ h.getResourcesOfType[MavenProjectResource]
      }*/

      var projects = Harvest.collectResourcesOnHarvesters[MavenProjectHarvester, MavenProjectResource, MavenProjectResource] {
        case project => project
      }
      projects.size match {
        case 0 =>
          "ui info message" :: p("No Maven Projects Detected")
        case _ =>
          "ui raised segment" :: div {

            importHTML(<a class="ui blue ribbon label">Maven Projects</a>)
            p("""Please find here a summary of the Detected Maven Projects""")

            ul {

              projects.foreach {
                p =>
                  li {
                    div(textContent(p.projectModel.getArtifactId.toString))

                    ul {
                      //p.updateDependencies
                      p.getDependenciesURL.foreach {
                        url =>
                          li {
                            textContent(url.toString)
                          }
                      }
                    }
                  }
              }

            }

          }
      }

    }
  }

}