package edu.kit.ipe.adl.indesign.core.module.ui.www

import com.idyria.osi.wsb.webapp.localweb.LocalWebHTMLVIew
import com.idyria.osi.wsb.webapp.localweb.DefaultLocalWebHTMLBuilder
import java.net.URL

class IndesignWWWView extends LocalWebHTMLVIew with DefaultLocalWebHTMLBuilder {
  
  this.viewContent {
    html {
      head {
        
        stylesheet(new URL("http://localhost:6666/resources/semantic/semantic.min.css")) {
 
        }
        
      }
      body {
        
        h1("InDesign UI 2") {
          
        }
        
        // Harvest
        //------------------
        h2("Harvest") {
          
        }
      }
    }
  }
}