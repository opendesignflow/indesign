package edu.kit.ipe.adl.indesign.core.module.webdraw

import java.net.URI
import com.idyria.osi.vui.html.HTMLNode
import org.w3c.dom.html.HTMLElement

trait PixiWebBuilder extends WebdrawViewBuilder {
  
 override def externalAdd(targetNode: HTMLNode[HTMLElement, Any]): Unit = {
    
     super.externalAdd(targetNode)
    switchToNode(targetNode, {

      script(new URI(createSpecialPath("resources", "modules/webdraw/pixi-v3/pixi.min.js"))) {
        
      }

     
      
    })
   

  }
  
}