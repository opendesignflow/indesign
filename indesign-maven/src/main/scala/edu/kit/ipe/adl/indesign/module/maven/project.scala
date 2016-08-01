package edu.kit.ipe.adl.indesign.module.maven

import com.idyria.osi.ooxoo.core.buffers.structural.xelement
import org.eclipse.aether.artifact.Artifact
import java.io.File
import com.idyria.osi.ooxoo.core.buffers.structural.io.sax.StAXIOBuffer
import java.io.FileInputStream

@xelement(name = "project")
class project extends projectTrait {

  def getArtifactId: String = {
    this.artifactId match {
      case null =>
       null
      case aid =>
        aid.toString
    }
  }
  def getGroupId: String = {
    this.groupId match {
      case null if (__parent != null && parent.groupId != null) =>
        parent.groupId.toString
      case null =>null
      case gid =>
        gid.toString
    }
  }
  
  def getVersion: String = {
    this.version match {
      case null if (__parent != null && parent.version != null) =>
        parent.version.toString
      case null =>null
      case v =>
        v.toString
    }
  }

  // Utilities
  //-------------
  def is(art: Artifact): Boolean = {

    (this.getGroupId, this.getArtifactId, this.getVersion) match {
      case (null, _, _) => false
      case (_, null, _) => false
      case (_, _, null) => false
      case (gid, aid, v) =>
        gid.toString == art.getGroupId && aid.toString == art.getArtifactId && v.toString == art.getVersion
    }

  }
}

object project {

  def apply(url: java.net.URL) = {

    // Instanciate
    var res = new project

    // Set Stax Parser and streamIn
    var io = com.idyria.osi.ooxoo.core.buffers.structural.io.sax.StAXIOBuffer(url)
    res.appendBuffer(io)
    io.streamIn

    // Return
    res

  }

  /**
   * Try to find a project definition from:
   *
   * Classloader META-INF/maven/groupId/artifactId/pom.xml
   * Local file start: pom.xml
   *
   */
  def apply(groupId: String, artifactId: String): Option[project] = {

    // Look for possibilities
    var url = Thread.currentThread().getContextClassLoader().getResource(s"META-INF/maven/$groupId/$artifactId/pom.xml")
    var file = new File("pom.xml")

    // Resolve
    (url, file.exists()) match {
      case (null, false) => None

      // Use URL
      case (url, false) =>

        var project = new project()
        project.appendBuffer(new StAXIOBuffer(url.openStream()))
        project.lastBuffer.streamIn

        Some(project)

      // Use File
      case (_, true) =>

        var project = new project()
        project.appendBuffer(new StAXIOBuffer(new FileInputStream(file)))
        project.lastBuffer.streamIn

        Some(project)

    }

  }

}