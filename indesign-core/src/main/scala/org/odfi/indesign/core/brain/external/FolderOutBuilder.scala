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
import org.odfi.indesign.core.brain.LFCDefinition
import org.odfi.indesign.core.harvest.fs.FSGlobalWatch
import org.odfi.indesign.core.harvest.Harvest
import scala.reflect.ClassTag
import scala.collection.convert.DecorateAsScala

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

class FolderOutputBrainRegion(val basePath: File, val outputPath: File) extends ExternalBrainRegion with ClassDomainSupport with DecorateAsScala {
  // this.addPath(basePath.toPath())

  //tlogEnableFull[LFCDefinition]

  def getRegionPath = basePath.getAbsolutePath

  override def getName = basePath.getName
  override def getId = basePath.getCanonicalPath
  def id = basePath.getAbsolutePath

  override def toString = s"FolderRegion: ${getRegionPath}"
  
  override def getDisplayName = s"Folder Region: ${basePath.getParentFile.getName+File.separator+basePath.getName} "

  // Auto Reload
  //---------------------
  var beautyTime = 10000
  var lastTime = 0L
  this.onAdded {
    case h if(h==Brain)=> 
    FSGlobalWatch.watcher.watchDirectoryRecursive(this, outputPath) {
      case f if (f.exists && f.getName.endsWith(".class")) =>

        println(s"######### Detected compilation on ${this.hashCode()}, reloading class ###########")
        if (lastTime < (System.currentTimeMillis() - beautyTime)) {
          lastTime = System.currentTimeMillis() 
          Thread.sleep(beautyTime/2)
          this.reload
          this.harvest
          Harvest.run
          //Harvest.run
        }

      case other =>
    }
  }

  // Create CLass Domain
  //------------------
  var classDomain: Option[ClassDomain] = None

  this.onSetup {
    logFine[FolderOutputBrainRegion](s"**** Setup folder: "+hashCode())
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
 
  this.onStop {
    logFine[FolderOutputBrainRegion](s"**** Stopping folder "+hashCode())

  }

  this.onShutdown {
    logFine[FolderOutputBrainRegion](s"**** Shutting Down folder "+hashCode())
    this.classDomain match {
      case Some(cd) =>

        cd.tainted = true
        this.classDomain = None
      case _ =>
    }

  } 

  this.onCleaned {
    case h =>

      logFine[FolderOutputBrainRegion](s"**** Cleaning folder")
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
  override def discoverRegions: List[String] = this.classDomain match {

    case Some(cd) =>

      var stream = Files.walk(outputPath.toPath())
      var regions = List[String]()
      stream.forEach {
        f =>

          // var relativeF = f.resolve(outputPath.toPath())
          //println("Lookin at: "+relativeF.toString())
          if (f.getFileName.toString().endsWith("$.class")) {

            var className = f.toString().replace(outputPath.getCanonicalPath + File.separator, "").replace(File.separator.toString, ".").replace(".class", "")
            // println("Lookin at: "+className)
            Brain.getObject(cd, className) match {
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

    case None =>
      List[String]()

  }
  
  override def discoverType[CT <: Any](implicit tag :ClassTag[CT]) = this.classDomain match {

    case Some(cd) =>

      var stream = Files.walk(outputPath.toPath())
      var foundTypes = List[Class[CT]]()
      stream.forEach {
        f =>

          /*f.getFileName.toString().endsWith("$.class") match {
            // Don't Check objects
            case true => 
          }*/
          
          // var relativeF = f.resolve(outputPath.toPath())
          //println("Lookin at: "+relativeF.toString())
          //if (f.getFileName.toString().endsWith("$.class")) {

            var className = f.toString().replace(outputPath.getCanonicalPath + File.separator, "").replace(File.separator.toString, ".").replace(".class", "")
             println(s"Discover ${tag} Lookin at: "+className)
             
             try {
               var cl = cd.loadClass(className)
               tag.runtimeClass.isAssignableFrom(cl) match {
                 case true => 
                   foundTypes = foundTypes :+ cl.asInstanceOf[Class[CT]]
                 case false => 
               }
             } catch {
               case e : Throwable => 
                 
             }
            
 
      }

      foundTypes

    case None =>
      List[Class[CT]]()

  }

}