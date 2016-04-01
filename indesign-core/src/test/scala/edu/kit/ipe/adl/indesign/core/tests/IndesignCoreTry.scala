package edu.kit.ipe.adl.indesign.core.tests

import java.io.File
import edu.kit.ipe.adl.indesign.core.brain.Brain
import edu.kit.ipe.adl.indesign.core.harvest.Harvest
import edu.kit.ipe.adl.indesign.core.harvest.fs.FileSystemHarvester
import edu.kit.ipe.adl.indesign.core.module.ui.www.IndesignWWWUIModule
import edu.kit.ipe.adl.indesign.module.maven.MavenModule
import edu.kit.ipe.adl.indesign.module.maven.POMFileHarvester
import edu.kit.ipe.adl.indesign.module.odfi.rfg.RFGModule
import edu.kit.ipe.adl.indesign.module.tcl.TCLModule
import edu.kit.ipe.adl.indesign.module.tcl.TCLFileHarvester
import edu.kit.ipe.adl.indesign.core.brain.ExternalBrainRegion

object IndesignCoreTry extends App {

  /*var tu = new URI("/resources")
  println(s"URI: "+tu.toString())
  
  sys.exit()*/

  
  // Load Modules
  //-----------------
  Brain += (Harvest,MavenModule,TCLModule,RFGModule,IndesignWWWUIModule)

  Brain += (new ExternalBrainRegion(new File("/home/rleys/git/adl/instruments/scala-instruments"),"kit.ipe.adl.instruments.nivisa.VISAModule"))
  
  Brain.init
  /*MavenModule.load
  TCLModule.load 
  RFGModule.load
  IndesignWWWUIModule.load
  
  Harvest.run*/

    
  // Create harvest
  //-------------------
  var fsh = new FileSystemHarvester(new File("src/test/resources/testFS").toPath())
  Harvest.addHarvester(fsh)
  fsh.addChildHarvester(new POMFileHarvester)
  fsh.addChildHarvester(new TCLFileHarvester)
  
}