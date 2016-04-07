package edu.kit.ipe.adl.indesign.core.brain

import java.io.File
import org.apache.maven.project.ProjectBuilder
import org.apache.maven.project.DefaultProjectBuilder
import org.apache.maven.project.ProjectBuildingRequest
import org.apache.maven.project.DefaultProjectBuildingRequest
import org.codehaus.plexus.DefaultPlexusContainer
import org.apache.maven.project.ProjectBuildingException
import com.idyria.osi.aib.core.dependencies.maven.model.Project
import com.idyria.osi.tea.compile.ClassDomain
import edu.kit.ipe.adl.indesign.core.module.artifactresolver.AetherResolver
import edu.kit.ipe.adl.indesign.module.maven.MavenProjectResource
import edu.kit.ipe.adl.indesign.core.module.IndesignModule

/**
 * Loads a Brain Region present in another externaly compiled module
 */
class ExternalBrainRegion(val basePath: File, val regionClass: String) extends MavenProjectResource(basePath.toPath()) with SingleBrainRegion  {

  
  //-- Load actual Region
  this.resetClassDomain
  var wrappedRegion = try {
  
  
    
    this.withClassLoader[BrainRegion[_]](this.classDomain) {
      var cl = this.classDomain.loadClass(regionClass)
      var inst = cl.newInstance()
      println(s"Class: "+cl.getClassLoader)
      println(s"Region of class: "+classOf[IndesignModule].isInstance(inst))
      inst.asInstanceOf[BrainRegion[_]]
    }
    
  } catch {
    case e: Throwable => 
      
      classDomain.getURLs.foreach {
        u => 
          println(s"D: "+u)
      }
      throw new RuntimeException(s"Could not create External Region $regionClass from classloader with CL path $classDomain ", e)
  }

  override def name = wrappedRegion.name
  

  this.onInit {
    Brain.moveToState(wrappedRegion, "init")
  }

  this.onLoad {
    Brain.moveToState(wrappedRegion, "load")
  }

}