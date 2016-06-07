package edu.kit.ipe.adl.indesign.core.brain

import edu.kit.ipe.adl.indesign.core.config.ConfigSupport
import java.io.File
import com.idyria.osi.tea.logging.TLogSource
import edu.kit.ipe.adl.indesign.core.harvest.Harvester
import edu.kit.ipe.adl.indesign.core.heart.Heart

trait Brain extends BrainLifecyleDefinition with BrainLifecycle with ConfigSupport with TLogSource {

}

object Brain extends Brain with Harvester {

  sys.addShutdownHook {
    Brain.moveToShutdown
  }
  
  Brain.deliverDirect(Heart)
 
  
  // Regions
  ///--------------------
  // var regions = List[BrainRegion]()

  /*def +=(rs: BrainRegion*) = {
    this.regions = this.regions ++ rs
    Brain.currentState match {
      case Some(state) => rs.foreach { r => Brain.moveToState(r, state) }
      case None =>
    }

  }*/

  /**
   * Process Depth first ordered
   */
  /* def onAllRegions(cl: BrainRegion => Unit) = {

    var processList = new scala.collection.mutable.ListBuffer[BrainRegion]()
    processList ++= this.regions

    while (processList.nonEmpty) {
      var r: BrainRegion = processList.head
      processList -= r
      r.keepErrorsOn(r) {
        cl(r)
        processList ++= r.subRegions.toTraversable.asInstanceOf[Traversable[BrainRegion]]
      }
    }

  }*/

  // Region Harvesting
  //-----------------

  onDeliverFor[BrainRegion] {
    case r =>
      gather(r)
      true
  }

  override def doHarvest = {

    // Get Regions
    //-------------------
    this.config match {
      case Some(conf) =>
        conf.values.keys.foreach {
          case key if (key.keyType === "region") =>
            key.values.foreach {
              v =>
                logFine[Brain](s"Adding Region: " + v)
                gather(Brain.createRegion(getClass.getClassLoader, v))
            }

          case key if (key.keyType === "external-region") =>

            var path = key.values(0).toString

            logFine[Brain](s"Adding External Region: $path ")
            try {
              keepErrorsOn(this) {
                
                var external = ExternalBrainRegion.build(new File(path).toURI().toURL)
                external.configKey = Some(key)
                gather(external)
              }

            } catch {
              case e: Throwable =>
              e.printStackTrace()
            }

          //-- Load classes in the region
          /* key.values.drop(1).foreach {
              cv =>
                logFine[Brain](s"Loading Region $cv")
                var current = external.subRegions.size
                external.loadRegionClass(cv)
                var now = external.subRegions.size
                if (now <= current) {
                  logWarn[Brain]("No errors during region load, it should be added to sub regions")
                }
            }*/

          case _ =>
        }
      case None =>
    }

  }

  // Region Creation
  //----------

  def createRegion(cl: ClassLoader, name: String) = {

    //-- Get Class
    var regionClass = cl.loadClass(name)

    //-- Object/Class
    name match {
      // Object
      case name if (name.endsWith("$")) =>
        regionClass.getFields.find { f => f.getName == "MODULE$" } match {
          case Some(objectField) =>
            objectField.setAccessible(true)
            objectField.get(null).asInstanceOf[BrainRegion]
          case None =>
            throw new RuntimeException(s"Cannot Create Region $name from Classloader $cl, detected object but no field MODULE$$ defined..maybe not an object")
        }

      // Class
      case name =>
        regionClass.newInstance().asInstanceOf[BrainRegion]
    }

  }

  // Lifecylce
  //------------------
  this.onSetup {
    this.onResources[BrainRegion]( r => r.keepErrorsOn(r)(Brain.moveToState(r, "setup")))
  }
  this.onLoad {
    this.onResources[BrainRegion](r => r.keepErrorsOn(r)(Brain.moveToState(r, "load")))
  }
  this.onInit {
    this.onResources[BrainRegion](r => r.keepErrorsOn(r)(Brain.moveToState(r, "init")))
  }
  this.onStart {
    this.onResources[BrainRegion](r => r.keepErrorsOn(r)(Brain.moveToState(r, "start")))
  }
  this.onStop {
    this.onResources[BrainRegion](r => r.keepErrorsOn(r)(Brain.moveToState(r, "stop")))
  }
  this.onShutdown {
    this.onResources[BrainRegion](r => r.keepErrorsOn(r)(Brain.moveToState(r, "shutdown")))
  }

  /*def load = {

    // Load
    //-------------
    Brain.moveToState(this, "load")

  }

  this.registerStateHandler("load") {

    logFine[Brain](s"Loading Regions")

    // Regions
    //
    this.regions.foreach {
      r =>
        //println(s"load region $r")
        Brain.moveToState(r, "load")
    }
  }*/

  /**
   *
   */
  /*def init = {
    Brain.moveToState(this, "init")

  }

  this.registerStateHandler("init") {
    this.regions.foreach {
      r =>
        //println(s"init region $r")
        Brain.moveToState(r, "init")
    }
  }*/

}