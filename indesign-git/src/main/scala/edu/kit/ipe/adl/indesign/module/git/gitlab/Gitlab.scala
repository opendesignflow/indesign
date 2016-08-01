package edu.kit.ipe.adl.indesign.module.git.gitlab

import com.idyria.osi.ooxoo.core.buffers.structural.xelement
import java.net.URL
import java.net.HttpURLConnection
import com.idyria.osi.tea.io.TeaIOUtils


@xelement(name="Gitlab")
class Gitlab extends GitlabTrait{
  
  
 /* def listProjects = {
    
    //-- Base URL
    var url = new URL(gitlabURL.toString()+this.aPIBase.toString)
    
    //-- Set Private Token
    var conn = url.openConnection().asInstanceOf[HttpURLConnection]
    conn.setRequestMethod("GET")
    conn.setRequestProperty("PRIVATE-TOKEN", this.privateToken)
    
    //-- Run
    conn.connect()
    var res = TeaIOUtils.swallowStream(conn.getInputStream)
    
    println("REs: "+res)
  }
  */
}