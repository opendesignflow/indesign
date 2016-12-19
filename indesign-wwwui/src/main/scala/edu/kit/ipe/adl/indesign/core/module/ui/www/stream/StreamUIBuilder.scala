package edu.kit.ipe.adl.indesign.core.module.ui.www.stream

import edu.kit.ipe.adl.indesign.core.module.ui.www.external.ExternalBuilder
import edu.kit.ipe.adl.indesign.core.module.stream.StringStreamInterface
import com.idyria.osi.vui.html.HTMLNode
import org.w3c.dom.html.HTMLElement
import java.net.URI

trait StreamUIBuilder extends ExternalBuilder {

  override def externalAdd(targetNode: HTMLNode[HTMLElement, Any]) = {
    super.externalAdd(targetNode)
    switchToNode(targetNode, {
      script(new URI(this.createSpecialPath("resources", "/modules/wwwui/stream/stream.js"))) {

      }
      stylesheet(new URI(this.createSpecialPath("resources", "/modules/wwwui/stream/stream.css"))) {

      }
    })

  }

  /*def streamArea(height: Int,interface:StringStreamInterface) = {
    
    
    
    interface.onData {
      text => 
        
    }
    
  }*/
  
  def sendStreamCreate(tid:String,name:String) = {
    var createMessage = StreamCreate()
    createMessage.name = name
    createMessage.ID = tid
    this.sendBackendMessage(createMessage)
  }

  def sendUpdateStreamText(tid: String, utext: String) = {

    var updateMessage = StreamUpdate()
    updateMessage.ID = tid
    updateMessage.text = utext

    updateMessage.line = utext.last match {
      case '\n' => true
      case '\r' => true
      case _ => false
    }

    this.sendBackendMessage(updateMessage)

  }
  
  def sendStreamParameter(tid:String,parameter:String,value:String) = {
    var parameterMessage = StreamParameter()
    parameterMessage.name = parameter
    parameterMessage.ID = tid
    parameterMessage.value = value
    this.sendBackendMessage(parameterMessage)
  }

  def textStreamArea(tid: String)(cl: => Any) = {
    textarea {
      id("stream-" + tid)
      classes("stream-area")
      cl
    }
  }
  
  def divStreamArea(tid: String)(cl: => Any) = {
    div {
      +@("contenteditable" -> "true")
      id("stream-" + tid)
      classes("stream-area")
      cl
    }
  }

}