package org.odfi.indesign.core.config

import org.odfi.indesign.core.config.model.CommonConfigTrait
import org.odfi.indesign.core.harvest.Harvester
import org.odfi.indesign.core.brain.BrainRegion
import org.odfi.indesign.core.config.model.HarvesterConfig
import org.odfi.indesign.core.config.model.DefaultConfig
import org.odfi.indesign.core.config.model.RegionConfig
import org.odfi.indesign.core.config.model.CommonConfig
import com.idyria.osi.ooxoo.db.store.DocumentContainer
import org.odfi.indesign.core.module.IndesignModule
import org.odfi.indesign.core.harvest.fs.FSGlobalWatch
import com.idyria.osi.ooxoo.core.buffers.structural.io.sax.STAXSyncTrait
import org.odfi.indesign.core.harvest.HarvestedResource
import org.odfi.indesign.core.harvest.Harvest

object Config extends IndesignModule {

  // Make this region always present
  this.root

  // Require file watcher
  this.onLoad {
    requireModule(FSGlobalWatch)
  }

  var implementation: Option[ConfigImplementation] = None

  /**
   * Set implementation realm
   * Ask implementation if it knows about the latest used realm
   */
  def setImplementation(i: ConfigImplementation) = {

    implementation = Some(i)
   // println(s"Starting from last realm with impl: "+i)
    i.detectLatestRealm match {
      case Some(realm) =>
        currentRealm = realm
      //  println("Found previous realm detected: "+realm)
      case None =>
     //   println("Not previous realm detected")
    }
  //  println(s"******************* OPENING ${currentRealm} *********************")
    implementation.get.openConfigRealm(this.currentRealm)
  }

  def listAvailableRealms = implementation match {
    case Some(i) => i.listAllRealms
    case None => List()
  }

  // Config realm
  //------

  var __currentRealm = "default"

  def currentRealm_=(str: String) = str match {
    case sameascurrent if (sameascurrent == __currentRealm) =>
    case other =>
      this.__currentRealm = str
      implementation match {
        case Some(impl) => 
          impl.openConfigRealm(str)
          this.@->("realm.changed")
        case None =>
      }
  }

  def currentRealm = __currentRealm

  def addRealm(name:String) = implementation match {
    case Some(i) =>
      i.addRealm(name)
    case None => 
  }
  
  def onRealmChanged(cl: => Any) = {
    this.on("realm.changed") {
      cl
    }
  }
  
  /*def getImplementation = implementation match {
    case Some(i) => i 
    case None => th
  }*/

  def documentName(target: HarvestedResource): String = {

    target.getClass.getCanonicalName match {
      // Object
      case cn if (cn.endsWith("$")) =>
        cn.replace("$", "")
      case cn =>
        // Class
        cn.replace("$", "Object") + "_" + target.getId
    }

  }

  /**
   * For config in external model than target, autosave is disabled if the target model is not a main file
   */
  def getConfigFor[CS <: ConfigSupport](target: ConfigSupport): Option[CommonConfig] = {

    try {

      // Get Document
      var document = implementation match {
 
        case Some(impl) =>
          target.getClass match {

            // Config is in external model
            case cl if (classOf[ConfigInModel[_]].isAssignableFrom(cl)) => 
              
              val configInModel = target.asInstanceOf[ConfigInModel[CommonConfig]]
              if (configInModel.configModel.isDefined && configInModel.configModel.get.staxPreviousFile.isEmpty) {
                configInModel.configModel.get.autosave = false
              }
              configInModel.configModel
              
            
            // Harvester
            //---------------
            case cl if (classOf[Harvester].isAssignableFrom(cl)) =>

              var c = impl.getContainer("harvesters")
              c.document(documentName(target), new HarvesterConfig, true)

            // Region
            //----------------
            case cl if (classOf[BrainRegion].isAssignableFrom(cl)) =>

              var c = impl.getContainer("regions")
              c.document(documentName(target), new RegionConfig, true)

            // Main/Default
            //----------
            case cl =>
              var c = impl.getContainer("default")
              c.document(documentName(target), new DefaultConfig, true)
          }
        case None => 
          println(s"No config impl defined")
          None
      }

      //-- See if we can listen to changes
      /*document match {
        case Some(doc: STAXSyncTrait) if(!FSGlobalWatch.watcher.isMonitoredBy(this, doc.staxPreviousFile.get)) =>
          doc.staxFileWatcher = FSGlobalWatch.watcher
          doc.onFileReload(this) {
            f =>
              //println(s"File reload")
              doc.parentContainer.get.clearCached(documentName(target))
              target.__config = getConfigFor(target)
              target.__config.get.staxPreviousFile = doc.staxPreviousFile
              target.triggerConfigUpdated

              //println(s"**** Reloaded -> Harvest")
             // Harvest.run
          }
        case other =>
      }*/

      document
    } catch {
      case e: Throwable =>
        //e.printStackTrace()
        None
    }

  }

  // Def Builders
  //------------------
  def apply(imp: ConfigImplementation) = {

    this.setImplementation(imp)

    this
  }

  // Generic Database Access
  //------------------------

  def getContainerFor(cl: String): Option[DocumentContainer] = {

    this.implementation match {
      case Some(implementation) =>
        Some(implementation.getContainer(cl))
      case None => None
    }

  }

  /**
   * Returns a container named after the class
   */
  def getContainerFor(cl: Class[_]): Option[DocumentContainer] = getContainerFor(cl.getCanonicalName.replace("$", ""))

  def getContainerFor(o: AnyRef): Option[DocumentContainer] = this.getContainerFor(o.getClass)
}