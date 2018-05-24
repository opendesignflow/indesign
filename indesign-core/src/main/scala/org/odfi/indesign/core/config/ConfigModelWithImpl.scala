package org.odfi.indesign.core.config

import com.idyria.osi.ooxoo.core.buffers.datatypes.ClassBuffer
import com.idyria.osi.ooxoo.core.buffers.structural.xattribute
import org.odfi.indesign.core.config.model.CommonConfig
import javafx.scene.Node
import java.awt.Window
import org.odfi.indesign.core.module.jfx.JFXRun
import javafx.stage.Stage

trait ConfigModelWithImpl[IT <: ConfigInModel[_ <: CommonConfig]] extends CommonConfig {

  // Implementation
  //-------------------
  @xattribute(name = "implementationType")
  var implementationType: ClassBuffer[IT] = null

  var implementationInstance: Option[IT] = None

  def ensureInstance = implementationInstance match {
    case Some(inst) =>
      inst
    case None if (implementationType == null) =>
      sys.error("Cannot ensure instance creation, no @implementationType value present")
    case None =>

      val implresult = (classOf[Node].isAssignableFrom(implementationType.data) || classOf[Stage].isAssignableFrom(implementationType.data)) match {
        case true =>
          JFXRun.onJavaFXBlock(implementationType.data.newInstance()).get
        case false =>
          implementationType.data.newInstance()
      }
      implementationInstance = Some(implresult)
      implementationInstance.get.asInstanceOf[ConfigInModel[CommonConfig]].setConfigModel(this)
      implementationInstance.get
  }

  /**
   * Throws an error if instance if not defined
   */
  def getImplementation = implementationInstance match {
    case Some(instance) => instance
    case _ => sys.error(s"Implementation for ${this.implementationType} not created use ensureInstance to ensure generic creation")
  }

  def deleteImplementation = implementationInstance match {
    case Some(instance) =>
      instance.clean
      implementationInstance = None
    case None =>
  }

  def setImplementation(i: IT) = {
    this.implementationInstance = Some(i)
    implementationInstance.get.asInstanceOf[ConfigInModel[CommonConfig]].setConfigModel(this)
    implementationInstance.get
  }

}