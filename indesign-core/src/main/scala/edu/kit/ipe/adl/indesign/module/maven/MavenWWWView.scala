package edu.kit.ipe.adl.indesign.module.maven

import com.idyria.osi.wsb.webapp.localweb.LocalWebEngine
import com.idyria.osi.wsb.webapp.localweb.LocalWebHTMLVIew
import edu.kit.ipe.adl.indesign.core.module.ui.www.IndesignUIView

class MavenProjectWWWView(val r:MavenProjectResource) extends IndesignUIView {
  
  override def name =r.pomFile.getParentFile.getName 
    
  this.root
  
  this.viewContent {
    
    div {
      h1("Maven Project") {
        
      }
      "ui segment" :: div {
        
        p {
         span {
           textContent("POM File Path: "+r.pomFile.getAbsolutePath)
         }
        }
        
      }
    }
    
  }
  
  
}