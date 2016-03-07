package edu.kit.ipe.adl.indesign.core.tests

import edu.kit.ipe.adl.indesign.core.harvest.Harvest
import edu.kit.ipe.adl.indesign.core.harvest.fs.FileSystemHarvester
import java.io.File
import edu.kit.ipe.adl.indesign.module.maven.MavenModule
import edu.kit.ipe.adl.indesign.module.tcl.TCLModule
import edu.kit.ipe.adl.indesign.module.odfi.rfg.RFGModule

object IndesignCoreTry extends App {

  // Create harvest
  //-------------------
  var fs = Harvest.addHarvester(new FileSystemHarvester(new File("src/test/resources/testFS").toPath()))
  
  // Load Modules
  //-----------------
  MavenModule.load
  TCLModule.load 
  RFGModule.load
  
  Harvest.run

}