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
import edu.kit.ipe.adl.indesign.module.maven.MavenProjectHarvester
import edu.kit.ipe.adl.indesign.module.scala.ScalaModule
import edu.kit.ipe.adl.indesign.core.module.ui.www.WWWViewHarvester
import edu.kit.ipe.adl.indesign.module.maven.MavenProjectResource

object IndesignCoreTry extends App {

  /*var tu = new URI("/resources")
  println(s"URI: "+tu.toString())
  
  sys.exit()*/

  //var r = new MavenProjectResource(new File("src/test/resources/testFS/maven-app-single").toPath())
  
 // r.parseModel
  
  
  
  
  //sys.exit()
  
  
  // Load ModulesIndesignWWWUIModule   // IndesignWWWUIModule
  //-----------------
  Brain += (
        Harvest,MavenModule,TCLModule,RFGModule,ScalaModule)

 // Brain += (new ExternalBrainRegion(new File("/home/rleys/git/adl/instruments/scala-instruments"),"kit.ipe.adl.instruments.nivisa.VISAModule"))
  
  Brain.init
  /*MavenModule.load
  TCLModule.load 
  RFGModule.load
  IndesignWWWUIModule.load
  
  Harvest.run*/

    
  // Create harvest
  //-------------------
  var fsh  = new FileSystemHarvester
  
  fsh.addPath(new File("src/test/resources/testFS").toPath())

  Harvest.addHarvester(fsh)
  fsh.addChildHarvester(new MavenProjectHarvester)
  //fsh.addChildHarvester(new POMFileHarvester)
  //fsh.addChildHarvester(new TCLFileHarvester)
  
  Harvest.run
  
  println(s"WWWVIew content now: "+WWWViewHarvester.getResources.size)
  
  
  Brain.onAllRegions { 
    r =>  
      
      println(s"Found Region: "+r.name+" -> childrend: "+r.subRegions.size)
  
  }
  
  
  
  
  
  
  Console.readLine()
  println(s"Stopping")
  
}