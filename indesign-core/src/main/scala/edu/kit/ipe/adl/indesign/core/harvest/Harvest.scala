package edu.kit.ipe.adl.indesign.core.harvest

import edu.kit.ipe.adl.indesign.core.brain.BrainRegion
import com.idyria.osi.tea.errors.ErrorSupport
import scala.reflect.ClassTag

object Harvest extends BrainRegion[BrainRegion[_]] {

  // Top Harvesters
  //------------------

  var harvesters = List[Harvester]()
  def addHarvester(h: Harvester) = {
    this.harvesters = this.harvesters :+ h
    h
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
        processList.pushAll( h.childHarvesters)
      }
    }

  }

  def onHarvesters[CT <: Harvester](cl: PartialFunction[CT, Unit])(implicit tag: ClassTag[CT]): Unit = {

    var processList = new scala.collection.mutable.ListBuffer[Harvester]()
    processList ++= this.harvesters

    while (processList.nonEmpty) {

      var h = processList.head
      processList -= h

      // Check type and PF definition
      h match {
        case h if (tag.runtimeClass.isInstance(h) && cl.isDefinedAt(h.asInstanceOf[CT])) =>

          cl(h.asInstanceOf[CT])

        case _ =>
      }

      processList ++= h.childHarvesters

    }

  }

  def run = {

    // Harvest
    //---------------
    this.onAllHarvesters {
      h =>
        keepErrorsOn(h) {
          h.resetErrors
          //println(s"********** harvest on ${h.getClass.getCanonicalName} **************");
          h.harvest
        }
    }

    // Process
    //-------------------
    this.onAllHarvesters {
      h =>
        keepErrorsOn(h) {
          h.resetErrors
          //println(s"********** harvest on ${h.getClass.getCanonicalName} **************");
          h.processResources
        }
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

  def registerAutoHarvesterClass[IT <: HarvestedResource, OT <: HarvestedResource](parentClass: Class[_ <: Harvester], harvesterClass: Class[_ <: Harvester]): Unit = {

    autoHarvesterClasses.get(parentClass) match {
      case Some(list) =>
        autoHarvesterClasses = autoHarvesterClasses.updated(parentClass, list :+ harvesterClass.asInstanceOf[Class[Harvester]])
      case None =>
        autoHarvesterClasses = autoHarvesterClasses + (parentClass -> List(harvesterClass.asInstanceOf[Class[Harvester]]))
    }

    updateAutoHarvester
  }

  def registerAutoHarvesterObject[IT <: HarvestedResource, OT <: HarvestedResource](parentClass: Class[_ <: Harvester], harvester: Harvester): Unit = {

    autoHarvesterObjects.get(parentClass) match {
      case Some(list) =>
        autoHarvesterObjects = autoHarvesterObjects.updated(parentClass, list :+ harvester)
      case None =>
        autoHarvesterObjects = autoHarvesterObjects.updated(parentClass, List(harvester))
    }
    updateAutoHarvester

  }

  def updateAutoHarvester = {
    onAllHarvesters {
      harvester =>
        updateAutoHarvesterOn(harvester)

    }
  }

  def updateAutoHarvesterOn(harvester: Harvester) = {
    var h = harvester.asInstanceOf[Harvester]
    var children = h.childHarvesters.asInstanceOf[List[Harvester]]

    // Make sure the harvester has at least one type of auto classes
    //----------------
    autoHarvesterClasses.get(harvester.getClass) match {
      case Some(autoHarvesterClasses) =>
        autoHarvesterClasses.filter {
          requiredClass: Class[_] =>
            children.find {
              ch =>
                ch.getClass == requiredClass
            } == None
        }.foreach {
          req =>
            harvester.addChildHarvester(req)
        }
      case None =>
    }

    // MAke sure the harvester has all the required objects
    //---------------
    autoHarvesterObjects.get(harvester.getClass) match {
      case Some(autoHarvesterObjects) =>
        autoHarvesterObjects.filterNot(obj => children.contains(obj)).foreach {
          requiredHarvester =>

            h.addChildHarvesterForce(requiredHarvester)
        }
      case None =>
    }
  }

  // Utilities
  //-------------------
  def printHarvesters = {
    this.onAllHarvestersDepthFirst { h =>

      // Tab
      var tabs = h.hierarchy(true).map { p => "----"}.mkString +">"
      
      println(s"$tabs ${h.getClass.getSimpleName} "+h.getResources.size)
      
    }
  }

}