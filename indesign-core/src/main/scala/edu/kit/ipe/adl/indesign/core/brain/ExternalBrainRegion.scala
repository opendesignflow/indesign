package edu.kit.ipe.adl.indesign.core.brain

import java.net.URL

import com.idyria.osi.tea.logging.TLogSource

import edu.kit.ipe.adl.indesign.core.brain.external.FolderOutBuilder
import edu.kit.ipe.adl.indesign.core.config.model.CommonConfigTraitValuesKey
import com.idyria.osi.tea.compile.ClassDomain

/**
 * Loads a Brain Region present in another externaly compiled module
 */
trait ExternalBrainRegion extends BrainRegion {

  // Associated Key Config
  var configKey: Option[CommonConfigTraitValuesKey] = None

  // Path Information
  def getRegionPath:String
  
  // Building
  //------------
  var regionBuilder : Option[ExternalBrainRegionBuilder]  = None
  
  // Region Class Loading
  //--------------

  /**
   * Load Sub Regions is Gathered on Brain
   */
  /*this.onGathered {
    case h if (h == Brain) =>

  }*/

  this.onLoad {
    logFine[Brain](s"Moving to load on: ${getClass}")
    configKey match {
      case Some(key) =>
        key.values.drop(1).foreach {
          cv =>
            logFine[Brain](s"Loading Region $cv")
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
  }

  this.onShutdown {
    // Remove all sub regions
    this.cleanDerivedResources
  }
  this.onCleaned {
    case h =>
      this.cleanDerivedResources
  }

  //var wrappedRegions = List[BrainRegion]

  /**
   * Load a Region Class using this external region method
   */
  def loadRegionClass(cl: String): BrainRegion

  /**
   * Uses Load Region to create instance, and deliver
   */
  def addRegionClass(cl: String): BrainRegion = {

    //-- Create
    try {
      logFine[Brain](s"Add Region: $cl ")
      var create = loadRegionClass(cl)
      var region = addDerivedResource(create)
      //println(s"Create ${create.hashCode()} ; return ${region.hashCode()}")

      //-- Make sure it is in config
      configKey match {
        case Some(key) if (key.values.find { v => v.toString() == cl }.isEmpty) =>
          key.values.add.setData(cl)
        case _ =>
      }

      //-- Move to same state
      this.currentState match {
        case Some(s) =>
          keepErrorsOn(region)(Brain.moveToState(region, s))
        case None =>
      }
      region
    } catch {
      case e: Throwable =>
        e.printStackTrace()
        throw e
    }
  }
  
  // Region Discover
  //--------------
  
  def discoverRegions : List[String] = {
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

  def addBuilder(b: ExternalBrainRegionBuilder) = {
    
    // Clean Tainted
     this.builders = builders.filter {
      case r if (r.isTainted) =>
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

        logFine[Brain](s"Building $url with ${winner._1}")
        var resultRegion = winner._1.build(url)
        resultRegion.regionBuilder = Some(winner._1)
        resultRegion
    }

  }

}