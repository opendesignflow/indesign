package edu.kit.ipe.adl.indesign.core.harvest.fs

import java.io.File
import edu.kit.ipe.adl.indesign.core.harvest.Harvester
import java.nio.file.Path
import java.nio.file.Files
import scala.language.implicitConversions
import edu.kit.ipe.adl.indesign.core.harvest.HarvestedResource

class FileSystemHarvester extends Harvester[HarvestedFile, HarvestedFile] {

  //var searchPaths = List[Path]()

  def addPath(p: Path) = {
    var f = new HarvestedFile(p)
    f.root
    deliverDirect(f)
    //this.searchPaths = this.searchPaths :+ p.toFile.getAbsoluteFile.toPath()
  }

  //def createResourceFromPath(p:Path) : RT

  override def deliver(r:HarvestedFile) = {
    (r.path.toFile().exists(),r.path.toFile().isDirectory()) match {
      case (true,true) =>
        gather(r)
        true
      case _ => false
    }
  }
  
  def doHarvest = {

    //println(s"Harvesting")
    /**
     * For each child harvester, a list of paths to stop at during walk
     */
    var stopList = scala.collection.mutable.Map[Harvester[_,_], scala.collection.mutable.ListBuffer[Path]]()
    this.childHarvesters.foreach { 
      ch  => 
      
        stopList.update(ch, new scala.collection.mutable.ListBuffer[Path])
       // stopList += (ch.asInstanceOf[Harvester[_, _]] -> new scala.collection.mutable.ListBuffer[Path])
      }

    this.getResources.foreach {
      resource =>

        var basePath = resource.path
        
        //println(s"Starting harvest on: $basePath")
        
        var stream = Files.walk(basePath)

        // Delevier files to child harvester
        //stream.forEach(FileSystemHarvester.childHarvesters.foreach(h => h.deliver(_)))
        //stream.forEach(p => FileSystemHarvester.childHarvesters.foreach(h => h.deliver(p))) 
        //stream.forEach { p => }

        def doF(inputPath: Path) = {
          var r = new HarvestedFile(inputPath)

          // Retain only child harvesters for which the current path has no parent in stop list
          var validChildren = this.childHarvesters.filter {
            ch => stopList(ch).find(stopPath => inputPath.startsWith(stopPath)) == None
          }

          // Deliver, if gathered with true, and it is a folder, then add path to stop path for child
          validChildren.foreach {
            ch =>
              ch.deliver(r) match {
                // Add to stop list
                case true if (inputPath.toFile().isDirectory()) =>
                  println(s"Gathered Directory")
                  stopList.keys.foreach {
                    ch => stopList(ch) += inputPath
                  }
                  
                case _ =>
              }

          }
        }

        stream.forEach {
          inputPath => doF(inputPath)

        }
    }

  }

  def processFile(inputPath: Path): Unit = {

  }
}

object FileSystemHarvester {

  implicit def pathToResource(p: Path) = new HarvestedFile(p)
}