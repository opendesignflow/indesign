package edu.kit.ipe.adl.indesign.core.heart

trait HearthUtilTrait {
  
  def createHearthTask (cl: => Any ) = new DefaultHeartTask {
    
    def doTask = {
      cl
    }
  }
  
}