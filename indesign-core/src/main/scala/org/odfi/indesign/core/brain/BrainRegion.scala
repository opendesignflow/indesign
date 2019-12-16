package org.odfi.indesign.core.brain

import org.odfi.tea.errors.ErrorSupport
import org.odfi.tea.logging.TLogSource
import org.odfi.indesign.core.harvest.HarvestedResource
import org.odfi.tea.compile.ClassDomain
import org.odfi.indesign.core.config.ConfigSupport

trait BrainRegion extends BrainLifecycle  with ErrorSupport with TLogSource with HarvestedResource  {
  
  
 
  
  // Parent / Child
  //--------------
  /*var parentRegion : Option[BrainRegion] = None
  
  var subRegions = List[BrainRegion]()
  
  def addSubRegion[ST <: BrainRegion](sr:ST) = {
    this.subRegions = this.subRegions :+ sr
    sr.parentRegion = Some(sr)
  }
  */
  
  /**
   * Return a simple name
   */
  def getName = getClass.getSimpleName.replace("$","")
  override def getId = getClass.getCanonicalName
  
  // Lifecycle Management
  //--------------------
  def kill = {
   this.moveToShutdown
    Brain
  }
  
  //-- Clean
  this.onCleaned {
    case h if (h==Brain) => 
      this.moveToShutdown
  }
  
  //-- On Gather; make sure we are at same state as brain/parent
 /* this.onGathered {
    case h if (h==Brain)  => 
      logFine[Brain](s"Region $this gathered, moving to latest state") 
      this.parentResource match {
        case Some(pr : BrainRegion) if(pr.currentState!=None) =>
          Brain.moveToState(this, pr.currentState.get)
          
        case _ if(Brain.currentState!=None) =>
           Brain.moveToState(this, Brain.currentState.get)
        case _ => 
      }
  }*/
  
  // Lifecycle
  //-----------------
  this.onSetup {
    this.onDerivedResources[BrainRegion]{case r => r.keepErrorsOn(r)(Brain.moveToState(r, "setup"))}
  }
  this.onLoad {
    this.onDerivedResources[BrainRegion]{case r =>  r.keepErrorsOn(r)(Brain.moveToState(r, "load"))}
  }
  this.onInit {
    this.onDerivedResources[BrainRegion]{case r =>  r.keepErrorsOn(r)(Brain.moveToState(r, "init"))}
  }
  this.onStart {
    this.onDerivedResources[BrainRegion]{case r =>  r.keepErrorsOn(r)(Brain.moveToState(r, "start"))}
  }
  this.onStop {
   this.onDerivedResources[BrainRegion]{case r =>  r.keepErrorsOn(r)(Brain.moveToState(r, "stop"))}
  }
  this.onShutdown{
    this.onDerivedResources[BrainRegion]{case r =>  r.keepErrorsOn(r)(Brain.moveToState(r, "shutdown"))}
  }
  this.onResetState {
    this.onDerivedResources[BrainRegion]{case r =>  r.keepErrorsOn(r)(Brain.resetLFCState(r))}
  }
  
}

