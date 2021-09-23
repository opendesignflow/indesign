package org.odfi.indesign.core.main

import java.io.File

import org.odfi.tea.logging.TLog

import org.odfi.indesign.core.brain.Brain
import org.odfi.indesign.core.config.Config
import org.odfi.indesign.core.config.ooxoo.OOXOOFSConfigImplementation
import org.odfi.indesign.core.harvest.Harvest
import org.odfi.indesign.core.harvest.fs.FileSystemHarvester
import org.odfi.indesign.core.brain.BrainRegion
import org.odfi.indesign.core.harvest.Harvester
import org.odfi.indesign.core.brain.LFCDefinition
import org.odfi.indesign.core.brain.ExternalBrainRegion
import org.odfi.indesign.core.heart.Heart
import org.odfi.indesign.core.module.HarvesterModule

object IndesignPlatorm  {

  // Utilities to help build simpler and faster mains
  //------------------

  def prepareDefault = {
    Brain.deliverDirect(Harvest)
    Brain.deliverDirect(Config)
    Harvest.addHarvester(Brain)
  }
  
  def setHarvestDelay(delay:Int) = {
    Harvest.harvestTask.scheduleEvery = Some(delay)
    Heart.repump(Harvest.harvestTask)
  }
  
  def stopHarvest = {
    Harvest.scheduleHarvest(0)
    
    Heart.killTask(Harvest.harvestTask)
  }

  def start = {
    Brain.moveToStart
    Harvest.run
  }

  def stop = {
    Brain.moveToShutdown
  }
  
  def use(r: BrainRegion): BrainRegion = {
    Brain.gatherPermanent(r)
    r.moveToSetup
    r
  }

  def use(h: Harvester): Harvester = {
    Harvest.addHarvester(h)
    h
  }
  
  def use(r: HarvesterModule): HarvesterModule = {
    Brain.gatherPermanent(r)
    r
  }


}