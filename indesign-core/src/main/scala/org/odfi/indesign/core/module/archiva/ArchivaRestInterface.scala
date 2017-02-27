package org.odfi.indesign.core.module.archiva

import org.odfi.indesign.core.harvest.HarvestedResource
import java.net.URL
import java.net.HttpURLConnection
import com.idyria.osi.tea.io.TeaIOUtils

class ArchivaRestInterface(var baseUrl : String) extends HarvestedResource {
  
  def getId = getClass.getCanonicalName+":"+baseUrl
  
  val getVersionsURL = (baseUrl+"/restServices/archivaServices/searchService/getArtifactVersions")
  
  val versionsList = baseUrl+"/restServices/archivaServices/browseService/versionsList"
  
  def getVersionsFor(groupId:String,artifactId:String) = {
    
    var url = new URL(getVersionsURL+s"?artifactId=$artifactId&groupId=$groupId&packaging=jar&format=xml")
    
    var con = url.openConnection().asInstanceOf[HttpURLConnection] 
    con.connect()
    
    println(s"Type:" +con.getContentType)
    println(s"Type:" +con.getResponseCode)
    
    var is = con.getInputStream
    var respXML = new String(TeaIOUtils.swallowStream(is))
    
    println(s"Resp:" +respXML)
  }
  
   def listVersionsFor(groupId:String,artifactId:String) = {
    
    var url = new URL(versionsList+s"/$groupId/$artifactId")
    
    var con = url.openConnection().asInstanceOf[HttpURLConnection] 
    con.addRequestProperty("Accept", "application/xml")
    con.connect()
    
    println(s"Type:" +con.getContentType)
    println(s"Type:" +con.getResponseCode)
    
    var is = con.getInputStream
    var respXML = new String(TeaIOUtils.swallowStream(is))
    
    println(s"Resp:" +respXML)
  }
}