package edu.kit.ipe.adl.indesign.core.module.ui.www.external

import org.w3c.dom.html.HTMLElement

import com.idyria.osi.vui.html.HTMLNode
import com.idyria.osi.vui.html.Head
import com.idyria.osi.vui.html.Html
import com.idyria.osi.wsb.webapp.localweb.DefaultLocalWebHTMLBuilder
import com.idyria.osi.wsb.webapp.localweb.LocalWebHTMLVIew
import com.idyria.osi.vui.html.basic.DefaultBasicHTMLBuilder._
import edu.kit.ipe.adl.indesign.core.module.ui.www.IndesignUIView
import java.net.URI

trait SemanticUIView extends LocalWebHTMLVIew with DefaultLocalWebHTMLBuilder {

  override def render: HTMLNode[HTMLElement, HTMLNode[HTMLElement, _]] = {

    // Let Main Rendereing Chain happen
    var result = super.render

    // Add Scripts/Stylesheet depending on result
    var targetNode = result match {

      // HTML: Look for Head; if none; add to result node
      case h: Html[_, _] =>

        h.children.find {
          case n if (classOf[Head[HTMLElement, _]].isInstance(n)) => true
          case _ => false
        } match {
          case Some(head) => head.asInstanceOf[Head[HTMLElement, _]]
          case None => result
        }

      // Others: Add to result node
      case _ => result

    }

    // ADd scripts
    switchToNode(targetNode, {
      
      stylesheet(new URI(s"${viewPath}/resources/semantic/semantic.min.css".noDoubleSlash)) {

      }
  
      script(new URI(s"${viewPath}/resources/semantic/semantic.min.js".noDoubleSlash)) {

      }
    })

    // Return
    result
  }

}