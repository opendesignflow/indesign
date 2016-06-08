package edu.kit.ipe.adl.indesign.core.module.ui.www.views

import edu.kit.ipe.adl.indesign.core.module.ui.www.IndesignUIView
import com.idyria.osi.wsb.webapp.localweb.LocalWebEngine
import com.idyria.osi.wsb.webapp.localweb.SingleViewIntermediary

class LocalWebView extends IndesignUIView {
  
  this.viewContent {
    
    div {
      h1("Local Web Views") {
        
      }
      
      
      var viewIntermediaries = LocalWebEngine.topViewsIntermediary.intermediaries.collect {
        case i if(classOf[SingleViewIntermediary].isInstance(i)) => i.asInstanceOf[SingleViewIntermediary]
      }
      
      "ui stripped celled table" :: table {
        thead {
          tr {
            th("Path") {
              
            }
            th("View Class") {
              
            }
            th("Approx. Number of Connections") {
              
            }
          }
        }
        tbody {
          
          viewIntermediaries.foreach {
            viewIntermediary => 
              
              tr {
                
                td(viewIntermediary.basePath) {
                  
                }
                td(viewIntermediary.viewClass.getCanonicalName) {
                  
                }
                td(viewIntermediary.viewPool.size.toString) {
                  
                }
                
              }
              
          }
          
          
        }
      }
    }
  }
}