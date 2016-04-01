package edu.kit.ipe.adl.indesign.core.brain

import java.io.File
import org.apache.maven.project.ProjectBuilder
import org.apache.maven.project.DefaultProjectBuilder
import org.apache.maven.project.ProjectBuildingRequest
import org.apache.maven.project.DefaultProjectBuildingRequest
import org.codehaus.plexus.DefaultPlexusContainer
import org.apache.maven.project.ProjectBuildingException
import com.idyria.osi.aib.core.dependencies.maven.model.Project
import edu.kit.ipe.adl.indesign.core.brain.maven.MavenResolver
import com.idyria.osi.tea.compile.ClassDomain

/**
 * Loads a Brain Region present in another externaly compiled module
 */
class ExternalBrainRegion(val basePath: File, val regionClass: String) extends BrainRegion {

  //-- Find target classloader folder
  val classloaderPath = basePath match {

    // Maven
    //-----------

    //-- Target classes standard
    case p if (p.getAbsolutePath.endsWith("target/classes")) => p

    //-- Folder with pom, use std target/classes
    case p if (new File(p, "pom.xml").exists()) =>
      new File(p, List("target", "classes").mkString(File.separator))
  }

  //-- Set Classloaders
  var urlClassloader = new ClassDomain(Array(classloaderPath.toURI().toURL()), getClass.getClassLoader)

  //-- Load dependencies
  var project = Project(new File(basePath, "pom.xml").toURL())
  var dependenciesFiles = project.dependencies.dependency.filter { d => d.artifactId.toString() != "indesign-core" }.map {
    d =>
      List(MavenResolver.getArtifactPath(d.groupId, d.artifactId, d.version)) ::: MavenResolver.resolveDependencies(d.groupId, d.artifactId, d.version, "compile").map(dd => MavenResolver.getArtifactPath(dd.getArtifact))
    /*println(s"Lookign for dependency: "+d.artifactId)
      println(s"Location -> "+MavenResolver.getArtifactPath(d.groupId, d.artifactId, d.version))
      
      var deps = MavenResolver.resolveDependencies(d.groupId, d.artifactId, d.version,"compile")
      deps.foreach {
        dd => 
          println(s"Found --> $dd -> ${dd.getArtifact.getFile}")
      }*/

  }
  dependenciesFiles.flatten.foreach {
    dF =>
      println(s"Found Dep File: $dF")
      urlClassloader.addURL(dF.toURI().toURL())
  }

  //-- Load actual Region
  var wrappedRegion = try {
    urlClassloader.loadClass(regionClass).newInstance().asInstanceOf[BrainRegion]
  } catch {
    case e: Throwable => throw new RuntimeException(s"Could not create External Region $regionClass from classloader with CL path $classloaderPath ", e)
  }

  override def name = wrappedRegion.name
  

  this.onInit {
    Brain.moveToState(wrappedRegion, "init")
  }

  this.onLoad {
    Brain.moveToState(wrappedRegion, "load")
  }

}