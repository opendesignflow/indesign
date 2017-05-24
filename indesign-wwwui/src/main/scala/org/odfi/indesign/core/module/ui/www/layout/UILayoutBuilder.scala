package org.odfi.indesign.core.module.ui.www.layout

import java.net.URI
import org.odfi.indesign.core.module.ui.www.external.ExternalBuilder
import com.idyria.osi.vui.html.HTMLNode
import org.w3c.dom.html.HTMLElement

trait UILayoutBuilder extends ExternalBuilder {
  
  override def externalAdd(targetNode: HTMLNode[HTMLElement, Any]): Unit = {

    super.externalAdd(targetNode)
    switchToNode(targetNode, {

      var n = script(new URI(createSpecialPath("resources", "modules/wwwui/indesign-layout.js"))) {

      }
      

    })

  }
}