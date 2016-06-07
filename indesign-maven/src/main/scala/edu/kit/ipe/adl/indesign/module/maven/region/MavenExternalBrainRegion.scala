package edu.kit.ipe.adl.indesign.core.brain

import java.io.File

import edu.kit.ipe.adl.indesign.module.maven.MavenProjectResource
import java.net.URL
import edu.kit.ipe.adl.indesign.core.brain.external.FolderOutputBrainRegion
import com.idyria.osi.tea.compile.ClassDomain
import edu.kit.ipe.adl.indesign.core.harvest.Harvest
import edu.kit.ipe.adl.indesign.core.harvest.fs.FileSystemHarvester
import edu.kit.ipe.adl.indesign.core.harvest.fs.HarvestedFile

class MavenExternalBrainRegionBuilder extends ExternalBrainRegionBuilder {

  def accept(url: URL): Integer = {

    url.getProtocol match {
      case "file" =>

        // Look for target/classes
        new File(new File(url.getPath), "pom.xml") match {
          case pomFile if (pomFile.exists()) =>
            // Return 2 to be above standard Folder Region Builder
            2
          case _ => 0
        }
      case _ => 0
    }

  }

  def build(url: URL): ExternalBrainRegion = {

    var base = new File(url.getPath).getCanonicalFile
    new MavenExternalBrainRegion(new HarvestedFile(base.toPath()))

  }
}

/**
 * Loads a Brain Region present in another externaly compiled module
 */
class MavenExternalBrainRegion(val basePath: HarvestedFile) extends MavenProjectResource(basePath.path) with ExternalBrainRegion {

  override def name = projectModel.artifactId match {
    case null => getId
    case v => v.toString
  }
  override def getId = projectModel.artifactId match {
    case null => basePath.path.toFile().getAbsolutePath
    case v => v.toString
  }
  
  override def getRegionPath = basePath.path.toFile.getAbsolutePath
  //-- Override tainted to make sure tainted is only if original classloader is tainted and also local one

  /**
   * Tainted if class domain is tainted or this classdomain tainted and classloader is also tainted
   */
  override def isTainted = {
    this.classDomain.tainted || (this.classDomain.tainted && this.getClass.getClassLoader.isInstanceOf[ClassDomain] && this.getClass.getClassLoader.asInstanceOf[ClassDomain].tainted)
  }
  
  

  //-- Load actual Region
  /*println(s"CL: " + Thread.currentThread().getContextClassLoader)
  this.resetClassDomain
  println(s"CL: " + Thread.currentThread().getContextClassLoader)*/

  // Region load
  def loadRegionClass(cl: String) = {

    forceUpdateDependencies
    logFine[Brain]("Create Region Class: " + this.classDomain)
    logFine[Brain]("Create Region Class: " + this.classDomain.getURLs.toList)
    /*var region = Brain.createRegion(this.classDomain, cl)
    this.addSubRegion(region)*/
    Brain.createRegion(this.classDomain, cl)

  }

  this.onSetup {
    logFine[Brain]("Reseting CLD: " + this + " - " + this.classDomain)
    this.resetClassDomain
    logFine[Brain]("Now CLD: " + this.classDomain)
    
    //-- Add to FSHarvester if needed
    println(s"***** Delivering base path $basePath to FS Harvester")
    Harvest.onHarvesters[FileSystemHarvester] {
      case fsh => 
        println(s"***** ----> Doing")
        fsh.deliverDirect(basePath)
    }
    
  }
  this.onShutdown {
    logFine[Brain]("Maven REgion on Shutdown: " + this + " - " + this.classDomain)
    this.classDomain.tainted = true
    //this.classDomain = null
    
    //-- Remove From Harvester
    Harvest.onHarvesters[FileSystemHarvester] {
      case fsh => 
        fsh.cleanResource(basePath)
      
    }
  }

  this.onCleaned {
    case h =>
      logFine[Brain]("Maven REgion Cleaned: " + this + " - " + this.classDomain)
      this.classDomain.tainted = true
  }
 
  // Region Discovery
  //-----------
  override def discoverRegions: List[String] = {

    var regionFiles = new File(this.basePath.path.toFile(), "target/classes/META-INF/indesign/regions.available")
    regionFiles match {
      case rf if (rf.exists() == true) =>

        scala.io.Source.fromFile(regionFiles, "UTF-8").getLines().toList

      case _ => List[String]()
    }

  }

}