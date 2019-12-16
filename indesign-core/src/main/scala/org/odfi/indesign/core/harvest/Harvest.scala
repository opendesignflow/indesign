package org.odfi.indesign.core.harvest

import scala.reflect.ClassTag

import org.odfi.tea.compile.ClassDomain
import org.odfi.tea.errors.ErrorSupport

import org.odfi.indesign.core.brain.BrainRegion
import org.odfi.indesign.core.heart.HeartTask
import org.odfi.indesign.core.heart.Heart
import org.odfi.tea.listeners.ListeningSupport
import org.odfi.indesign.core.config.ConfigSupport

object Harvest extends BrainRegion with ListeningSupport with ConfigSupport {

  // Make this region always present
  this.root

  // Register Harvest Task
  //------------
  var harvestTask = new HeartTask[Any] {

    def getId = "harvest"

    def doTask = {

      Harvest.run

    }

  }

  def scheduleHarvest(everyMS: Int) = {
    everyMS match {
      case 0 =>
        harvestTask.scheduleEvery = None
        Heart.killTask(harvestTask)
      case other =>
        harvestTask.scheduleEvery = Some(other)
        Heart.repump(harvestTask)
    }

  }

  // Top Harvesters
  //------------------

  var harvesters = List[Harvester]()

  def addHarvester(h: Harvester) = {

    var addingHarvester = h
    this.harvesters.contains(h) match {
      case true =>
      case false =>
        this.harvesters = addingHarvester :: this.harvesters

    }

    h
  }

  def -->[HT <: Harvester](h: HT) = {
    addHarvester(h)
    h
  }

  def removeHarvester(h: Harvester): Harvester = {
    this.harvesters.contains(h) match {
      case true =>
        this.harvesters = this.harvesters.filter(_ != h)
      //h.parentHarvester = None
      case false =>

    }
    h
  }

  /**
   * Type Check is done but no class casting will be involed
   *
   * @warning: Useful to find objects matching a type whose definition may be outdated due to classloading reload
   */
  def getHarvesters[CT <: Harvester](implicit cl: ClassTag[CT]): Option[List[CT]] = {

    this.harvesters.collect { case r if (cl.runtimeClass.isInstance(r) || cl.runtimeClass.getCanonicalName == r.getClass.getCanonicalName) => r } match {
      case res if (res.size > 0) => Some(res.map { h => h.asInstanceOf[CT] })
      case res => None
    }
  }

  /**
   * Process Depth first ordered
   */
  def onAllHarvesters(cl: Harvester => Unit): Unit = {

    var processList = new scala.collection.mutable.ListBuffer[Harvester]()
    processList ++= this.harvesters

    while (processList.nonEmpty) {

      var h = processList.head
      processList -= h
      keepErrorsOn(h) {
        cl(h)
        processList ++= h.childHarvesters
        /*h.errors.foreach {
          e => 
          e.printStackTrace()
        }*/
      }
    }

  }

  /**
   * Process Depth first ordered
   */
  def onAllHarvestersDepthFirst(cl: Harvester => Unit): Unit = {

    var processList = new scala.collection.mutable.Stack[Harvester]()
    processList ++= this.harvesters

    while (processList.nonEmpty) {

      var h = processList.pop()

      keepErrorsOn(h) {
        cl(h)
        processList.pushAll(h.childHarvesters)
      }
    }

  }

  /*def onAllHarvestersDepthFirst(cl: Harvester => Boolean): Unit = {

    var processList = new scala.collection.mutable.Stack[Harvester]()
    processList ++= this.harvesters

    while (processList.nonEmpty) {

      var h = processList.pop()

      keepErrorsOn(h) {
        cl(h) match {
          case true => 
          case false => 
        }
        processList.pushAll(h.childHarvesters)
      }
    }

  }*/

  def onHarvesters[CT <: Harvester](cl: PartialFunction[CT, Unit])(implicit tag: ClassTag[CT]): Unit = {

    // Processing List
    var processList = new scala.collection.mutable.LinkedHashSet[Harvester]()
    processList.addAll(this.harvesters)

    // Visited List
    var visited = scala.collection.mutable.LinkedHashSet[Harvester]()

    while (processList.nonEmpty) {

      // Get Top From Process List
      var h = processList.head
      processList -= h

      // If visited, pass
      visited.contains(h) match {
        case false =>

          // Add to visited
          visited += h

          // Check type and PF definition
          h match {
            case h if (tag.runtimeClass.isInstance(h) && cl.isDefinedAt(h.asInstanceOf[CT])) =>

              cl(h.asInstanceOf[CT])

            case _ =>
          }

          processList.addAll(h.childHarvesters)

        case true =>
      }

    }

  }

