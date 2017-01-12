package org.odfi.indesign.core.module.webdraw.superchart

import com.idyria.osi.wsb.webapp.localweb.DefaultLocalWebHTMLBuilder
import com.idyria.osi.vui.html.basic.DefaultBasicHTMLBuilder._
import java.net.URI
import org.odfi.indesign.core.module.ui.www.IndesignUIView
import com.idyria.osi.vui.html.HTMLNode
import com.idyria.osi.tea.compile.ClassDomainSupport
import com.idyria.osi.wsb.webapp.localweb.LocalWebHTMLVIew
import org.odfi.indesign.core.module.ui.www.external.ExternalBuilder
import org.w3c.dom.html.HTMLElement

trait SuperChartBuilder extends ExternalBuilder {

  override def externalAdd(targetNode: HTMLNode[HTMLElement, Any]): Unit = {

    super.externalAdd(targetNode)
    switchToNode(targetNode, {

     

      stylesheet(getViewResourcePath("modules/webdraw/superchart/jqplot/jquery.jqplot.css")) {

      }
      script(getViewResourcePath("modules/webdraw/superchart/jqplot/jquery.jqplot.min.js")) {

      }
      List("dateAxisRenderer", "cursor", "highlighter").foreach {
        pn =>
          script(getViewResourcePath(s"modules/webdraw/superchart/jqplot/plugins/jqplot.${pn}.min.js")) {

          }

      }
      
       var n = script(new URI(createSpecialPath("resources", "modules/webdraw/superchart/superchart.js"))) {

      }

      stylesheet(new URI(createSpecialPath("resources", "modules/webdraw/superchart/superchart.css"))) {

      }

    })

  }

  /*override def head(cl: => Any) = {

    // Create if necessary 
    var node = super.head(cl)

    // Make sure superchart JS is as script
    switchToNode(node, {

      script(new URI(this.viewPath + "/resources/superchart.js".noDoubleSlash)) {

      }
      stylesheet(new URI(this.viewPath + "/resources/superchart.css".noDoubleSlash)) {

      }

    })

    node

  }*/

  def superChart(chartId: String) {
    div {
      id("superchart-" + chartId)
    }
  }

}

trait SuperChartView extends IndesignUIView with ClassDomainSupport with SuperChartBuilder {

  /*override def render: com.idyria.osi.vui.html.HTMLNode[org.w3c.dom.html.HTMLElement, com.idyria.osi.vui.html.HTMLNode[org.w3c.dom.html.HTMLElement, _]] = {

    var result = super.render

    // Make sure superchart is
    println(s"Add superchart to div")

    var topview = getTopParentView

    switchToNode(result, {
      

      stylesheet(getViewResourcePath("jqplot/jquery.jqplot.css")) {

      }
      script(getViewResourcePath("jqplot/jquery.jqplot.min.js")) {

      }
      List("dateAxisRenderer", "cursor", "highlighter").foreach {
        pn =>
          script(getViewResourcePath(s"jqplot/plugins/jqplot.${pn}.min.js")) {

          }

      }
      script(getViewResourcePath("superchart.js")) {

      }
      stylesheet(getViewResourcePath("superchart.css")) {

      }

    })

    result

  }*/

}