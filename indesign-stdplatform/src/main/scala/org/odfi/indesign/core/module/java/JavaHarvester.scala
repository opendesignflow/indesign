package org.odfi.indesign.core.module.java

import org.odfi.indesign.core.harvest.Harvester
import org.odfi.indesign.core.harvest.fs.HarvestedFile
import java.io.File

import org.odfi.indesign.core.module.windows.WinRegistryKey
import org.odfi.tea.os.OSDetector

object JavaHarvester extends Harvester {
 
  
  override def doHarvest = {
    
    OSDetector.isWindows() match {
      case true => 
        
        var reg = new WinRegistryKey("\"HKEY_LOCAL_MACHINE\\SOFTWARE\\JavaSoft\\Java Development Kit\"")
        reg.exists match {
          case true => 
            
            // Get all sub values, some lines will start with "JavaHome" -> use them as source
            reg.queryAllValuesRecursive.collect {
              case l if (l.trim().startsWith("JavaHome")) => 
                l.trim().stripPrefix("JavaHome").trim().stripPrefix("REG_SZ").trim()
            }.foreach {
              case jHome => 
               // println("JHOME: "+jHome)
                var file = new File(jHome)
                if (file.exists()) {
                  gather(new JavaInstallation(file))
                }
            }
            
          case false => 
            //println("No REg key")
        }
      case false => 
    }
    
  }
  
  
}

class JavaInstallation(f:File) extends HarvestedFile(f.toPath) {
  
  def isJDK = this.path.toFile().getName.contains("jdk")
  
}