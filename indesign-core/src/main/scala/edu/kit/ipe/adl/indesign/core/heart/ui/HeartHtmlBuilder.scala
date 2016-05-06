package edu.kit.ipe.adl.indesign.core.heart.ui

import edu.kit.ipe.adl.indesign.core.module.ui.www.IndesignUIHtmlBuilder
import edu.kit.ipe.adl.indesign.core.heart.HeartTask

trait HeartHtmlBuilder extends IndesignUIHtmlBuilder {
 
  /**
   * Button for fast task creation
   */
  def taskButton[RT <: Any](id:String)(name:String)(content:  HeartTask[RT] => RT) :  HeartTaskButton = {
    
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
    var taskButton = new HeartTaskButton(hButton,task)
   
   
    
    // Now setup run
    switchToNode(hButton,  {
      
      //-- Get an Action String
      var actionString = getActionString {
        taskButton.submitTask
      }
      
      +@("onclick"->s"indesign.heart.launchTask(this,'${taskButton.submitTask}')")
      
    })
    
    taskButton
  }
  
}