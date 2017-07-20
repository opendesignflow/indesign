package org.odfi.indesign.core.config

import org.odfi.indesign.core.config.model.CommonConfig

trait ConfigInModel[MT <: CommonConfig] extends ConfigSupport {
  
  var configModel : Option[MT] = None
  
  def setConfigModel(c:MT) = {
    configModel = Some(c)
    this.@->("configModel.updated")
  }
  
  /**
   * 
   * Triggered when this object has a config model set, so configuration operations are safe
   * 
   */
  def onConfigModelUpdated(cl: => Any)  = {
    this.on("configModel.updated") {
      cl
    }
  }
  
}