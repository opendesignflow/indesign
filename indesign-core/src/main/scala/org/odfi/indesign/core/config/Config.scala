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

  def setImplementation(i: ConfigImplementation) = {
    
    
    implementation = Some(i)
    implementation.get.openConfigRealm(this.currentRealm)
  }

  // Config realm
  //------
  
  var __currentRealm = "default"
  
  def currentRealm_=(str:String) = {
    this.__currentRealm = str
    implementation match {
      case Some(impl) => impl.openConfigRealm(str)
      case None=>
    }
  }
  
  def currentRealm = __currentRealm
  
  
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

  def getConfigFor[CS <: ConfigSupport](target: ConfigSupport): Option[CommonConfig] = {

    try {

      // Get Document
      var document = implementation match {
        case Some(impl) =>
          target.getClass match {

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
        case None => None
      }
      
      //-- See if we can listen to changes
      document match {
        case Some(doc : STAXSyncTrait) =>
          doc.staxFileWatcher = Some(FSGlobalWatch.watcher)
          doc.onFileReload(this) { 
            f => 
              
              doc.parentContainer.get.clearCached(documentName(target))  
              target.__config = getConfigFor(target)
              target.triggerConfigUpdated
              
              println(s"**** Reloaded -> Harvest")
              Harvest.run
          }
        case other  => 
      }
      
      document
    } catch {
      case e: Throwable =>
        //e.printStackTrace()
        None
    }

  }
  
  // Def Builders
  //------------------
  def apply (imp: ConfigImplementation) = {
    
    this.setImplementation(imp)
    
    this
  }
  
  // Generic Database Access
  //------------------------
  
   def getContainerFor(cl:String) : Option[DocumentContainer] = {
    
    this.implementation match {
      case Some(implementation) => 
        Some(implementation.getContainer(cl))
      case None => None
    }
    
  }
  
  /**
   * Returns a container named after the class
   */
  def getContainerFor(cl:Class[_]) : Option[DocumentContainer] = getContainerFor(cl.getCanonicalName.replace("$", ""))
  
  def getContainerFor(o:AnyRef) : Option[DocumentContainer] =  this.getContainerFor(o.getClass)
}