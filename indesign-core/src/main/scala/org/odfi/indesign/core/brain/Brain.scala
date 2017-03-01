package org.odfi.indesign.core.brain

import org.odfi.indesign.core.config.ConfigSupport
import java.io.File
import com.idyria.osi.tea.logging.TLogSource
import org.odfi.indesign.core.harvest.Harvester
import org.odfi.indesign.core.heart.Heart
import org.eclipse.aether.impl.ArtifactResolver
import org.odfi.indesign.core.artifactresolver.AetherResolver
import org.odfi.indesign.core.brain.artifact.ArtifactExternalRegion
import org.odfi.indesign.core.harvest.Harvest

trait Brain extends BrainLifecyleDefinition with BrainLifecycle with ConfigSupport with TLogSource with Harvester {

}

object Brain extends Brain {

  // Defaults
  //------------

  // Graceful Shutdown
  sys.addShutdownHook {

    //Brain.moveToShutdown
  }

  // Heart is always a region
  Brain.gatherPermanent(Heart)

  // BRain should be in Harvest
  Harvest.addHarvester(this)

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
  
  // Config help
  //-------------------
  def addExternalFolderRegion(file:File) = {
    this.config match {
      case Some(conf) if(!conf.isInConfig("external-region-folder", file.getCanonicalPath)) =>
        var k = conf.addKey("region", "external-region-folder")
        k.values.add.set(file.getCanonicalPath)
        conf.resyncToFile
      case other => 
    }
  }

  // Region Harvesting
  //-----------------

  onDeliverFor[BrainRegion] {
    case r =>
      gather(r)
      true
  }

  override def doHarvest = {

    logFine[Brain](s"Starting Brain Harvest")

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

          case key if (key.keyType === "external-region-folder") =>

            var path = key.values(0).toString

            logFine[Brain](s"Adding External Region: $path ")

            try {
              keepErrorsOn(this) {

                var external = ExternalBrainRegion.build(new File(path).toURI().toURL)
                println(s"Created $path with: " + external.regionBuilder.get.getClass.getClassLoader)
                key.values.foreach {
                  v => 
                    println("Configu key has value: "+v)
                }
                external.configKey = Some(key)
                gather(external)
              }

            } catch {
              case e: Throwable =>
                e.printStackTrace()
            }
          case key if (key.keyType === "external-region-artifact") =>

            //-- Get Spec
            var path = key.values(0).toString
            val spec = """([\w-_\.]+):([\w-_\.]+):([\w-_\.]+)(?::([\w-_\.]+))?""".r
            val spec(gid, aid, v, cl) = path

            logFine[Brain](s"Adding External Region Artficact: $path ")
            try {
              keepErrorsOn(this) {

                var external = new ArtifactExternalRegion(gid, aid, v)
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

    logFine[Brain]("Brain Regions: " + Brain.getResourcesOfType[BrainRegion])

  }

  this.onGatheredResources {
    resources =>

      var regions = resources.collect { 
        case e : ExternalBrainRegion => 
          addChildHarvester(e)
          e
      case e: BrainRegion => e 
        
      }

      logFine[Brain](s"Regions gathered moving to latest state ${this.currentState}: $regions")

      this.currentState match {
        case Some(state) =>
          regions.foreach {
            region =>
              region.keepErrorsOn(region)(this.moveToState(region, state))
          }
        case None =>
      }

    //-- Get Target Index
    /*this.currentState match {
        case Some(cs) =>

          var targetIndexState = this.states.indexOf(cs)
          logFine[Brain](s"Target State: "+targetIndexState)
          //-- Loop over states until target (included hence to "to" usage) and apply to all gathered regions
          (0 to targetIndexState).foreach {
            i =>
              regions.foreach {
                r =>
                  logFine[Brain](s"-> "+Brain.states(i))
                  r.keepErrorsOn(r)(Brain.moveToState(r,Brain.states(i)))
              }

          }

        //-- NO State on brain, go nowhere
        case None =>
      }*/

  }

  // Region Creation
  //----------
  def getObject(cl: ClassLoader, name: String) = {

    //-- Get Class
    var objectClass = cl.loadClass(name)

    //-- Object/Class
    name match {
      // Object
      case name if (name.endsWith("$")) =>
        objectClass.getFields.find { f => f.getName == "MODULE$" } match {
          case Some(objectField) =>
            objectField.setAccessible(true)
            Some(objectField.get(null))
          case None =>
            None
        }

      // Class
      case name =>
        None
    }
  }

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
    this.onResources[BrainRegion](r => r.keepErrorsOn(r)(Brain.moveToState(r, "setup")))
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
  this.onResetState {
    this.onResources[BrainRegion](r => r.keepErrorsOn(r)(Brain.resetLFCState(r)))
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