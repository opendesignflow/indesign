package org.odfi.indesign.core.module.stream

import com.idyria.osi.tea.listeners.ListeningSupport

trait StreamInterface extends ListeningSupport {
  
  
  
}

trait StringStreamInterface {
  
  def onData(cl: String => Unit) = {
    
  }
  
  def sendData(str:String)= {
    
  }
  
}