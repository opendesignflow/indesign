package test 

import org.odfi.indesign.core.module.ui.www.IndesignUIView
import java.io.File
import java.io.StringWriter
import java.io.PrintWriter
import java.util.prefs.Preferences

import com.idyria.osi.ooxoo.core.buffers.structural.ElementBuffer
import com.idyria.osi.ooxoo.core.buffers.structural.XList
import com.idyria.osi.ooxoo.core.buffers.structural.xattribute
import com.idyria.osi.ooxoo.core.buffers.structural.xelement


class TestWebView extends IndesignUIView {
  
  
  this.viewContent {
    
    div {
      
      h1("Hello test view") {
        
      }
      
      "ui segment" :: div {
        
        input {
          bindValue {
            v : Long => 
              println(s"Got Long From Web page: "+v)
          }
        }
        
      }
      
      "ui segment" :: div {
        
        span {
          textContent("Heartbeat: ")
        }
        span {
          id("heartbeat-value")
        }
        button("Get") {
          onClick {
            var hb = new com.idyria.osi.wsb.webapp.localweb.HeartBeat
            hb.time = System.currentTimeMillis()
            sendBackendMessage(hb)
          }
        }
        
      }
      
      script{"""
$(function() {

  console.info("Test Web View");
  localWeb.onPushData("HeartBeat",function(payload) {
        
      console.log("Got HB: "+payload+"->"+JSON.stringify(payload)+"->"+payload._a_time);
      $("#heartbeat-value").html(payload._a_time);
      
  });
});
"""}
      
    }
    
  }
  
  

}

