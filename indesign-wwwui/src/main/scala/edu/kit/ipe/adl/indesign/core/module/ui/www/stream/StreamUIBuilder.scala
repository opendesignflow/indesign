package edu.kit.ipe.adl.indesign.core.module.ui.www.stream

import edu.kit.ipe.adl.indesign.core.module.ui.www.external.ExternalBuilder
import edu.kit.ipe.adl.indesign.core.module.stream.StringStreamInterface

class StreamUIBuilder extends ExternalBuilder {
  
  
  def streamArea(height: Int,interface:StringStreamInterface) = {
    
    interface.onData {
      text => 
        
    }
    
  }
  
}