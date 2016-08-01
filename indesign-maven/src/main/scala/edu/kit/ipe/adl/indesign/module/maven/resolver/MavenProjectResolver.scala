package edu.kit.ipe.adl.indesign.module.maven.resolver

import org.eclipse.aether.repository.WorkspaceReader
import org.eclipse.aether.repository.WorkspaceRepository
import java.io.File
import org.eclipse.aether.artifact.Artifact
import com.idyria.osi.tea.logging.TLogSource

import scala.collection.JavaConversions._
import edu.kit.ipe.adl.indesign.core.harvest.Harvest
import edu.kit.ipe.adl.indesign.module.maven.MavenProjectHarvester
import edu.kit.ipe.adl.indesign.module.maven.MavenProjectResource
import edu.kit.ipe.adl.indesign.module.maven.project
import edu.kit.ipe.adl.indesign.core.brain.Brain
import edu.kit.ipe.adl.indesign.core.brain.external.FolderOutputBrainRegion
import edu.kit.ipe.adl.indesign.core.brain.BrainRegion
import edu.kit.ipe.adl.indesign.module.maven.region.MavenExternalBrainRegion

trait MavenProjectIndesignWorkspaceReader {
  
}
object MavenProjectIndesignWorkspaceReader extends WorkspaceReader with TLogSource {

  val workspaceRepository: WorkspaceRepository = new WorkspaceRepository("indesign-maven")

  var allProjects: Option[List[(File, project)]] = None

 def resetAllProjects = {
    this.allProjects = None
  }
  
  def getAllProjects: List[(File, project)] = allProjects match {
    case Some(ap) => ap
    case None =>
      // Find Maven
      //----------------------
      var foundMaven = Harvest.collectResourcesOnHarvesters[MavenProjectHarvester, MavenProjectResource, MavenProjectResource] {
        case mp =>
          // println("Match")
          mp

      }
      var foundMavenRes = foundMaven.map { mp => (mp.pomFile, mp.projectModel) }

      // Find MavenRegions
      //----------------------
      var foundMavenRegions = Harvest.collectResourcesOnHarvesters[Brain, MavenExternalBrainRegion, MavenExternalBrainRegion] {
        case mp =>
          // println("Match")
          mp

      }
      var foundMavenRegionsRes = foundMavenRegions.map { mp => (mp.pomFile, mp.projectModel) }

      // Find Folder
      //---------------------
      /*logFine[MavenProjectIndesignWorkspaceReader]("Fodler Regions: "+Brain.getResourcesOfType[BrainRegion])
    Brain.onResources[FolderOutputBrainRegion] {
      case r => 
        logFine[MavenProjectIndesignWorkspaceReader]("Testing: "+r)
    }*/
      /*Harvest.collectResourcesOnHarvesters[Brain, FolderOutputBrainRegion, FolderOutputBrainRegion] {
      case r  =>
        logFine[MavenProjectIndesignWorkspaceReader]("Testing: "+r)
        r
    }*/
      var foundfolder = Harvest.collectResourcesOnHarvesters[Brain, FolderOutputBrainRegion, FolderOutputBrainRegion] {
        case r if (new File(r.basePath, "pom.xml").exists) =>
          // println("Match")
          r

      }
      var foundfolderRes = foundfolder.map {
        r =>
          logFine[MavenProjectIndesignWorkspaceReader]("Found: " + r)
          (new File(r.basePath, "pom.xml"), project(new File(r.basePath, "pom.xml").toURI().toURL()))
      }

      var res = foundMavenRes ::: foundfolderRes ::: foundMavenRegionsRes

      logFine[MavenProjectIndesignWorkspaceReader]("found all: " + res)
      this.allProjects = Some(res)
      res

  }

  /**
   * Looking for artifact
   */
  def findArtifact(artifact: Artifact): File = {

    //logFine[MavenProjectIndesignWorkspaceReader]("Looking in Other Maven Projects: " + artifact)

    var found = getAllProjects.collect {
      case (file, mp) if (mp != null && mp.is(artifact)) =>
        file

    }

    /*var found = Harvest.collectResourcesOnHarvesters[MavenProjectHarvester, MavenProjectResource, MavenProjectResource] {
      case mp if (mp.projectModel != null && mp.projectModel.is(artifact)) =>
        // println("Match")
        mp

    }*/
    //println("Found: " + found)
    var res = found.size match {
      case 0 =>
        null
      case _ =>

        //new File(found.head.path.toFile().getCanonicalFile, "target/classes")
        found.head
    }

    // println("Looking in Other Maven Projects: " + artifact+ "-> "+res)

    res

  }

  def findVersions(artifact: Artifact): java.util.List[String] = {

    var found = getAllProjects.collect {
      case (file, mp) if (mp != null && mp.is(artifact)) => mp
    }

    /*var found = Harvest.collectResourcesOnHarvesters[MavenProjectHarvester, MavenProjectResource, MavenProjectResource] {
      case mp if (mp.projectModel != null && mp.projectModel.is(artifact)) =>
        // println("Match")
        mp

    }*/

    found.map {
      mp => mp.version.toString()
    }.toList

    /*this.projectArtifacts.filter {
            case (file, art) => artifact.getGroupId == art.groupId.toString() &&
                artifact.getArtifactId == art.artifactId.toString()

        }.map {
            case (file, art) => art.version.toString()
        }.toList*/

    //List[String]()
  }

  def getRepository(): WorkspaceRepository = workspaceRepository

}