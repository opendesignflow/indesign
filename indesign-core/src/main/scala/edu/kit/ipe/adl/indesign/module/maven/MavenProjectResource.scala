package edu.kit.ipe.adl.indesign.module.maven

import java.io.File
import java.nio.file.Path
import com.idyria.osi.tea.compile.ClassDomain
import edu.kit.ipe.adl.indesign.core.harvest.fs.HarvestedFile
import edu.kit.ipe.adl.indesign.core.module.ui.www.WWWViewHarvester
import edu.kit.ipe.adl.indesign.core.brain.SingleBrainRegion
import edu.kit.ipe.adl.indesign.core.heart.HeartTask
import edu.kit.ipe.adl.indesign.core.heart.DefaultHeartTask

class MavenProjectResource(p: Path) extends HarvestedFile(p) with SingleBrainRegion {

  //-- Get Pom File 
  var pomFile = new File(p.toFile(), "pom.xml")

  this.onAdded {
    case h if (h.isInstanceOf[MavenProjectHarvester]) =>
      
      WWWViewHarvester.deliverDirect(view)
      println(s"Maven Project resource added to harvster")
      MavenModule.addSubRegion(this)

    //-- 

    case _ =>
  }
  
  
  this.onProcess {
    println(s"Scheduling Maven Project for building")
  }

  //-- WWW VIew
  var view = new MavenProjectWWWView(this)

  //-- Classdomain
  var classDomain = new ClassDomain(Thread.currentThread().getContextClassLoader)

  //-- Maven Model
  var projectModel = project(pomFile.toURI().toURL())

  /*class MavenProjectBuilder(val project: MavenProjectResource) extends SingleBrainRegion {

    override def name = project.getId

    // Clean
    //-----------------
    project.onCleaned {
      case h if (h.isInstanceOf[MavenProjectHarvester]) =>
        this.kill
      case _ =>
    }

  }*/

  // Builder Task
  //-----------------------
  class BuilderTask extends DefaultHeartTask {
    
    def doTask = {
      
      
      
    }
    
  }
  
  
}