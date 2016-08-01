package edu.kit.ipe.adl.indesign.core.module.ui.www.edit.ace


import java.net.URI
import com.idyria.osi.vui.html.HTMLNode
import org.w3c.dom.html.HTMLElement
import edu.kit.ipe.adl.indesign.core.module.ui.www.external.ExternalBuilder

trait ACEEditorBuilder extends ExternalBuilder {
  
  override def externalAdd(targetNode: HTMLNode[HTMLElement, Any]): Unit = {

    super.externalAdd(targetNode)
    switchToNode(targetNode, {

      script(new URI(createSpecialPath("resources", "modules/wwwui/external/indesign-ace.js"))) {

      }
       stylesheet(new URI(createSpecialPath("resources", "modules/wwwui/external/indesign-webdraw-ace.css"))) {

      }
      script(new URI(createSpecialPath("resources", "modules/wwwui/external/ace/ace.js"))) {

      }
      

    })

  }
  
  def aceEditor(language:String,width:String,height:String,text:String)(cl: => Any) = {
    
    "indesign-ace-editor" :: div {
      +@("style"->s"width:$width;height:$height;position:relative")
      +@("data-language"->language)
      textContent(text)
      
      //-- Extra stuff
      cl
    }
    
  }
}