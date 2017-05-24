package org.odfi.indesign.core.module.ui.www.heart

import com.idyria.osi.vui.html.Button
import org.odfi.indesign.core.heart.HeartTask
import org.odfi.indesign.core.heart.Heart

class HeartTaskButton(val button : Button[_,_],val task: HeartTask[_]) {
  
  
  def submitTask = {
    Heart.pump(task)
  }
  
}