package org.odfi.indesign.core.module.archiva

import java.net.{HttpURLConnection, URL}

import org.odfi.indesign.core.harvest.HarvestedResource
import org.odfi.tea.io.TeaIOUtils
import org.eclipse.aether.artifact.DefaultArtifact
import org.eclipse.aether.resolution.MetadataRequest
import org.eclipse.aether.metadata.DefaultMetadata
import org.eclipse.aether.metadata.Metadata
import org.odfi.indesign.core.module.artifactresolver.AetherResolver

class ArchivaRestInterface(var baseUrl: String) extends HarvestedResource   {

  def getId = getClass.getCanonicalName + ":" + baseUrl

  val getVersionsURL = (baseUrl + "/restServices/archivaServices/searchService/getArtifactVersions")

  val versionsList = baseUrl + "/restServices/archivaServices/browseService/versionsList"

  def getVersionsFor(groupId: String, artifactId: String) = {

    var url = new URL(getVersionsURL + s"?artifactId=$artifactId&groupId=$groupId&packaging=jar&format=xml")

    var con = url.openConnection().asInstanceOf[HttpURLConnection]
    con.connect()

    println(s"Type:" + con.getContentType)
    println(s"Type:" + con.getResponseCode)

    var is = con.getInputStream
    var respXML = new String(TeaIOUtils.swallowStream(is))

    println(s"Resp:" + respXML)
  }

  def listVersionsFor(groupId: String, artifactId: String) = {

    var url = new URL(versionsList + s"/$groupId/$artifactId")

    var con = url.openConnection().asInstanceOf[HttpURLConnection]
    con.addRequestProperty("Accept", "application/xml")
    con.connect()

    println(s"Type:" + con.getContentType)
    println(s"Type:" + con.getResponseCode)

    var is = con.getInputStream
    var respXML = new String(TeaIOUtils.swallowStream(is))

    println(s"Resp:" + respXML)
  }

  

}