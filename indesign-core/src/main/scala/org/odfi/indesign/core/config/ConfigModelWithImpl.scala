package org.odfi.indesign.core.config

import com.idyria.osi.ooxoo.core.buffers.datatypes.ClassBuffer
import com.idyria.osi.ooxoo.core.buffers.structural.xattribute
import org.odfi.indesign.core.config.model.CommonConfig

trait ConfigModelWithImpl[IT <: ConfigInModel[CommonConfig]] extends CommonConfig  {
  
  // Implementation
  //-------------------
  @xattribute(name="implementationType")
  var implementationType : ClassBuffer[IT] = null
  
  var implementationInstance : Option[IT] = None
  
  def ensureInstance = implementationInstance match {
    case Some(inst) => inst 
    case None if (implementationType==null) => 
      sys.error("Cannot ensure instance creation, no @implementationType value present")
    case None => 
      implementationInstance = Some(implementationType.data.newInstance())
      implementationInstance.get.configModel = Some(this)
      implementationInstance.get
  }
  
  /**
   * Throws an error if instance if not defined
   */
  def getImplementation = implementationInstance match {
    case Some(instance) => instance
    case _ => sys.error("Implementation not created use ensureInstance to ensure generic creation")
  }
  
  def deleteImplementation = implementationInstance match {
    case Some(instance) =>
      instance.clean
      implementationInstance = None
    case None => 
  }
  
}