package org.odfi.indesign.core.harvest

import scala.reflect.ClassTag

import org.odfi.tea.errors.ErrorSupport
import org.odfi.tea.listeners.ListeningSupport
import org.odfi.tea.logging.TLogSource

import org.odfi.indesign.core.brain.LFCDefinition
import org.odfi.indesign.core.brain.LFCSupport
import org.odfi.tea.compile.ClassDomain
import org.odfi.indesign.core.heart.HeartTask
import org.odfi.indesign.core.heart.ResourceTask
import org.odfi.indesign.core.heart.Heart
import org.odfi.indesign.core.heart.ResourceTask

trait HarvestedResource extends ListeningSupport with LFCSupport with ErrorSupport with TLogSource {

  // Taint Check
  def isTainted = this.getClass.getClassLoader.isInstanceOf[ClassDomain] && this.getClass.getClassLoader.asInstanceOf[ClassDomain].tainted

  /**
   * ID used for cleanup
   */
  def getId: String

  /**
   * Display name for gui, should be not too long
   */
  def getDisplayName = getId

  /**
   * A rooted resource won't be deleted after harvesting and cleaning up the actual resources with the new resources found
   */
  var rooted = false

  def root = {
    rooted = true
    this
  }

  def unroot = {
    rooted = false
    true
  }

  /**
   * If local, a resource won't be propagated to children
   */
  var local = false

  // Harvester Relatio
  //----------------
  var originalHarvester: Option[Harvester] = None

  // Parenting and Derived Resources
  //-------------------

  var parentResource: Option[HarvestedResource] = None
  var derivedResources = Map[String, HarvestedResource]()

  /**
   * If derived from a resource, it becomes this resources parent
   * returns this
   */
  def deriveFrom(parentResource: HarvestedResource): HarvestedResource = {

    this.parentResource = Some(parentResource)
    parentResource.addDerivedResource(this)
    triggerParentResourceAdded

    /*
    this.parentResource match {
      case Some(p) =>
      case None =>
        this.parentResource = Some(parentResource)
        parentResource.addDerivedResource(this)
      /* this.onAdded {
          case _ =>
            println(s"*** Resource gathered : $this , time to set derived resource to $parentResource *****")
            
        }*/
    }*/

