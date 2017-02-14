package org.odfi.indesign.core.config.model

import com.idyria.osi.ooxoo.db.store.DocumentContainer
import com.idyria.osi.ooxoo.db.traits.DBContainerReference

trait CommonConfig extends CommonConfigTrait with DBContainerReference {
  
 
  
  
  def isInConfig(keyType:String,value:String) = {
    this.values.keys.find {
      key => key.keyType === keyType && key.values.find {v => v === value}.isDefined
    }.isDefined
  }
  
  /**
   * Get first match for key name
   */
  def getKey(name:String) = {
    
    this.values.keys.find(_.name.toString()==name)
    
  }
  
  /**
   * Get All keys answering to name and type
   */
  def getKeys(name:String,t:String) = {
    this.values.keys.filter {
      case key if(key.name!=null && key.name === name && key.keyType!=null && key.keyType===t) => true
      case _ => false
    }
  }
  
   def removeFromConfig(keyType:String,value:String) : Boolean = {
     this.values.keys.find {
      key => key.keyType === keyType
    } match {
      case Some(key) =>
        key.values.find {v => v === value} match {
          case Some(valueNode) =>
            key.values -= valueNode
            true
          case None => false
        }
      case None =>  false
        
    }
   }
   
   def addKey(name:String,t:String) = {
     
     var key = this.values.keys.add
     key.keyType = t
     key.name = name
     key
     
   }
}