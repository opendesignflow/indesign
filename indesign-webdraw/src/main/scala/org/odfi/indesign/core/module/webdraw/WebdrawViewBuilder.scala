package org.odfi.indesign.core.module.webdraw

import org.odfi.indesign.core.module.ui.www.external.ExternalBuilder
import java.net.URI
import com.idyria.osi.vui.html.HTMLNode
import org.w3c.dom.html.HTMLElement

trait WebdrawViewBuilder extends ExternalBuilder {
  
  override def externalAdd(targetNode: HTMLNode[HTMLElement, Any]): Unit = {

    super.externalAdd(targetNode)
    switchToNode(targetNode, {

      var n = script(new URI(createSpecialPath("resources", "modules/webdraw/indesign-webdraw.js"))) {

      }
      

    })

  }
} 