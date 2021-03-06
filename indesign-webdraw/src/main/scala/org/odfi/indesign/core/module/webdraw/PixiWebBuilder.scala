package org.odfi.indesign.core.module.webdraw

import java.net.URI
import com.idyria.osi.vui.html.HTMLNode
import org.w3c.dom.html.HTMLElement

trait PixiWebBuilder extends WebdrawViewBuilder {
  
 override def externalAdd(targetNode: HTMLNode[HTMLElement, Any]): Unit = {
    
     super.externalAdd(targetNode)
    switchToNode(targetNode, {

      script(new URI(createSpecialPath("resources", "modules/webdraw/external/pixi-v3/pixi.min.js"))) {
        
      }

     
      
    })
   

  }
  
}