package org.odfi.indesign.core.config

import org.odfi.indesign.core.config.model.CommonConfigTrait
import org.odfi.indesign.core.config.model.CommonConfig

trait ConfigSupport {

  def getId = "default"

  /**
   * Config Possible keys
   */
  def updateAvailableKeysToConfig = {
    
  }
  
  /**
   * get Config
   */
  var __config: Option[CommonConfig] = None
  def config = __config match {
    case Some(_) => __config
    case None =>
      __config = Config.getConfigFor(this)
      __config 
  }
    
    
  

}