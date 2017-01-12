package org.odfi.indesign.module.maven.utils

import java.io.File
import org.odfi.indesign.module.maven.project

/**
 * This Utility Object is used to try to find current version based on starting folder pom.xml, like when launching from an IDE,
 * or using the packaged classpath
 */
object VersionLocator {

  def findVersion(groupId: String, artifactId: String): Option[Version] = {

    findProject(groupId, artifactId) match {
      case None => None
      case Some(pr) =>

        Some(Version(pr.getVersion))

    }
  }

  /**
   * Try to find a project definition from:
   *
   * Classloader META-INF/maven/groupId/artifactId/pom.xml
   * Local file start: pom.xml
   *
   */
  def findProject(groupId: String, artifactId: String): Option[project] = {

    // Look for possibilities
    var url = Thread.currentThread().getContextClassLoader().getResource(s"META-INF/maven/$groupId/$artifactId/pom.xml")
    var file = new File("pom.xml")

    // Resolve
    (url, file.exists()) match {
      case (null, false) => None

      // Use URL
      case (url, false) =>

        var p = project(url)

        Some(p)

      // Use File
      case (_, true) =>

        var p = project(file)

        Some(p)

    }

  }

}