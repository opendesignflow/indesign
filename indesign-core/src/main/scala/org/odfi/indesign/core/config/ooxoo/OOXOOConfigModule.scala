package org.odfi.indesign.core.config.ooxoo

import org.odfi.indesign.core.module.IndesignModule
import org.odfi.indesign.core.config.Config
import java.io.File

object OOXOOConfigModule extends IndesignModule {
  
  var configFolder = new File("indesign-config")
  
  this.onSetup {
    requireModule(Config)
    
    
    
  }
  
  this.onLoad {
    Config.setImplementation(new OOXOOFSConfigImplementation(configFolder))
  }
  
  
  this.onStart {
    
  }
}