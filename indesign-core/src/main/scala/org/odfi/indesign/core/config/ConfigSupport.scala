package org.odfi.indesign.core.config

import org.odfi.indesign.core.config.model.CommonConfigTrait
import org.odfi.indesign.core.config.model.CommonConfig
import com.idyria.osi.tea.listeners.ListeningSupport

trait ConfigSupport extends ListeningSupport {

  def getId = "default"

  /**
   * Config Possible keys
   */
  def updateAvailableKeysToConfig = {
    
  }
  
  def triggerConfigUpdated = {
    this.@->("config.updated")
  }
  
  def onConfigUpdated(cl: => Any) = {
    this.on("config.updated") {
      cl
    }
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