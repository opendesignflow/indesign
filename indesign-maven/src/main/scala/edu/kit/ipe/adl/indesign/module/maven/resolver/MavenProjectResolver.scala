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

class MavenProjectIndesignWorkspaceReader extends WorkspaceReader with TLogSource {

  val workspaceRepository: WorkspaceRepository = new WorkspaceRepository("indesign-maven")

  /**
   * Looking for artifact
   */
  def findArtifact(artifact: Artifact): File = {

    logFine[MavenProjectIndesignWorkspaceReader]("Looking in Other Maven Projects: " + artifact)

    var found = Harvest.collectResourcesOnHarvesters[MavenProjectHarvester, MavenProjectResource, MavenProjectResource] {
      case mp if (mp.projectModel != null && mp.projectModel.is(artifact)) =>
        // println("Match")
        mp

    }
    //println("Found: " + found)
    found.size match {
      case 0 =>
        null
      case _ =>

        //new File(found.head.path.toFile().getCanonicalFile, "target/classes")
        found.head.pomFile
    }

  }

  def findVersions(artifact: Artifact): java.util.List[String] = {

    var found = Harvest.collectResourcesOnHarvesters[MavenProjectHarvester, MavenProjectResource, MavenProjectResource] {
      case mp if (mp.projectModel != null && mp.projectModel.is(artifact)) =>
        // println("Match")
        mp

    }

    found.map {
      mp => mp.projectModel.version.toString()
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