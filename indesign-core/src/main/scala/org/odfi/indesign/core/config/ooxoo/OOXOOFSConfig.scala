package org.odfi.indesign.core.config.ooxoo

import org.odfi.indesign.core.config.ConfigImplementation
import java.io.File
import com.idyria.osi.ooxoo.db.store.fs.FSStore
import java.util.Properties
import java.util.PropertyResourceBundle
import java.io.FileReader
import java.io.FileInputStream
import java.io.FileOutputStream

class OOXOOFSConfigImplementation(var baseFile: File) extends ConfigImplementation {

  // Create FSStore
  baseFile = baseFile.getCanonicalFile
  var fsStore = new FSStore(baseFile)

  var realmFSStore: Option[FSStore] = None

  def addRealm(name: String) = listAllRealms.find(_ == name) match {
    case None =>
      new File(baseFile, name).mkdirs()
    case Some(newRealm) =>
  }

  def detectLatestRealm: Option[String] = {

    var latestRealmFile = new File(baseFile, "config-properties.xml")

    //println(s"Using file to reload properties: " + latestRealmFile)

    latestRealmFile.exists() match {
      case true =>

        var props = new Properties()
        var is = new FileInputStream(latestRealmFile)
        props.loadFromXML(is)
       // props.list(System.out)
        try {
          props.containsKey("realm.current") match {
            case true =>
              //println(s"Found key: " + props.getProperty("realm.current"))
             //println(s"Found key: " + props.getProperty("realm.current"))
              Some(props.getProperty("realm.current"))
            case false =>
              None
          }
        } finally {
          is.close()
        }
      case false => None
    }
  }

  def listAllRealms = {
    baseFile.listFiles().filter(_.isDirectory()).map { _.getName }.toList
  }

  def openConfigRealm(str: String) = {
    this.realmFSStore = Some(new FSStore(new File(baseFile, str)))

    var latestRealmFile = new File(baseFile, "config-properties.xml")
    var properties = latestRealmFile.exists() match {
      case true =>
        var p = new Properties()
        var is = new FileInputStream(latestRealmFile)
        p.loadFromXML(is)
        is.close
        p
      case false =>
        var p = new Properties()
        p
    }

    properties.put("realm.current", str)
    var os = new FileOutputStream(latestRealmFile)
    properties.storeToXML(os, "UTF8")
    os.close()

  }

  // Containers
  def getContainer(str: String) = this.realmFSStore match {
    case Some(store) => store.container(str)
    case None => throw new IllegalArgumentException("Cannot open container if no realm has been opened")
  }

}