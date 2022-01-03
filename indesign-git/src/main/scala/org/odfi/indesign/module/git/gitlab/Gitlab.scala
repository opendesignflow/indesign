package org.odfi.indesign.module.git.gitlab

import org.odfi.ooxoo.core.buffers.structural.xelement
import java.net.URL
import java.net.HttpURLConnection
import org.odfi.tea.io.TeaIOUtils


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