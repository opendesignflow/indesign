package edu.kit.ipe.adl.indesign.module.maven

import edu.kit.ipe.adl.indesign.core.harvest.fs.FileSystemHarvester
import edu.kit.ipe.adl.indesign.core.harvest.fs.HarvestedFile
import java.io.File
import edu.kit.ipe.adl.indesign.core.module.ui.www.WWWViewHarvester

class MavenProjectHarvester extends FileSystemHarvester {
  
  
  
  
  this.addChildHarvester(new POMFileHarvester)
  
  override def doHarvest = {
    println(s"Running Do Harvest on Maven havester, resources: "+this.getResources)
    super.doHarvest
  }
  
  /**
   * If the Fiel is a folder, and has a POM,xml in it -> go
   */
  override def deliver(r:HarvestedFile) = {
    
    var pomFile = new File(r.path.toFile(),"pom.xml")
    
    (r.path.toFile().isDirectory(),pomFile.exists) match {
      case  (true,true) => 
        println(s"Delivered Maven Project ")
        gather(new MavenProjectResource(r.path))
       
        true
      case _ => false
    }
  }
  
}

object MavenProjectHarvester {
  
}