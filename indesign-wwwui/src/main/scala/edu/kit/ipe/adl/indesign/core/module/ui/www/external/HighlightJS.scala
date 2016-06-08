package edu.kit.ipe.adl.indesign.core.module.ui.www.external

import com.idyria.osi.vui.html.HTMLNode
import org.w3c.dom.html.HTMLElement
import java.net.URI

trait HighlightJSBuilder extends ExternalBuilder {
  
  override def externalAdd(targetNode: HTMLNode[HTMLElement, Any]): Unit = {
    
     super.externalAdd(targetNode)
    switchToNode(targetNode, {
      // Extra scripts
      stylesheet(new URI(createSpecialPath("resources", "highlightjs/styles/default.css"))) {
        
      }
      script(new URI(createSpecialPath("resources", "highlightjs/highlight.pack.js"))) {
        
      }
      script(new URI(createSpecialPath("resources", "indesign-highlightjs.js"))) {
        
      }

     
      
    })
   

  }
  
}