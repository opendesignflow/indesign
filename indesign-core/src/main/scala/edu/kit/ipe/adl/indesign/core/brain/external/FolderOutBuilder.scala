package edu.kit.ipe.adl.indesign.core.brain.external

import java.io.File
import java.net.URL

import com.idyria.osi.tea.compile.ClassDomain
import com.idyria.osi.tea.compile.ClassDomainSupport

import edu.kit.ipe.adl.indesign.core.brain.Brain
import edu.kit.ipe.adl.indesign.core.brain.ExternalBrainRegion
import edu.kit.ipe.adl.indesign.core.brain.ExternalBrainRegionBuilder
import edu.kit.ipe.adl.indesign.core.harvest.fs.FileSystemHarvester
import javax.swing.plaf.synth.Region
import edu.kit.ipe.adl.indesign.core.brain.BrainRegion

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
    this.classDomain match {
      case Some(cd) =>
        cd.tainted = true
        this.classDomain = None
      case _ =>
    }

  }

  this.onCleaned {
    case h =>
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
  def loadRegionClass(cl: String): BrainRegion = {

    Brain.createRegion(classDomain.get, cl)
    // this.addSubRegion(region)

  }

}