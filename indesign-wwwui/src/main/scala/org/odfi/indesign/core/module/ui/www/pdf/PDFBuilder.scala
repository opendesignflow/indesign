package org.odfi.indesign.core.module.ui.www.pdf

import org.odfi.indesign.core.module.ui.www.external.ExternalBuilder
import com.idyria.osi.vui.html.HTMLNode
import org.w3c.dom.html.HTMLElement
import java.net.URI

trait PDFBuilder extends ExternalBuilder {

  // Current Page
  //----------------
  var pdfPage = 1

  def pdfCanvas(url: String, id: String = "canvas") = {
   
    var c = canvas {

      this.id("pdfjs-" + id)
      +@("data-url" -> url)
      +@("page" -> pdfPage.toString)
      /*+@("width" -> "600")
      +@("height" -> "300")*/

    }
    this.registerAction("pdfjs.updatePage")(c) {
      n => 
        println(s"Updating Page "+request.get.getURLParameter("page"))
        pdfPage = request.get.getURLParameter("page").get.toInt
    }
    c
   
  }

  override def externalAdd(targetNode: HTMLNode[HTMLElement, Any]): Unit = {
    
    super.externalAdd(targetNode)
    switchToNode(targetNode, {
      // Extra scripts
      // Extra scripts
     
      stylesheet(new URI(createSpecialPath("resources", "pdfjs/web/locale/locale.properties"))) {
        +@("rel"->"resource")
        +@("type"->"application/l10n")
      }
      script(new URI(createSpecialPath("resources", "pdfjs/web/l10n.js"))) {
        
      }
      script(new URI(createSpecialPath("resources", "pdfjs/web/compatibility.js"))) {
        
      }
      script(new URI(createSpecialPath("resources", "pdfjs/build/pdf.js"))) {
        
      }
      script(new URI(createSpecialPath("resources", "pdfjs/indesign-pdfjs.js"))) {
        
      }
      /*$(<link rel="resource" type="application/l10n" href="/resources/pdfjs/web/locale/locale.properties"/>)
      $(<script src="/resources/pdfjs/web/l10n.js"></script>)
      $(<script src="/resources/pdfjs/web/compatibility.js"></script>)
      $(<script src="/resources/pdfjs/build/pdf.js"></script>)
      $(<script src="/resources/pdfjs/indesign-pdfjs.js"></script>)*/
    })
    //super.externalAdd(targetNode)

  }

}