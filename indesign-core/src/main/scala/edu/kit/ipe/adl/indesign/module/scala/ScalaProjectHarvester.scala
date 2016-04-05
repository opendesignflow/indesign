package edu.kit.ipe.adl.indesign.module.scala

import edu.kit.ipe.adl.indesign.core.harvest.Harvester
import edu.kit.ipe.adl.indesign.core.harvest.fs.HarvestedFile
import edu.kit.ipe.adl.indesign.module.maven.MavenProjectHarvester

class ScalaProjectHarvester extends Harvester[HarvestedFile, ScalaSourceFile] {

  this.addChildHarvester(new ScalaAppHarvester)
  
  def doHarvest = {

    parentHarvester match {
      case Some( mh:MavenProjectHarvester) =>
        
      case _ => 
    }
    
  }

  override def deliver(r: HarvestedFile): Boolean = {

    r.path.toString.endsWith(".scala") match {
      case true =>

        gather(new ScalaSourceFile(r.path))
        true
      case _ =>
        false
    }
  }

}