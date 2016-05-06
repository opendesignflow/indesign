package edu.kit.ipe.adl.indesign.core.heart

import com.idyria.osi.tea.timing.TimingSupport

trait HeartTimingSupport  {
  
  var timings = Map[String,Long]()
  
  
  def time[RT <: Any](id:String)(cl : => RT) : RT = {
    
   
    try {
       var  startTime = System.currentTimeMillis()
      var res = cl
      var  stopTime = System.currentTimeMillis()
      timings = timings + (id -> (stopTime-startTime))
      
      res
    } catch {
      case e : Throwable =>
        timings = timings + (id -> -1)
        throw e
    }
    
    
    
  }
  
  def timingsReset = {
    
  }
  
}