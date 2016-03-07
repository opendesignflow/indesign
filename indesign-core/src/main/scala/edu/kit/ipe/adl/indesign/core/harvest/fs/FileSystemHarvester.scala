package edu.kit.ipe.adl.indesign.core.harvest.fs

import java.io.File
import edu.kit.ipe.adl.indesign.core.harvest.Harvester
import edu.kit.ipe.adl.indesign.core.harvest.HarvestSupport
import java.nio.file.Path
import java.nio.file.Files
import scala.language.implicitConversions

class FileSystemHarvester(var basePath : Path) extends Harvester[HarvestedFile] {
  
  
  def harvest = {
    
    println(s"Starting harvest on: $basePath")
    var stream = Files.walk(basePath)
    
    
    // Delevier files to child harvester
    //stream.forEach(FileSystemHarvester.childHarvesters.foreach(h => h.deliver(_)))
    //stream.forEach(p => FileSystemHarvester.childHarvesters.foreach(h => h.deliver(p))) 
    stream.forEach {
      p => 
       processFile(p)
        
    }
  }
  
  def processFile(p:Path) = {
    var r = new HarvestedFile(p)
    FileSystemHarvester.childHarvesters.foreach(h => h.deliver(r))
  }
}

object FileSystemHarvester extends HarvestSupport[HarvestedFile] {
  
  
  implicit def pathToResource(p : Path) = new HarvestedFile(p)
}