    this
  }

  def triggerParentResourceAdded = {
    this.@->("parent.resource.added")
  }

  def onParentResourceAdded(cl: => Unit) = {
    this.on("parent.resource.added")(cl)
  }

  def addDerivedResources[RT <: HarvestedResource](r: Iterable[RT]): Unit = {
    r.foreach(addDerivedResource(_))
  }

  /**
   * Add a Resource as derived
   * If same type and same id already exist; don't add an return the existing resource
   *
   */
  def addDerivedResource[RT <: HarvestedResource](r: RT): RT = {

    // Add to local list, but not if already there
    derivedResources.get(r.getId) match {
      case Some(res) =>
        logWarn[HarvestedResource]("Readding derived resource of ID: " + r.getId + s", actual resource is ${res.getClass}, trying to add ${r.getClass}")
        res.asInstanceOf[RT]
      case None =>
        derivedResources = derivedResources.updated(r.getId, r)

        r.parentResource match {
          case Some(p) =>
          case None =>
            r.parentResource = Some(this)
        }

        // Make sure we are the parent
        //r.deriveFrom(this)
        r
    }

  }

  def removeDerivedResource[RT <: HarvestedResource](r: RT)(implicit tag: ClassTag[RT]): RT = {

    derivedResources.get(r.getId) match {
      case Some(res) if (tag.runtimeClass.isInstance(res)) =>
        res.parentResource = None
        derivedResources = derivedResources - r.getId
        res.asInstanceOf[RT]
      case _ =>
        r
    }

  }

  def cleanDerivedResources: Unit = {

    //println("Cleaning Derived Resources")

    // Clean List 
    val toClean = this.derivedResources
    this.derivedResources = this.derivedResources.empty

    // Remove Parent reference from derived resources
    toClean.foreach {
      case dr if (dr != this) =>
        //println(s"Clean: "+dr)
        dr._2.clean
        dr._2.parentResource = None
      case other =>

    }

  }

  def cleanDerivedResource(r: HarvestedResource) = {

    //-- Remove rparent link if it is ithis
    r.parentResource match {
      case Some(p) if (p == this) =>
        r.parentResource = None
      case other =>
    }

    //-- Remove
    // println(s"Removing: " + r + " -> " + this.derivedResources.size)

    this.derivedResources = this.derivedResources.view.filter {
      case (id, k) => k != r
    }.toMap
    // println(s"Removed: " + r + " -> " + this.derivedResources.size)

    //-- Clean 
    r.clean
  }

  def cleanDerivedResourcesOfType[RT <: HarvestedResource](implicit tag: ClassTag[RT]) = {

    this.getDerivedResources[RT].foreach(cleanDerivedResource(_))
  }

  def findUpchainResource[CT <: HarvestedResource](implicit tag: ClassTag[CT]): Option[CT] = {

    tag.runtimeClass.isInstance(this) match {
      case true =>
        Some(this.asInstanceOf[CT])
      case false =>
        var currentParent = this.parentResource
        var stop = false
        while (!stop && currentParent.isDefined) {

          tag.runtimeClass.isInstance(currentParent.get) match {
            case true =>
              stop = true
            case false =>
              currentParent = currentParent.get.parentResource
          }

        }

        currentParent match {
          case Some(res) => Some(res.asInstanceOf[CT])
          case None => None
        }
    }

  }

  /**
   * Finds first up chain resource of type and matching criteria, not including current resource
   */
  def findUpchainResourceAnd[CT <: HarvestedResource](cl: CT => Boolean)(implicit tag: ClassTag[CT]): Option[CT] = {


    var currentParent = this.parentResource
    var stop = false
    while (!stop && currentParent.isDefined) {

      tag.runtimeClass.isInstance(currentParent.get) match {
        case true if (cl(currentParent.get.asInstanceOf[CT])) =>
          stop = true
        case other =>
          currentParent = currentParent.get.parentResource
      }

    }

    currentParent match {
      case Some(res) => Some(res.asInstanceOf[CT])
      case None => None
    }


  }

  /**
   * Find the top most resource of type in chain, not returning the actual resource
   */
  def findTopMostResource[CT <: HarvestedResource](implicit tag: ClassTag[CT]): Option[CT] = {

    /* tag.runtimeClass.isInstance(this) match {
       case true =>
         Some(this.asInstanceOf[CT])
       case false =>*/
    var currentParent = this.parentResource
    var lastFound: Option[CT] = None
    while (currentParent.isDefined) {

      tag.runtimeClass.isInstance(currentParent.get) match {
        case true =>
          lastFound = Some(currentParent.get.asInstanceOf[CT])
          currentParent = currentParent.get.parentResource
        case false =>
          currentParent = currentParent.get.parentResource
      }

    }

    lastFound
    //}

  }

  /**
   * Maps resources upchain
   */
  def mapUpchainResources[T](cl: HarvestedResource => T): List[T] = {

    var res = scala.collection.mutable.ArrayBuffer[T]()
    var currentParent = this.parentResource
    while (currentParent.isDefined) {

      //-- Closure
      res += cl(currentParent.get)

      //-- Next
      currentParent = currentParent.get.parentResource

    }

    res.toList
  }

  /**
   * Maps parent resources matching Type
   */
  def mapUpResources[RT <: HarvestedResource, T](cl: RT => T)(implicit tag: ClassTag[RT]): List[T] = {

    var res = scala.collection.mutable.ArrayBuffer[T]()
    var currentParent = this.parentResource
    while (currentParent.isDefined) {

      //-- Closure
      tag.runtimeClass.isInstance(currentParent.get) match {
        case true =>
          res += cl(currentParent.get.asInstanceOf[RT])
        case false =>

      }

      //-- Next
      currentParent = currentParent.get.parentResource

    }

    res.toList
  }

  def onDerivedResources[CT <: HarvestedResource](cl: PartialFunction[CT, Unit])(implicit tag: ClassTag[CT]): Unit = {
    // println(s"onDerivedResources on $this which has: ${derivedResources.size}")
    this.derivedResources.foreach {
      case (id, r) if (tag.runtimeClass.isInstance(r) && cl.isDefinedAt(r.asInstanceOf[CT])) =>
        cl(r.asInstanceOf[CT])
      case (id, r) =>
        // println(s"Recursino on $r which has: ${r.derivedResources.size}")
        r.onDerivedResources[CT](cl)
    }

  }

  def getDerivedResources[CT <: HarvestedResource](implicit tag: ClassTag[CT]) = {

    /*this.derivedResources.foreach {
      case (id,r) => 
        
        //println(s""" DRST ${tag.runtimeClass} against ${r.getClass} """)
    }*/

    this.derivedResources.toList.collect {
      case ((id, r)) if (tag.runtimeClass.isInstance(r)) =>
        r.asInstanceOf[CT]

    }.toList
  }

  /**
   * If no resourced of type, use closure to build and save
   */
  def getDerivedResourcesOrElseSave[CT <: HarvestedResource](b: => Iterable[CT])(implicit tag: ClassTag[CT]) = {
    getDerivedResources[CT] match {
      case r if (r.size == 0) =>
        val res = b
        this.addDerivedResources(res)
        res.toList
      case other => other.toList
    }
  }

  /**
   * Get Derived Resources Recursively
   */
  def getSubDerivedResources[CT <: HarvestedResource](implicit tag: ClassTag[CT]) = {
    this.derivedResources.toIterable.map {
      case (id, r) if (tag.runtimeClass.isInstance(r)) =>
        List(r.asInstanceOf[CT])
      case (id, r) =>
        r.getDerivedResources[CT]

    }.flatten
  }

  def hasDerivedResourceOfType[CT <: HarvestedResource](implicit tag: ClassTag[CT]): Boolean = {

    this.derivedResources.find {
      case (id, r) if (tag.runtimeClass.isInstance(r)) =>
        true
      case (id, r) =>
        // println(s"Recursino on $r which has: ${r.derivedResources.size}")
        r.hasDerivedResourceOfType[CT]
    }.isDefined

    //this.derivedResources.find{case (id,r) if (tag.runtimeClass.isInstance(r))=>true;case _ => false }.isDefined
  }

  /**
   * Recursive!!
   */
  def findDerivedResourceOfType[CT <: HarvestedResource](implicit tag: ClassTag[CT]): Option[CT] = {
    this.derivedResources.collectFirst {
      case (id, r) if (tag.runtimeClass.isInstance(r)) =>
        r.asInstanceOf[CT]
      case (id, r) if (r.hasDerivedResourceOfType[CT]) =>
        // println(s"Recursino on $r which has: ${r.derivedResources.size}")
        r.findDerivedResourceOfType[CT].get
    }
  }

  /**
   * Not Recursive!!
   */
  def findDerivedResourceOfTypeAnd[CT <: HarvestedResource](cl: CT => Boolean)(implicit tag: ClassTag[CT]): Option[CT] = {
    this.derivedResources.collectFirst {
      case (id, r) if (tag.runtimeClass.isInstance(r) && cl(r.asInstanceOf[CT])) =>
        r.asInstanceOf[CT]

    }
  }

  // Lifecycle management
  //-------------------
  def onAdded(cl: PartialFunction[Harvester, Unit]) = {
    this.onWith("added") {
      (h: Harvester) =>
        cl.isDefinedAt(h) match {
          case true => keepErrorsOn(this) {
            cl(h)
          }
          case false =>
        }
    }
  }

  def onGathered(cl: PartialFunction[Harvester, Unit]) = {
    this.onWith[Harvester]("gathered") {
      (h: Harvester) =>
        //println("Running a gathered even on "+this)
        cl.isDefinedAt(h) match {
          case true =>
            // println("Ev defined")
            cl(h)
          //keepErrorsOn(this){cl(h)}
          case false =>
          //println("Ev not defined")
        }
    }

  }

  def onGatheredBy(h: Harvester)(cl: => Unit) = {
    this.onGathered {
      case gh if (gh == h) => cl
    }

  }

  /**
   * Clean recursively
   */
  def clean: Unit = {

    //println(s"Cleaning: " + this)

    this.originalHarvester match {
      case Some(h) =>
        h.availableResources.clear()
        h.availableResources.addAll(this.originalHarvester.get.availableResources.filterNot(_==this))
        this.originalHarvester = None
      //  println(s"Removing " + this + " from original harvester: " + h)
      case None =>
    }
    this.@->("clean")

    this.cleanDerivedResources
  }

  def onClean(cl: => Any): Unit = {
    this.on("clean") {
      cl
    }
  }

  def onClean(cl: PartialFunction[Harvester, Unit]): Unit = onCleaned(cl)

  def onCleaned(cl: PartialFunction[Harvester, Unit]): Unit = {
    this.onWith("clean") {
      (h: Harvester) =>
        cl.isDefinedAt(h) match {
          case true => keepErrorsOn(this) {
            cl(h)
          }
          case false =>
        }
    }
  }

  def onKept(cl: PartialFunction[Harvester, Unit]) = {
    this.onWith("kept") {
      (h: Harvester) =>
        cl.isDefinedAt(h) match {
          case true => keepErrorsOn(this) {
            cl(h)
          }
          case false =>
        }
    }
  }

  def onProcess(cl: => Unit) = {
    this.registerStateHandler("processed") {
      cl
    }
  }

  /**
   * Run the Direct process
   */
  def runDirectProcess = {
    this.keepErrorsOn(this, verbose = true) {

      HarvestedResource.moveToState(this, "processed")
    }
  }

  def runProcessResource = {

    runSingleTask("process") {
      this.keepErrorsOn(this, verbose = true) {

        HarvestedResource.moveToState(this, "processed")
      }
    }

  }

  // Naming Utils
  //------------
  def resourceHierarchyName(sep: String = ".", withoutSelf: Boolean = false) = {

    resourceHierarchy(withoutSelf).map(h => h.getClass.getSimpleName.replace("$", "") + "-" + h.hashCode()).mkString(sep)

  }

  def resourceHierarchy(withoutSelf: Boolean = false) = {

    // Get Parent Line
    var parents = withoutSelf match {
      case true => List[HarvestedResource]()
      case false => List[HarvestedResource](this)
    }
    var currentParent: Option[HarvestedResource] = this.parentResource
    while (currentParent != None) {
      parents = currentParent.get :: parents
      currentParent = currentParent.get.parentResource
    }

    // Return Reversed to have top most parent first
    parents.reverse

  }

  // Tasking
  //----------------

  def runSingleTask(id: String)(cl: => Any) = {

    var task = new ResourceTask(id, this) {
      def doTask = {
        cl
      }
    }

    Heart.pump(task)

    task

  }

}

trait HarvestedResourceDefaultId extends HarvestedResource {
  def getId = getClass.getCanonicalName + ":" + getClass.hashCode()
}

object HarvestedResource extends LFCDefinition {

  this.defineState("new")
  this.defineState("processed")

}