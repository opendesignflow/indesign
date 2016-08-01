package edu.kit.ipe.adl.indesign.core.module.ui.www.heart

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

  def taskMonitorAltUI(text: String, t: Option[HeartTask[_]])(altUI: => Any) = t match {
    case Some(t) =>
      "ui active small inline indeterminate text loader" :: div {
        +@("hearth-task-id" -> t.getId)
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
  def taskButton(id: String)(startName: String, stopName: String)(content: HeartTask[Any] => Any) = {

    // Check Task is not running
    //--------------------
    Heart.running(id) match {
      case Some(t) =>

        //-- Create Button
        "ui button" :: button(stopName) {
          "settings icon" :: i {

          }
          onClickReload {
            
            Heart.killTask(t)
          }

        }

      case None =>
        
        //-- Create Button
        "ui button" :: button(startName) {
          "settings icon" :: i {

          }
          onClickReload {
            
            //-- Create task
            var task = new HeartTask[Any] {

              def getId = id

              def doTask = {

                content(this)
              }

            }
            
            //-- Run
            Heart.pump(task)
          }

        }
    }

    //-- Create Wrapper
    /*var taskButton = new HeartTaskButton(hButton, task)

    // Now setup run
    switchToNode(hButton, {

      //-- Get an Action String
      var actionString = getActionString {
        taskButton.submitTask
      }

      +@("onclick" -> s"indesign.heart.launchTask(this,'${taskButton.submitTask}')")

    })

    taskButton*/
  }

}