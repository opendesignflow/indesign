package org.odfi.indesign.core.config

import com.idyria.osi.tea.listeners.ListeningSupport
import org.odfi.indesign.core.config.model.CommonConfig

/**
 * Trait to mark classes which may hold a configuration
 */
trait ConfigHolder extends ListeningSupport {
  
  
  /**
   * Config Possible keys
   */
  def updateAvailableKeysToConfig = {
    
  }
  
  def triggerConfigUpdated = {
    this.@->("config.updated")
  }
  
  /**
   * Reacts on Config Update
   * If no config is loaded, try to load it, so that the action gets called right away
   */
  def onConfigUpdated(cl: => Any) = {
    isConfigLoaded match {
      case true => 
      case false => 
        this.config match {
          case Some(c) => cl 
          case None=>
        }
    }
    this.on("config.updated") {
      cl
    }
  }
  
  def isConfigLoaded : Boolean
  def config : Option[CommonConfig]
  
  
  
}