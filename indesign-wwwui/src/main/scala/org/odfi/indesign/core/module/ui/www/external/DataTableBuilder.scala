package org.odfi.indesign.core.module.ui.www.external

import com.idyria.osi.vui.html.HTMLNode
import org.w3c.dom.html.HTMLElement
import java.net.URI
 
trait DataTableBuilder extends ExternalBuilder {
  
  override def externalAdd(targetNode: HTMLNode[HTMLElement, Any]): Unit = {
    
     super.externalAdd(targetNode)
    switchToNode(targetNode, {
      // Extra scripts
      stylesheet(new URI(createSpecialPath("resources", "modules/wwwui/external/datatables/datatables.min.css"))) {
        
      }
      script(new URI(createSpecialPath("resources", "modules/wwwui/external/datatables/datatables.min.js"))) {
        
      }
       script(new URI(createSpecialPath("resources", "modules/wwwui/external/datatables/indesign-datatables.js"))) {
        
      }
      
    })
   

  }
  
}