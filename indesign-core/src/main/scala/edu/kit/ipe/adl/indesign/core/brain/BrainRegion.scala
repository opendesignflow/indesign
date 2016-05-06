package edu.kit.ipe.adl.indesign.core.brain

import com.idyria.osi.tea.errors.ErrorSupport
import com.idyria.osi.tea.logging.TLogSource

trait BrainRegion[CT <: BrainRegion[_]] extends BrainLifecycle  with ErrorSupport with TLogSource {
  
  // Parent / Child
  //--------------
  var parentRegion : Option[BrainRegion[_]] = None
  
  var subRegions = List[CT]()
  
  def addSubRegion[ST <: CT](sr:ST) = {
    this.subRegions = this.subRegions :+ sr
    sr.parentRegion = Some(sr)
  }
  
  
  /**
   * Return a simple name
   */
  def name = getClass.getSimpleName.replace("$","")
  
  // Lifecycle Management
  //--------------------
  def kill = {
    Brain.moveToState(this, "shutdown")
    Brain
  }
  
}

trait SingleBrainRegion extends BrainRegion[BrainRegion[_ <: BrainRegion[_]]] {
  
}

