package org.odfi.indesign.core.brain

import java.net.URL

import com.idyria.osi.tea.logging.TLogSource

import org.odfi.indesign.core.brain.external.FolderOutBuilder
import org.odfi.indesign.core.config.model.CommonConfigTraitValuesKey
import com.idyria.osi.tea.compile.ClassDomain
import scala.reflect.ClassTag
import org.odfi.indesign.core.harvest.Harvester
import org.odfi.indesign.core.harvest.HarvestedResource

class RegionClassName(val className: String, val region: ExternalBrainRegion) extends HarvestedResource {
  this.deriveFrom(region)

  override def getId = getClass.getCanonicalName+":"+className

  def isType[T](implicit tag: ClassTag[T]) = {
    load[T] match {
      //case ESome(cl) if (tag.runtimeClass.isAssignableFrom(cl)) => true 
      case ESome(r) if (tag.runtimeClass.isInstance(r)) => true
      case ENone => false
      case EError(err) =>
        addError(err)
        false
    }
  }

  def load[T](implicit tag: ClassTag[T]) = region.loadRegionClass(className) match {
    //case ESome(cl) if (tag.runtimeClass.isAssignableFrom(cl)) => true 
    case ESome(r) if (tag.runtimeClass.isInstance(r)) =>

      this.addDerivedResource(r)
      ESome(r.asInstanceOf[T])
    case ENone => ENone
    case EError(err) =>
      addError(err)
      ENone
    case other => ENone
  }

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
    logFine[ExternalBrainRegion](s"Harvesting on external region: ${getClass}")

    //-- Gather Region Classes
    this.discoverRegions.foreach {
      name =>

        logFine[ExternalBrainRegion](s"Found Module name: " + name)

        //-- Gather
        gather(new RegionClassName(name, this))
    }

    //-- Gather Region themselves

    /*configKey match {
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
    }*/
  }

  // Created gatehered resources
  //---------------
  this.onGatheredResources {
    resources =>
      resources.collect { case e: RegionClassName => e }.foreach {
        regionClass =>

          //-- IF the region is kept found, recheck it's state with the main config
          regionClass.onKept {
            case h if (h == this) =>

              logFine[ExternalBrainRegion]("Region kept: " + regionClass.className + " -> Checking for setup")

              configKey match {
                case Some(configBase) =>

                  configBase.values.foreach {
                    v =>
                      logFine[ExternalBrainRegion]("-- Available config: " + v)
                  }
                  
                  this.derivedResources.foreach {
                    case (id,region : BrainRegion) => 
                      logFine[ExternalBrainRegion]("-- Region present: "+region.getClass.getCanonicalName)
                    case other => 
                  }

                  configBase.values.find { v => v.toString() == regionClass.className } match {

                    //-- Region is configured and not present
                    case Some(configured) if(this.findDerivedResourceOfTypeAnd[BrainRegion](_.getClass.getCanonicalName == regionClass.className).isEmpty) =>

                      this.loadRegionClass(regionClass.className) match {
                        case ESome(region) =>

                          logFine[ExternalBrainRegion]("Saving REgion: "+regionClass.className)
                          
                          //-- Save Region
                          this.addDerivedResource(region)

                          //-- Cleaning
                          /*region.onClean {
                            region.moveToShutdown
                          }*/

                          //-- Move to same state as this one
                          this.currentState match {
                            case Some(state) =>
                              Brain.moveToState(region, state)
                            case None =>
                            //Brain.moveToState(region, this.currentState.get)
                          }

                        case ENone =>
                        case EError(e) => addError(e)
                      }

                    //-- Not configured
                    case None =>

                      //-- Look if regino is present, if then remove
                      this.derivedResources.find {
                        case (id, resource: BrainRegion) if (resource.getClass.getCanonicalName == regionClass.className) =>
                          true
                        case other =>
                          false
                      } match {
                        case Some((id, region)) =>
                          this.cleanDerivedResource(region)
                        case None =>
                      }

                    //-- Present
                    case other => 
                      
                  }

                case None =>
              }

          }

      
      }
  }


  this.onShutdown {
    /*println(s"Cleaning derived resources")
    this.derivedResources.foreach {
      r => 
        println(s"-> "+r)
    }*/
    // Remove all sub regions
    this.cleanDerivedResources
  }
  this.onCleaned {
    case h =>
      logFine[ExternalBrainRegion](s"Cleaning External Region")
      this.cleanDerivedResources
      this.moveToShutdown
  }

  //var wrappedRegions = List[BrainRegion]

  /**
   * Load a Region Class using this external region method
   */
  def loadRegionClass(cl: String): ErrorOption[BrainRegion]

 

  // Region Discover
  //--------------

  def discoverRegions: List[String] = {
    List[String]()

  }
  
  // Type Discover
  //------------------
  
  def discoverType[CT <: Any](implicit tag :ClassTag[CT]) = {
    
    List[Class[CT]]()
    
  }
  
  // Reload
  //------------

  /**
   * Shutdown and Reload of external region should lead to deletion of all related loaded regions and enabling reloading
   */
  def reload = {
    keepErrorsOn(this)(this.moveToShutdown)
    keepErrorsOn(this)(this.resetState)
    keepErrorsOn(this)(this.moveToStart)
  }

  

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