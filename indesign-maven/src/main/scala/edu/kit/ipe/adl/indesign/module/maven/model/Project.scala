/**
 *
 */
package com.idyria.osi.aib.core.dependencies.maven.model

import com.idyria.osi.ooxoo.core.buffers.structural._
import com.idyria.osi.ooxoo.core.buffers.datatypes.XSDStringBuffer
import java.io.File
import com.idyria.osi.ooxoo.core.buffers.structural.io.sax.StAXIOBuffer
import java.io.FileInputStream
import org.eclipse.aether.artifact.Artifact

/**
 * @author rleys
 *
 */
@xelement(name = "project", ns = "http://maven.apache.org/POM/4.0.0")
class Project extends ElementBuffer {

  @xelement(ns = "http://maven.apache.org/POM/4.0.0")
  var artifactId: XSDStringBuffer = ""

  @xelement(ns = "http://maven.apache.org/POM/4.0.0")
  var groupId: XSDStringBuffer = ""

  @xelement(ns = "http://maven.apache.org/POM/4.0.0")
  var version: XSDStringBuffer = ""

  @xelement(ns = "http://maven.apache.org/POM/4.0.0")
  var packaging: XSDStringBuffer = null

  /**
   * <Dependencies>
   */
  @xelement(name = "dependencies")
  var dependencies: Dependencies = null

  // Utilities
  //-------------
  def is(art: Artifact): Boolean = {

    (this.groupId, this.artifactId, this.version) match {
      case (null, _, _) => false
      case (_, null, _) => false
      case (_, _, null) => false
      case (gid,aid,v) =>
        gid.toString==art.getGroupId && aid.toString==art.getArtifactId && v.toString == art.getVersion
    }

  }

}
object Project {

  def apply(url: java.net.URL) = {

    // Instanciate
    var res = new Project

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
  def apply(groupId: String, artifactId: String): Option[Project] = {

    // Look for possibilities
    var url = Thread.currentThread().getContextClassLoader().getResource(s"META-INF/maven/$groupId/$artifactId/pom.xml")
    var file = new File("pom.xml")

    // Resolve
    (url, file.exists()) match {
      case (null, false) => None

      // Use URL
      case (url, false) =>

        var project = new Project()
        project.appendBuffer(new StAXIOBuffer(url.openStream()))
        project.lastBuffer.streamIn

        Some(project)

      // Use File
      case (_, true) =>

        var project = new Project()
        project.appendBuffer(new StAXIOBuffer(new FileInputStream(file)))
        project.lastBuffer.streamIn

        Some(project)

    }

  }

}

@xelement(name = "dependencies")
class Dependencies extends ElementBuffer {

  /**
   * <Dependency>
   */
  @xelement(name = "dependency")
  var dependency = XList[Dependency] { new Dependency }

}

@xelement(name = "dependency")
class Dependency extends ElementBuffer {

  @xelement(name = "artifactId")
  var artifactId: XSDStringBuffer = ""

  @xelement(name = "groupId")
  var groupId: XSDStringBuffer = ""

  @xelement(name = "version")
  var version: XSDStringBuffer = ""

  /*var groupId: XSDStringBuffer
  var version: XSDStringBuffer
  var packaging : XSDStringBuffer*/

}
