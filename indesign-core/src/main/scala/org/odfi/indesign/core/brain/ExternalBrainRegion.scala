package org.odfi.indesign.core.brain

import java.net.URL

import com.idyria.osi.tea.logging.TLogSource

import org.odfi.indesign.core.brain.external.FolderOutBuilder
import org.odfi.indesign.core.config.model.CommonConfigTraitValuesKey
import com.idyria.osi.tea.compile.ClassDomain
import scala.reflect.ClassTag
import org.odfi.indesign.core.harvest.Harvester
import org.odfi.indesign.core.harvest.HarvestedResource

class RegionClassName(val className: String) extends HarvestedResource {

  override def getId = className

}

/**
 * Loads a Brain Region present in another externaly compiled module
 */
trait ExternalBrainRegion extends BrainRegion with Harvester {

  // Associated Key Config
  var configKey: Option[CommonConfigTraitValuesKey] = None

  // Path Information
  def getRegionPath: String

  override def getId = super.getId

  // Building
  //------------
  var regionBuilder: Option[ExternalBrainRegionBuilder] = None

  // Region Class Loading
  //--------------

  /**
   * Load Sub Regions when in load State
   * Resolve parent class domain and such during setup
   */
  override def doHarvest = {
    logFine[Brain](s"Moving to load on external region: ${getClass}")
    configKey match {
      case Some(key) =>
        key.values.drop(1).foreach {
          cv =>
            logFine[Brain](s"Loading Region/Module from config: $cv")

            //-- Create Region
            //var region = loadRegionClass(cv)

            //-- Gather
            gather(new RegionClassName(cv))

        }
      case None =>
    }
  }

  // Created gatehered resources
  //---------------
  this.onGatheredResources {
    resources =>
      resources.collect { case e: RegionClassName => e }.foreach {
        regionClass =>

          var region = addRegionClass(regionClass.getId) match {
            case ESome(region) =>
              region.onCleaned {
                case h if (h == ExternalBrainRegion.this) =>
                  region.moveToShutdown

              }
            case other =>
          }

      }
  }

  /**
   * Load Sub Regions when in load State
   * Resolve parent class domain and such during setup
   */
  /*this.onLoad {
    logFine[Brain](s"Moving to load on external region: ${getClass}")
    configKey match {
      case Some(key) =>
        key.values.drop(1).foreach {
          cv =>
            logFine[Brain](s"Loading Region/Module from config: $cv")
            /* var current = external.subRegions.size
              external.loadRegionClass(cv)
              var now = external.subRegions.size
              if (now <= current) {
                logWarn[Brain]("No errors during region load, it should be added to sub regions")
              }*/

            //-- Create Region
            var region = addRegionClass(cv)

        }
      case None =>
    }
  }*/

  this.onShutdown {
    // Remove all sub regions
    this.cleanDerivedResources
  }
  this.onCleaned {
    case h =>
      logFine[Brain](s"Cleaning External Region")
      this.cleanDerivedResources
      this.moveToShutdown
  }

  //var wrappedRegions = List[BrainRegion]

  /**
   * Load a Region Class using this external region method
   */
  def loadRegionClass(cl: String): ErrorOption[BrainRegion]

  /**
   * Uses Load Region to create instance, and deliver
   */
  def addRegionClass(cl: String): ErrorOption[BrainRegion] = {

    //-- Create
    try {
      logFine[Brain](s"Add Region: $cl ")
      loadRegionClass(cl) match {
        case ESome(create) =>
          addDerivedResource(create)

          //-- Make sure it is in config
          configKey match {
            case Some(key) if (key.values.find { v => v.toString() == cl }.isEmpty) =>
              key.values.add.data = (cl)
            case _ =>
          }

          //-- Move to same state
          this.currentState match {
            case Some(s) =>
              keepErrorsOn(create)(Brain.moveToState(create, s))
            case None =>
          }

          ESome(create)

        case other =>
          other
      }

    } catch {
      case e: Throwable =>
        e.printStackTrace()
        throw e
    }
  }

  // Region Discover
  //--------------

  def discoverRegions: List[String] = {
    List[String]()

  }
  // Reload
  //------------

  /**
   * Shutdown and Reload of external region should lead to deletion of all related loaded regions and enabling reloading
   */
  def reload = {
    keepErrorsOn(this)(this.moveToShutdown)
    keepErrorsOn(this)(this.moveToLoad)
    keepErrorsOn(this)(this.moveToInit)
  }

  //var wrappedRegion : BrainRegion

  /*
  override def name = wrappedRegion.name
  

  this.onInit {
    //println(s"External INIT")
    Brain.moveToState(wrappedRegion, "init")
  }

  this.onLoad {
   //  println(s"External LOAD")
    Brain.moveToState(wrappedRegion, "load")
  }*/

}

trait ExternalBrainRegionBuilder {

  def accept(url: URL): Integer
  def build(url: URL): ExternalBrainRegion

  def isTainted = getClass.getClassLoader.isInstanceOf[ClassDomain] && getClass.getClassLoader.asInstanceOf[ClassDomain].tainted
}

object ExternalBrainRegion extends TLogSource {

  var builders = List[ExternalBrainRegionBuilder](new FolderOutBuilder)

  def removeBuilder[BT <: ExternalBrainRegionBuilder](implicit tag: ClassTag[BT]): Unit = {
    this.builders = builders.filter {
      case r if (r.isTainted) =>
        false
      case r if (r.getClass.getCanonicalName == tag.runtimeClass.getCanonicalName) =>
        //println("Forcing replacement of: "+r)
        false
      case _ =>
        true
    }
  }

  def addBuilder(b: ExternalBrainRegionBuilder, force: Boolean = false) = {

    // Clean Tainted
    this.builders = builders.filter {
      case r if (r.isTainted) =>
        false
      case r if (force && r.getClass.getCanonicalName == b.getClass.getCanonicalName) =>
        println("Forcing replacement of: " + r)
        false
      case _ =>
        true
    }

    // Add
    this.builders.find { eb => eb == b || eb.getClass == b.getClass } match {
      case Some(existing) =>
        logWarn[Brain](s"Cannot add external region builder $b, it already exists or another instance of same type exists")
      case None =>
        this.builders = this.builders :+ b
    }
  }

  /**
   * Look for candidates
   */
  def build(url: URL) = {

    // Get Builder scores
    var matchingBuilders = builders.map { b => (b, b.accept(url)) }.filter { case (b, score) => score > 0 }.sortBy { case (b, score) => score }

    matchingBuilders.size match {
      case 0 =>
        throw new RuntimeException(s"Cannot Builder External Region : $url, no matching builder")
      case other =>

        // Use the highest score
        var winner = matchingBuilders.last

        // Issue a Warning if more than one builder has the same score
        matchingBuilders.filter { case (b, score) => score == winner._2 } match {
          case res if (res.size <= 1) =>

          case res => logWarn[Brain](s"Two Builders have identical scores for $url: $res")
        }

        //logFine[Brain](s"Building $url with ${winner._1}")
        //println(s"Building $url with ${winner._1} "+)
        var resultRegion = winner._1.build(url)
        resultRegion.regionBuilder = Some(winner._1)
        resultRegion.@->("region.created")
        resultRegion
    }

  }

}