  /**
   * This Method runs recursively on Harvesters
   * It uses a stop list to not double collect
   */
  def collectOnHarvesters[CT <: Harvester, B](cl: PartialFunction[CT, B])(implicit tag: ClassTag[CT]) = {

    var collected = List[B]()
    onHarvesters[CT] {
      case h if (cl.isDefinedAt(h)) =>
        collected = cl(h) :: collected
    }
    collected
  }

  def collectResourcesOnHarvesters[CT <: Harvester, RT <: HarvestedResource : ClassTag, B](cl: PartialFunction[RT, B])(implicit htag: ClassTag[CT]) = {

    var r = collectOnHarvesters[CT, List[B]] {
      case h =>
        var r = h.getResourcesOfType[RT].collect {
          case hr if (cl.isDefinedAt(hr)) =>

            cl(hr)
        }
        r

    }
    r.toIterable.flatten
  }

  // Delivering
  //------------------

  def deliverToHarvesters[HT <: Harvester : ClassTag](v: HarvestedResource) = {

    this.getHarvesters[HT] match {
      case Some(harvesters) =>
        harvesters.foreach {
          h =>
            h.deliverDirect(v)
        }
      case None =>

    }

  }

  // Run
  //------------

  def run = {

    //--- Harvest
    //---------------
    this.onAllHarvesters {
      h =>
        keepErrorsOn(h) {
          h.resetErrors
          //println(s"********** harvest on ${h.getClass.getCanonicalName} **************");
          h.harvest
        }
    }

    //-- Done
    //this.@->("done")

    //println(s"********** harvest Done, doing process **************");

    //-- Process
    //-------------------
    this.onAllHarvesters {
      h =>
        keepErrorsOn(h) {
          h.resetErrors
          //println(s"********** harvest on ${h.getClass.getCanonicalName} **************");
          h.processResources
        }
    }

    //-- Done
    this.@->("done")

  }

  def onHarvestDone(cl: => Any) = {

    this.on("done") {
      cl
    }

  }

  /* harvesters.foreach {
      h =>
        
    }*/

  // Auto Harvesters
  // On Can define harvesters which have to be present as children of a specified type
  //----------------------

  /**
   * Each Havester key type must have one child harvester of each of the value
   */
  var autoHarvesterClasses = Map[Class[_ <: Harvester], List[Class[Harvester]]]()

  /**
   * Each harvester key type must have all value instances as children
   */
  var autoHarvesterObjects = Map[Class[_ <: Harvester], List[Harvester]]()

  def resetAutoHarvester = {
    autoHarvesterClasses = autoHarvesterClasses.empty
    autoHarvesterObjects = autoHarvesterObjects.empty
  }

  def registerAutoHarvesterClass[IT <: HarvestedResource, OT <: HarvestedResource](t: Tuple2[Class[_ <: Harvester], Class[_ <: Harvester]]): Unit = {
    registerAutoHarvesterClass(t._2, t._1)
  }

  def registerAutoHarvesterClass[IT <: HarvestedResource, OT <: HarvestedResource](parentClass: Class[_ <: Harvester], harvesterClass: Class[_ <: Harvester]): Unit = {

    autoHarvesterClasses.get(parentClass) match {
      case Some(list) =>
        autoHarvesterClasses = autoHarvesterClasses.updated(parentClass, harvesterClass.asInstanceOf[Class[Harvester]] :: list)
      case None =>
        autoHarvesterClasses = autoHarvesterClasses.updated(parentClass, List(harvesterClass.asInstanceOf[Class[Harvester]]))
    }

    updateAutoHarvester
  }

  def registerAutoHarvesterObject[IT <: HarvestedResource, OT <: HarvestedResource](parentClass: Class[_ <: Harvester], harvester: Harvester): Unit = {

    autoHarvesterObjects.get(parentClass) match {
      case Some(list) =>
        autoHarvesterObjects = autoHarvesterObjects.updated(parentClass, harvester :: list)
      case None =>
        autoHarvesterObjects = autoHarvesterObjects.updated(parentClass, List(harvester))
    }
    updateAutoHarvester

  }

