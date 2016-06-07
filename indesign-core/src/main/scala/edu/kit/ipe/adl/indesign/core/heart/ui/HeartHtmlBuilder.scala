package edu.kit.ipe.adl.indesign.core.heart.ui

import java.net.URI

import org.w3c.dom.html.HTMLElement

import com.idyria.osi.vui.html.HTMLNode

import edu.kit.ipe.adl.indesign.core.heart.HeartTask
import edu.kit.ipe.adl.indesign.core.module.ui.www.IndesignUIHtmlBuilder
import edu.kit.ipe.adl.indesign.core.module.ui.www.external.ExternalBuilder
import edu.kit.ipe.adl.indesign.core.heart.Heart
import edu.kit.ipe.adl.indesign.core.heart.model.HeartTaskStatus

trait HeartHtmlBuilder extends ExternalBuilder {

  override def externalAdd(targetNode: HTMLNode[HTMLElement, Any]): Unit = {
    
    super.externalAdd(targetNode)
    switchToNode(targetNode, {
      // Extra scripts
      script(new URI(createSpecialPath("resources", "module/heart/heart.js"))) {

      }

    })
    //super.externalAdd(targetNode)

  }

  /**
   * Create a Generic Taks
   */
  def heartTaskWithCondition(tid: String)(c: Option[_])(cl: => Any): Option[HeartTask[_]] = {

    c match {
      case None =>
        Heart.running(tid) match {
          case None =>
            var ht = new HeartTask[Any] {
              
              def getId = tid
              def doTask = {
                cl
              }
            }

            Heart.pump(ht)
            Some(ht)
          case Some(r) => Some(r)
        }

      case _ =>
        None
    }

  }

  def taskMonitorAltUI(text:String,t: Option[HeartTask[_]])(altUI: => Any) = t match {
    case Some(t) =>
      "ui active small inline indeterminate text loader" :: div {
        +@("hearth-task-id"->t.getId)
        textContent(text)
      }
      t.onCleaned {
        case h => 
          //println("Sending Backend Message")
          var status = new HeartTaskStatus
          status.ID = t.getId
          status.state = HeartTask.DONE.name
          sendBackendMessage(status)
      }
    case None =>
      altUI
  }

  /**
   * Button for fast task creation
   */
  def taskButton[RT <: Any](id: String)(name: String)(content: HeartTask[RT] => RT): HeartTaskButton = {

    //-- Create Button
    var hButton = "ui button" :: button(name) {
      "settings icon" :: i {

      }

    }

    //-- Create task
    var task = new HeartTask[RT] {

      def getId = id

      def doTask = {

        content(this)
      }

    }

    //-- Create Wrapper
    var taskButton = new HeartTaskButton(hButton, task)

    // Now setup run
    switchToNode(hButton, {

      //-- Get an Action String
      var actionString = getActionString {
        taskButton.submitTask
      }

      +@("onclick" -> s"indesign.heart.launchTask(this,'${taskButton.submitTask}')")

    })

    taskButton
  }

}