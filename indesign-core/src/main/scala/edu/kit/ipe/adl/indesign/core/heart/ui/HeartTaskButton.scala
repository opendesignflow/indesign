package edu.kit.ipe.adl.indesign.core.heart.ui

import com.idyria.osi.vui.html.Button
import edu.kit.ipe.adl.indesign.core.heart.HeartTask
import edu.kit.ipe.adl.indesign.core.heart.Heart

class HeartTaskButton(val button : Button[_,_],val task: HeartTask[_]) {
  
  
  def submitTask = {
    Heart.pump(task)
  }
  
}