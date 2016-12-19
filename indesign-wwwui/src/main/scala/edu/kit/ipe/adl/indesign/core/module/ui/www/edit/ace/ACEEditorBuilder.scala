package edu.kit.ipe.adl.indesign.core.module.ui.www.edit.ace

import java.net.URI
import com.idyria.osi.vui.html.HTMLNode
import org.w3c.dom.html.HTMLElement
import edu.kit.ipe.adl.indesign.core.module.ui.www.external.ExternalBuilder
import com.idyria.osi.vui.html.Div
import com.idyria.osi.wsb.webapp.localweb.DefaultLocalWebHTMLBuilder

trait ACEEditorBuilder extends ExternalBuilder {

  var aceVersion = "1.2.6"

  override def externalAdd(targetNode: HTMLNode[HTMLElement, Any]): Unit = {

    super.externalAdd(targetNode)
    switchToNode(targetNode, {

      script(new URI(createSpecialPath("resources", s"modules/wwwui/external/ace/${aceVersion}/src-min-noconflict/ace.js"))) {

      }

      script(new URI(createSpecialPath("resources", "modules/wwwui/external/ace/indesign-ace.js"))) {

      }
      stylesheet(new URI(createSpecialPath("resources", "modules/wwwui/external/ace/indesign-ace.css"))) {

      }

    })

  }

  def aceEditor(language: String, width: String, height: String, text: String)(cl: => Any): Unit = {

    "indesign-ace-editor" :: div {
      +@("style" -> s"width:$width;height:$height;position:relative")
      +@("data-language" -> language)
      textContent(text)

      //-- Extra stuff
      cl
    }

  }

  def aceFastEditor(language: String, text: String)(cl: => Any): Unit = {

    var editorDiv = "indesign-ace-editor" :: div {
      +@("style" -> s"position:relative")
      +@("data-language" -> language)
      textContent(text)

      //-- Extra stuff
      cl

      //-- Save
      withActionName("editor.save") {
        //withActionData("fileContent" -> """ $(\".indesign-ace-editor\").data(\"editor\").getSession().getDocument().getValue()  """) {
        withActionData("fileContent" -> """indesign.ace.getEditorFor""") {
          onFilteredKeyTyped(""" e.ctrlKey && e.key=='s' """) {
            x =>
              //request
              println("Received CTRL+S")
          }
        }
      }

    }

    new AceEditor(editorDiv)

  }
}

class AceEditor(val node: Div[HTMLElement, Any]) extends DefaultLocalWebHTMLBuilder {

  def onSave(cl: String => Unit) = {

    //-- Add CTRL+S Save
    this.onNode(node) {

    }

  }

}