  def cleanAutoHarvesters = {

    // Clean Auto Classes
    this.autoHarvesterClasses.foreach {

      // Clean Source map if source is tainted
      case (source, required) if (source.getClass.getClassLoader.isInstanceOf[ClassDomain] && source.getClass.getClassLoader.asInstanceOf[ClassDomain].tainted) =>

        //this.autoHarvesterClasses.filterKeys { k => k != source }.toMap
        this.autoHarvesterClasses = this.autoHarvesterClasses.view.filterKeys { k => k != source }.toMap
      case (source, required) =>
        var filtered = required.filter {
          case req if (req.getClassLoader.isInstanceOf[ClassDomain]) =>
            !req.getClassLoader.asInstanceOf[ClassDomain].tainted
          case _ => true
        }
        this.autoHarvesterClasses = this.autoHarvesterClasses.updated(source, filtered)

    }

    // Clean Auto Objects
    this.autoHarvesterObjects.foreach {
      case (source, required) =>
        var filtered = required.filter {
          case req if (req.getClass.getClassLoader.isInstanceOf[ClassDomain]) =>
            !req.getClass.getClassLoader.asInstanceOf[ClassDomain].tainted
          case _ => true
        }
        this.autoHarvesterObjects = this.autoHarvesterObjects.updated(source, filtered)

    }

    // Clean Harvesters
    this.onAllHarvestersDepthFirst {
      case harvester if (harvester.getClass.getClassLoader.isInstanceOf[ClassDomain] && harvester.getClass.getClassLoader.asInstanceOf[ClassDomain].tainted) =>

        // REmove from parent or top 
        harvester.clean
      /*harvester.parentHarvester match {
        case Some(parent) =>
          parent.removeChildHarvester(harvester)
        case None =>
          Harvest.removeHarvester(harvester)

      }*/

      // Keep
      case _ =>

    }

  }

  def updateAutoHarvester = {

    // Clean
    cleanAutoHarvesters

    // Update on all
    onAllHarvesters {
      harvester =>
        updateAutoHarvesterOn(harvester)

    }
  }

  def updateAutoHarvesterOn(harvester: Harvester) = {

    logFine[Harvester]("Auto Update on : " + harvester.getClass)
    var h = harvester.asInstanceOf[Harvester]
    var children = h.childHarvesters.asInstanceOf[List[Harvester]]

    // Finder:
    // - Get all objects/classes matching harvester's class or parent class
    // - Make the list distinct to avoid duplicate instantiations
    // - Add missing classtype/objects

    // Make sure the harvester has at least one type of auto classes
    //----------------
    //var requiredClasses = autoHarvesterClasses.filter { case (matchClass, objects) => matchClass.isAssignableFrom(harvester.getClass) }.values.flatten.toList.distinct
    var requiredClasses = autoHarvesterClasses.filterKeys { case matchClass => matchClass == harvester.getClass }.values.flatten.toList.distinct
    logFine[Harvester]("Required Classes : " + requiredClasses)

    requiredClasses.foreach {

      // Can't find required class in children harvesters, and required is not same as harvester, add
      //case requiredClass if (requiredClass != harvester.getClass && ((harvester.parentHarvester.isDefined && harvester.parentHarvester.get.getClass!=requiredClass) || harvester.parentHarvester.isEmpty) && children.find { ch => requiredClass.isAssignableFrom(ch.getClass) }.isEmpty) =>
      case requiredClass if (requiredClass != harvester.getClass && children.find { ch => requiredClass.isAssignableFrom(ch.getClass) }.isEmpty) =>

        try {
          harvester.addChildHarvester(requiredClass)
        } catch {
          case e: Throwable =>
            println(s"An error occured during auto update on: ${requiredClass} -> ${harvester.getClass} " + e.getLocalizedMessage)
        }
      case _ =>
    }

    // MAke sure the harvester has all the required objects
    //---------------
    var requiredObjects = autoHarvesterObjects.toIterable.collect {
      case (matchClass, objects) if (matchClass.isAssignableFrom(harvester.getClass)) => objects.toSeq
    }.flatten
    requiredObjects.foreach {
      case requiredHarvester if (!children.contains(requiredHarvester)) =>
        h.addChildHarvesterForce(requiredHarvester)
      case _ =>
    }

  }

  // Utilities
  //-------------------
  def printHarvesters = {
    this.onAllHarvestersDepthFirst { h =>

      // Tab
      var tabs = h.hierarchy(true).map { p => "----" }.mkString + ">"

      println(s"$tabs ${h.getClass.getSimpleName} " + h.getResources.size)

    }
  }

  // Utilities
  //-----------------------
  this.onInit {

    // Try to load harvesters from config
    this.config match {
      case Some(c) =>
        c.getKeys("harvester", "class").foreach {
          key =>
            key.values.headOption match {
              case Some(className) =>

                this.keepErrorsOn(this) {

                  //-- Load class
                  var cl = getClass.getClassLoader.loadClass(className)

                  //-- Instantiate and add
                  var h = cl.getDeclaredConstructor().newInstance().asInstanceOf[Harvester]
                  addHarvester(h)
                }

              case None =>
            }
        }
      case None =>
    }

  }

}