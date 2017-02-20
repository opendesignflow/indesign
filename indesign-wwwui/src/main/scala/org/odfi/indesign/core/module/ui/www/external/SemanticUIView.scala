package org.odfi.indesign.core.module.ui.www.external

import org.w3c.dom.html.HTMLElement

import com.idyria.osi.vui.html.HTMLNode
import com.idyria.osi.vui.html.Head
import com.idyria.osi.vui.html.Html
import com.idyria.osi.wsb.webapp.localweb.DefaultLocalWebHTMLBuilder
import com.idyria.osi.wsb.webapp.localweb.LocalWebHTMLVIew
import com.idyria.osi.vui.html.basic.DefaultBasicHTMLBuilder._
import org.odfi.indesign.core.module.ui.www.IndesignUIView
import java.net.URI

trait SemanticUIView extends ExternalBuilder {

  var semanticUIVersion = "2.1.4"
  var wwwuiBasePath = "modules/wwwui/"
  var semanticBasePath = s"$wwwuiBasePath/external/semantic"

  override def externalAdd(targetNode: HTMLNode[HTMLElement, Any]): Unit = {
    super.externalAdd(targetNode)

    switchToNode(targetNode, {
      stylesheet(new URI(createSpecialPath("resources", s"$semanticBasePath/semantic.min.css"))) {

      }
      script(new URI(createSpecialPath("resources", s"$semanticBasePath/semantic.min.js"))) {

      }
      script(new URI(createSpecialPath("resources", s"$wwwuiBasePath/indesign-semantic.js"))) {

      }
    })

  }

}