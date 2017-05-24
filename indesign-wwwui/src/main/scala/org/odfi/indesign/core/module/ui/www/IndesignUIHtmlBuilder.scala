package org.odfi.indesign.core.module.ui.www

import com.idyria.osi.wsb.webapp.localweb.DefaultLocalWebHTMLBuilder
import org.odfi.indesign.core.module.ui.www.external.ExternalBuilder
import java.net.URI
import com.idyria.osi.vui.html.HTMLNode
import org.w3c.dom.html.HTMLElement

trait IndesignUIHtmlBuilder extends ExternalBuilder {

  override def externalAdd(targetNode: HTMLNode[HTMLElement, Any]): Unit = {

    super.externalAdd(targetNode)
    switchToNode(targetNode, {

      script(new URI(createSpecialPath("resources", "modules/wwwui/indesign.js"))) {

      }

    })

  }

}