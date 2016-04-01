package edu.kit.ipe.adl.indesign.module.tcl

import edu.kit.ipe.adl.indesign.core.harvest.Harvester
import edu.kit.ipe.adl.indesign.core.harvest.fs.HarvestedFile
import java.nio.file.Path

class TCLFileHarvester extends Harvester[HarvestedFile,TCLFile] {

  /**
   * Deliver collected TCL file to children harvesters
   */
  def doHarvest = {

   

  }

  /**
   * Reacts on pom.xml file
   */
  override def deliver(r: HarvestedFile) = {

    //println(s"Testign TCL file: "+r.path)
    
    //if (r.
    r.path.toUri().toString().endsWith(".tcl") match {
      case true =>
        println(s"Delivered TCL FILE: " + r.path.toUri())
        this.gather(new TCLFile(r.path))
        true
      case false =>
        false
    }

  }
}

object TCLFileHarvester {

  implicit def pathToResource(p: Path) = new HarvestedFile(p)
}