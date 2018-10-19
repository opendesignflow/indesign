package org.indesign.boot

import org.boot.stage1.Boot

import javafx.stage.Stage
import org.odfi.indesign.core.module.IndesignModule
import org.odfi.indesign.core.config.ooxoo.OOXOOConfigModule

object Stage2 extends IndesignModule {
  
    this.onInit {
        requireModule(OOXOOConfigModule)
    }
     
    def launch(stage1:Boot) = {
        println(s"Inside Stage2")
        stage1.changeScene(null)
        
        moveToStart
    }
    
}