package edu.kit.ipe.adl.indesign.core.harvest

import edu.kit.ipe.adl.indesign.core.brain.BrainRegion
import edu.kit.ipe.adl.indesign.core.brain.errors.ErrorSupport

object Harvest extends BrainRegion[BrainRegion[_]] {

  // Top Harvesters
  //------------------

  var harvesters = List[Harvester[_, _]]()
  def addHarvester(h: Harvester[_, _]) = {
    this.harvesters = this.harvesters :+ h
    h
  }

  /**
   * Process Depth first ordered
   */
  def onAllHarvesters(cl: Harvester[_, _] => Unit) = {

    var processList = new scala.collection.mutable.ListBuffer[Harvester[_, _]]()
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
  var autoHarvesterClasses = Map[Class[_ <: Harvester[_, _]], List[Class[Harvester[_ <: HarvestedResource, _ <: HarvestedResource]]]]()

  /**
   * Each harvester key type must have all value instances as children
   */
  var autoHarvesterObjects = Map[Class[_ <: Harvester[_, _]], List[Harvester[_ <: HarvestedResource, _ <: HarvestedResource]]]()

  def resetAutoHarvester = {
    autoHarvesterClasses = autoHarvesterClasses.empty
    autoHarvesterObjects = autoHarvesterObjects.empty
  }

  def registerAutoHarvesterClass[IT <: HarvestedResource, OT <: HarvestedResource](parentClass: Class[_ <: Harvester[IT, OT]], harvesterClass: Class[_ <: Harvester[IT, _ <: HarvestedResource]]): Unit = {

    autoHarvesterClasses.get(parentClass) match {
      case Some(list) =>
        autoHarvesterClasses = autoHarvesterClasses.updated(parentClass, list :+ harvesterClass.asInstanceOf[Class[Harvester[_ <: HarvestedResource, _ <: HarvestedResource]]])
      case None =>
        autoHarvesterClasses = autoHarvesterClasses + (parentClass -> List(harvesterClass.asInstanceOf[Class[Harvester[_ <: HarvestedResource, _ <: HarvestedResource]]]))
    }

    updateAutoHarvester
  }

  def registerAutoHarvesterObject[IT <: HarvestedResource, OT <: HarvestedResource](parentClass: Class[_ <: Harvester[IT, OT]], harvester: Harvester[IT, _ <: HarvestedResource]): Unit = {

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

      /*var h = harvester.asInstanceOf[Harvester[_,_]]
        var children = h .childHarvesters.asInstanceOf[List[Harvester[_,_]]]

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
*/
    }
  }

  def updateAutoHarvesterOn(harvester: Harvester[_, _]) = {
    var h = harvester.asInstanceOf[Harvester[_, _]]
    var children = h.childHarvesters.asInstanceOf[List[Harvester[_, _]]]

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

}