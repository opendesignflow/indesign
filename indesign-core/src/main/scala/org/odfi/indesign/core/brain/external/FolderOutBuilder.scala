package org.odfi.indesign.core.brain.external

import java.io.File
import java.net.URL
import java.nio.file.Files

import org.odfi.indesign.core.brain.Brain
import org.odfi.indesign.core.brain.BrainRegion
import org.odfi.indesign.core.brain.ExternalBrainRegion
import org.odfi.indesign.core.brain.ExternalBrainRegionBuilder

import com.idyria.osi.tea.compile.ClassDomain
import com.idyria.osi.tea.compile.ClassDomainSupport
import org.odfi.indesign.core.module.IndesignModule

class FolderOutBuilder extends ExternalBrainRegionBuilder {

  def accept(url: URL): Integer = {

    url.getProtocol match {
      case "file" =>

        // Look for target/classes
        new File(new File(url.getPath), List("target", "classes").mkString(File.separator)) match {
          case outputFolder if (outputFolder.exists() && outputFolder.isDirectory()) =>
            1
          case _ => 0
        }
      case _ => 0
    }

  }

  def build(url: URL): ExternalBrainRegion = {

    var base = new File(url.getPath).getCanonicalFile
    var output = new File(base, List("target", "classes").mkString(File.separator)).getCanonicalFile

    new FolderOutputBrainRegion(base, output)

  }

}

class FolderOutputBrainRegion(val basePath: File, val outputPath: File) extends ExternalBrainRegion with ClassDomainSupport {
  // this.addPath(basePath.toPath())

  def getRegionPath = basePath.getAbsolutePath

  override def name = basePath.getName
  override def getId = basePath.getCanonicalPath
  def id = basePath.getAbsolutePath

  override def toString = s"FolderRegion: ${getRegionPath}"

  // Create CLass Domain

  var classDomain: Option[ClassDomain] = None

  this.onSetup {
    println(s"**** Setup folder")
    classDomain = Some(new ClassDomain(getClass.getClassLoader))
    classDomain.get.addURL(outputPath.toURI.toURL)

    // Try to add jars in lib folder
    var libFolder = new File(basePath, "target/dependency")
    libFolder.exists() match {
      case true =>
        libFolder.listFiles.filter(f => f.getName.endsWith(".jar")).foreach {
          lf => classDomain.get.addURL(lf.toURI.toURL)
        }

      case false =>
    }
  }

  this.onShutdown {
    println(s"**** Shutting Down folder")
    this.classDomain match {
      case Some(cd) =>

        cd.tainted = true
        this.classDomain = None
      case _ =>
    }

  }

  this.onCleaned {
    case h =>

      println(s"**** Cleaning folder")
      logFine[Brain]("Cleaning: " + this)
      this.classDomain match {
        case Some(cd) =>
          cd.tainted = true
          this.classDomain = None
        case _ =>
      }
  }

  /**
   *
   */
  def loadRegionClass(cl: String): ErrorOption[BrainRegion] = {

    classDomain match {
      case Some(cd) =>
        try {
          ESome(Brain.createRegion(classDomain.get, cl))
        } catch {
          case e: Throwable =>
            EError(e)
        }
      case None =>
        ENone
    }

    // this.addSubRegion(region)

  }

  //-- Discover
  override def discoverRegions: List[String] = {

    var stream = Files.walk(outputPath.toPath())
    var regions = List[String]()
    stream.forEach {
      f =>

        // var relativeF = f.resolve(outputPath.toPath())
        //println("Lookin at: "+relativeF.toString())
        if (f.getFileName.toString().endsWith("$.class")) {

          var className = f.toString().replace(outputPath.getCanonicalPath + File.separator, "").replace(File.separator.toString, ".").replace(".class", "")
          // println("Lookin at: "+className)
          Brain.getObject(this.classDomain.get, className) match {
            case Some(obj) =>
              classOf[IndesignModule].isInstance(obj) match {
                case true =>
                  //println("Found object")
                  regions = regions :+ obj.getClass.getCanonicalName
                  Some(obj.getClass.getCanonicalName)
                case false => None
              }
            case None => None
          }
        }
    }

    regions

    /*var jf = new File(this.getRegionPath)
    (jf.exists && jf.getName.endsWith(".jar")) match {
      case true =>

        // Open Jar File
        var jar = new JarFile(jf)
        jar.entries().collect {
          case entry if (entry.getName.endsWith("$.class")) =>
            //println("Entry: "+entry)
            Brain.getObject(this.classdomain.get, entry.getName.replace("/", ".").replace(".class", "")) match {
              case Some(obj) =>
                classOf[IndesignModule].isInstance(obj) match {
                  case true => Some(obj.getClass.getCanonicalName)
                  case false => None
                }
              case None => None
            }

        }.collect { case rno if (rno.isDefined) => rno.get }.toList

      case false => List()
    }*/

  }

}