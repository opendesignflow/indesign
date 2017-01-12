package org.odfi.indesign.core.module.ui.www.external

import com.idyria.osi.vui.html.HTMLNode
import org.w3c.dom.html.HTMLElement
import java.net.URI

trait HighlightJSBuilder extends ExternalBuilder {
  
  override def externalAdd(targetNode: HTMLNode[HTMLElement, Any]): Unit = {
    
     super.externalAdd(targetNode)
    switchToNode(targetNode, {
      // Extra scripts
      stylesheet(new URI(createSpecialPath("resources", "modules/external/highlightjs/styles/default.css"))) {
        
      }
      script(new URI(createSpecialPath("resources", "modules/external/highlightjs/highlight.pack.js"))) {
        
      }
      script(new URI(createSpecialPath("resources", "modules/external/indesign-highlightjs.js"))) {
        
      }

     
      
    })
   

  }
  
}