package edu.kit.ipe.adl.indesign.core.main

import java.io.File

import com.idyria.osi.tea.logging.TLog

import edu.kit.ipe.adl.indesign.core.brain.Brain
import edu.kit.ipe.adl.indesign.core.config.Config
import edu.kit.ipe.adl.indesign.core.config.ooxoo.OOXOOFSConfigImplementation
import edu.kit.ipe.adl.indesign.core.harvest.Harvest
import edu.kit.ipe.adl.indesign.core.harvest.fs.FileSystemHarvester
import edu.kit.ipe.adl.indesign.core.brain.BrainRegion
import edu.kit.ipe.adl.indesign.core.harvest.Harvester

object IndesignPlatorm extends App {

  println("Welcome to the Indesign Platform")
  
  TLog.setLevel(classOf[Brain], TLog.Level.FULL)

  // TLog.setLevel(classOf[Harvester], TLog.Level.FULL)
  
  /*var n = ""
  println(s"Name: "+Config.documentName(Brain))
  sys.exit*/

  /*var gitName = "edu.kit.ipe.adl.indesign.core.module.git.GitModule$"
  
  var gitClass = getClass.getClassLoader.loadClass(gitName) 
  
  gitClass.getFields.foreach {
    m => 
      println(s"M: "+m.getName)
  }
  var f = gitClass.getField("MODULE$")
  f.setAccessible(true)
  var instance = f.get(null).asInstanceOf[BrainRegion]
  
  println(s"Module: "+instance.hashCode())
  println(s"Module: "+GitModule.hashCode())
  
  sys.exit*/

  // Brain / Config Setup
  //-------------------
  
  var wsName = args.indexOf("--workspace") match {
    case -1 => new File("workspace-main").getCanonicalFile
    case i =>
      var targetFile = new File("workspace-"+args(i + 1)).getCanonicalFile
      targetFile match {
        case f if (!f.exists || !f.isDirectory()) => sys.error("Content Folder Folder must exists and be directory")
        case f => f
      }

  }
  wsName.mkdirs
  
  Config.setImplementation(new OOXOOFSConfigImplementation(new File(wsName,"indesign-config")))
  Brain.deliverDirect(Harvest)
  Brain.deliverDirect(Config)
  Harvest.addHarvester(Brain)
  
   //-- Add one Harvester
  Harvest.addHarvester(new FileSystemHarvester)

  
  /*
   Brain.onResources[BrainRegion] {
    case r => 
      println("Found Region: "+r)
  }
  Harvest.addHarvester(Brain)
  Harvest.run
  Brain.onResources[BrainRegion] {
    case r => 
      println("Found Region: "+r)
  }
  Harvest.run
  Brain.onResources[BrainRegion] {
    case r => 
      println("Found Region: "+r)
  }*/
  Harvest.run
  
  
  /*Console.readLine()
  Brain.moveToInit
   Harvest.printHarvesters
  
  Console.readLine()
    Harvest.run
  Console.readLine()
  Harvest.run
  
  Console.readLine()
  Harvest.printHarvesters
  sys.exit
  Harvest.run*/
  
  
  Brain.moveToStart

  /* sys.exit
  Brain += (Config, Harvest)*/

 
  // Init
  //---------------
  println(s"Start init ")
  //Brain.init
  println(s"Done init ")

  // Run One Harvest Phase
  //-----------
  Harvest.printHarvesters

  /*Harvest.run
  Harvest.printHarvesters*/

}