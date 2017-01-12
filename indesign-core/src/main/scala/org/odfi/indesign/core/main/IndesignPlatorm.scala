package org.odfi.indesign.core.main

import java.io.File

import com.idyria.osi.tea.logging.TLog

import org.odfi.indesign.core.brain.Brain
import org.odfi.indesign.core.config.Config
import org.odfi.indesign.core.config.ooxoo.OOXOOFSConfigImplementation
import org.odfi.indesign.core.harvest.Harvest
import org.odfi.indesign.core.harvest.fs.FileSystemHarvester
import org.odfi.indesign.core.brain.BrainRegion
import org.odfi.indesign.core.harvest.Harvester
import org.odfi.indesign.core.brain.LFCDefinition
import org.odfi.indesign.core.brain.ExternalBrainRegion

object IndesignPlatorm extends App {

  // Utilities to help build simpler and faster mains
  //------------------

  def prepareDefault = {
    Brain.deliverDirect(Harvest)
    Brain.deliverDirect(Config)
    Harvest.scheduleHarvest(2000)
    Harvest.addHarvester(Brain)
  }

  def start = {
    Brain.moveToStart
    Harvest.run
  }

  def stop = {
    Brain.moveToShutdown
  }
  
  def use(r: BrainRegion): BrainRegion = {
    Brain.deliverDirect(r)
    r
  }

  def use(h: Harvester): Harvester = {
    Harvest.addHarvester(h)
    h
  }

  // Indesign Standalone Main
  //----------------------

  println("Welcome to the Indesign Platform")

  TLog.setLevel(classOf[Brain], TLog.Level.FULL)

  //TLog.setLevel(classOf[LFCDefinition], TLog.Level.FULL)

  // Brain / Config Setup
  //-------------------

  var wsName = args.indexOf("--workspace") match {
    case -1 => new File("workspace-main").getCanonicalFile
    case i =>
      var targetFile = new File("workspace-" + args(i + 1)).getCanonicalFile
      targetFile
    /*targetFile match {
        case f if (!f.exists || !f.isDirectory()) => sys.error("Content Folder Folder must exists and be directory")
        case f => f
      }*/

  }
  wsName.mkdirs

  Config.setImplementation(new OOXOOFSConfigImplementation(new File(wsName, "indesign-config")))
  Brain.deliverDirect(Harvest)
  Brain.deliverDirect(Config)
  Harvest.addHarvester(Brain)

  //-- Add one Harvester
  Harvest.run

  Brain.moveToStart

  println("============= Builder check =============")
  ExternalBrainRegion.builders.foreach {
    b =>
      println(s"Available Builder: " + b + " -> " + b.getClass.getClassLoader())
  }

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

  //Console.readLine()

  Harvest.run
  Harvest.run

  println("============= Builder check =============")
  ExternalBrainRegion.builders.foreach {
    b =>
      println(s"Available Builder: " + b + " -> " + b.getClass.getClassLoader())
  }

  /*Harvest.run
  Harvest.printHarvesters*/

}