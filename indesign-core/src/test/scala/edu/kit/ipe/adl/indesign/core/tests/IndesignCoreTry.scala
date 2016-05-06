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
import edu.kit.ipe.adl.indesign.module.scala.ScalaAppHarvester
import edu.kit.ipe.adl.indesign.module.scala.ScalaSourceFile
import edu.kit.ipe.adl.indesign.module.scala.ScalaAppSourceFile
import edu.kit.ipe.adl.indesign.core.module.eclipse.EclipseModule
import edu.kit.ipe.adl.indesign.core.module.git.GitModule

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
        Harvest,MavenModule,TCLModule,RFGModule,ScalaModule,EclipseModule,IndesignWWWUIModule,GitModule)

  
  
  Brain.init
  /*MavenModule.load
  TCLModule.load 
  RFGModule.load
  IndesignWWWUIModule.load
  
  Harvest.run*/

    
  // Create harvest
  //-------------------
  var fsh  = new FileSystemHarvester
  
  fsh.addPath(new File("src/test/testFS").toPath())
  
  fsh.addPath(new File("/home/rleys/git/adl/Neutrinomass_ADC").toPath())
  
  fsh.addPath(new File("/home/rleys/eclipse-workspaces/mars").toPath())
  
  fsh.addPath(new File("""E:\Common\Projects\git""").toPath())

  Harvest.addHarvester(fsh)
  fsh.addChildHarvester(new MavenProjectHarvester) 
  //fsh.addChildHarvester(new POMFileHarvester)
  //fsh.addChildHarvester(new TCLFileHarvester)
  
 
  
  
 // Brain += (new ExternalBrainRegion(new File("/home/rleys/git/adl/dev-tools/scala/adept-interface"),"kit.ipe.adl.bsp.adept.AdeptModule"))
 // Harvest.run
  println(s"WWWVIew content now: "+WWWViewHarvester.getResources.size)
  
  
  Brain.onAllRegions { 
    r =>  
      
      println(s"Found Region: "+r.name+" -> childrend: "+r.subRegions.size)
  
  }
  

  
  Harvest.run
  Harvest.printHarvesters
  
  /*Harvest.run
  Harvest.printHarvesters*/
  
  /*
  println(s"Finding Scala App Mains and running them")
  
  Harvest.onHarvesters[ScalaAppHarvester] {
    case appHarvester if(appHarvester.getResources.size>0) => 
      
      println(s"Found Scala App sources: "+appHarvester.getResources)
      appHarvester.onResources[ScalaAppSourceFile] {
        case r => 
          println(s"Found A Scala App to load: "+r+", parent: "+r.parentResource)
          
          r.ensureCompiled
          r.run
          
      }
      
    
    
  }*/
  
  
  
  Console.readLine()
  println(s"Stopping")
  
}