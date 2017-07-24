package org.odfi.indesign.core.config.ooxoo

import org.odfi.indesign.core.module.IndesignModule
import org.odfi.indesign.core.config.Config
import java.io.File

object OOXOOConfigModule extends IndesignModule {
  
  private var configFolder = new File("indesign-config")
  
//  println("Init configu module: "+configFolder)
  def setConfigFolder(f:File) = {
   // println("Setting Config Folder:"+f)
    this.configFolder = f
  }
  
  this.onSetup {
    requireModule(Config)
    
    
    
  }
  
  this.onLoad {
   // println("Setting up implementation to "+configFolder)
    Config.setImplementation(new OOXOOFSConfigImplementation(configFolder))
  }
  
  
  this.onStart {
    
  }
  
  // Utils
  //-------------

}