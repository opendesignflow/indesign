package org.odfi.indesign.core.config

import org.odfi.indesign.core.config.model.CommonConfigTrait
import org.odfi.indesign.core.config.model.CommonConfig
import com.idyria.osi.tea.listeners.ListeningSupport
import org.odfi.indesign.core.harvest.HarvestedResource

trait ConfigSupport extends ConfigHolder with HarvestedResource {

  // def getId = "default"

  /**
   * get Config
   */
  var __config: Option[CommonConfig] = None

  // Clean config on reload
  Config.onRealmChanged {
    synchronized {
      __config = None
    }

  }

  def isConfigLoaded: Boolean = synchronized { __config.isDefined }

  def config: Option[CommonConfig] = synchronized {
    __config match {
      case Some(_) => __config
      case None =>
        __config = Config.getConfigFor(this)
        
        __config
    }
  }
  
  def saveConfig = {
    this.config match {
      case Some(c) => c.resyncToFile
      case other => 
    }
  }
  
  // User Config
  //-------------------
  
  // Get Values
  //-----------------
  def configGetDouble(name:String,default:Double) = config match {
    case Some(c) => c.getDouble(name, default)
    case other => default
  }
  
  def configGetString(name:String,default:String) = config match {
    case Some(c) => c.getString(name, default)
    case other => default
  }
  
  

}