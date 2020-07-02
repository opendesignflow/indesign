package org.odfi.indesign.core.module.filestash

import org.odfi.indesign.core.module.IndesignModule
import java.io.File
import java.nio.ByteBuffer
import org.odfi.tea.io.TeaIOUtils
import java.io.ByteArrayInputStream
import org.odfi.indesign.core.harvest.fs.HarvestedFile
import com.idyria.osi.ooxoo.core.buffers.structural.AnyXList
import com.idyria.osi.ooxoo.core.buffers.structural.xelement

object FileStash extends IndesignModule {
  
  var basePath = new File("")
  
  this.onInit {
    AnyXList(classOf[FileStash])
  }
  this.onShutdown {
    saveConfig
  }
  
  def getStashConfig =  {
    
    val fs = this.config.get.custom.content.ensureElement[FileStash]
    fs.basePath = basePath.getCanonicalPath
    fs
  }
    
  
  
}

@xelement(name="FileStash")
class FileStash extends FileStashTrait {
  
  /**
   * Return stash for specific owner
   */

  def getOwnerStash (id:String) = this.owners.findOrCreateByEId(id)
  
  /**
   * owner/id
   */
  def getStashByUID(id:String) = {
    assert(id.matches("""[@\w_\.-]+\/[\d]+"""),"Stash id: "+id+" doesn't match correct format")
    val splitted = id.split("/")
    val (owner,sid) = (splitted(0),splitted(1))
    
    val stashOwner = this.getOwnerStash(owner) 
    stashOwner.getStashById(sid)
    /*
     *  match {
      case None => sys.error(s"Cannot find Stash with UID: $id")
      case Some(stash) => stash
        
    }
     */
    
  }
  
  def stashFile(s:FileStashTraitOwnerTraitStash) = {
    val stashFile = new File(this.basePath+File.separator+s.parentReference.get.eid+File.separator+s.eid)
    stashFile.mkdirs
    stashFile
  }
  
}

class FileStashTraitOwner extends FileStashTraitOwnerTrait {
  
  /**
   * Create Stash with time id
   */
  def createDefaultStash = {
    
    val stash = this.stashes.add
    stash.creationDate
    stash.eid = System.currentTimeMillis()
    
    stash
  }
  
  def getEmptyStash = {
    this.stashes.find {
      stash => stash.filesCountOption.isDefined && stash.filesCount.data == 0
    }  match {
      case Some(empty) => empty
      case None => this.createDefaultStash
    }
  }
  
  def getStashById(eid:String) = this.stashes.find {
    case s => s.eid.toString == eid
  }
  
}

class FileStashTraitOwnerTraitStash extends FileStashTraitOwnerTraitStashTrait {
  
  def getUID = parentReference.get.eid+ "/"+eid
  
  override def toString = getUID
  
  /**
   * Convert stash local name to File relative to whole stash
   */
  def localNameToFile(name:String) = {
    new File(this.parentReference.get.parentReference.get.stashFile(this),name)
  }
  
  def getStashFile = this.parentReference.get.parentReference.get.stashFile(this)
  
  def writeFile(name:String,b : Array[Byte]) = {
    TeaIOUtils.writeToFile(localNameToFile(name), new ByteArrayInputStream(b))
    updateFilesCount
  }
  
  def updateFilesCount =  {
    this.filesCount = getStashFile.listFiles.size
  }
  
  def rescan = {
    this.filesCount = this.getStashFile.listFiles().size
  }
  
  // Files info
  //---------------
  
  /**
   * @return true if audio files are detected
   */
  def containsAudioFiles = {
    this.getStashFile.listFiles.find {
      f => 
       FileStashTraitOwnerTraitStash.audioExtensions.find {
          ext =>  f.getName.endsWith("."+ext)
        }.isDefined
       
    }.isDefined
  }
  
  def listAudioFiles = {
    this.getStashFile.listFiles.filter {
      f => 
       FileStashTraitOwnerTraitStash.audioExtensions.find {
          ext =>  f.getName.endsWith("."+ext)
        }.isDefined
       
    }.map(new StashFile(_))
  }
  
}

object FileStashTraitOwnerTraitStash {
  
  val audioExtensions = List("mp3","ogg","flac")
  
}

class StashFile(b:File) extends HarvestedFile(b.toPath()) {
  
  def isAudio = FileStashTraitOwnerTraitStash.audioExtensions.find(path.toString.endsWith(_)).isDefined
  def isMP3 = path.toString().endsWith(".mp3")
}