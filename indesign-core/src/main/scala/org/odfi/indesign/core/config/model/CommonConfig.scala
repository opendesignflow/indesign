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
  def getKey(name:String) : Option[CommonConfigTraitValuesKey] = {
    
    this.values.keys.find(_.name.toString()==name)
    
  }
  
  /**
   * Return key if a value is defined
   */
  def getKey(name:String,kType:String) : Option[CommonConfigTraitValuesKey] = {
    this.values.keys.find(k => k.name.toString()==name && k.keyType.toString()==kType && k.values.size>0)
  }
  
  def setKeyFirstValue(name:String,kType:String,v:String) = {
    this.getKey(name,kType) match {
      case Some(key) if (key.values.size>0) => 
          key.values(0).set(v)
      case Some(key) => 
        key.values.add.set(v)
      case None => 
       this.addKey(name, kType).values.add.set(v)
    }
    resyncToFile
    
  }
  
  def setUniqueKeyFirstValue(keyType:String,v:String) = {
    this.values.keys.find { k => k.keyType!=null && k.keyType.toString()==keyType && k.values.size>0 && k.values(0).toString==v} match {
      case None => 
        var k = this.values.keys.add
        k.keyType = keyType
        k.values.add.set(v)
      case other => 
    }
  }
  
  def getBoolean(name:String,default:Boolean) = {
    this.getKey(name,"boolean") match {
      case Some(key) if (key.values.size>0) => 
        try {
          key.values(0).toString().toBoolean
        } catch {
          case e : Throwable => 
            default
        }
      case other => default
    }
  }
  
  def setBoolean(name:String,v:Boolean) = setKeyFirstValue(name,"boolean",v.toString)
    
     
  def getInt(name:String,default:Int) = {
    this.getKey(name,"integer") match {
      case Some(key) if (key.values.size>0) => 
        try {
          key.values(0).toString().toInt
        } catch {
          case e : Throwable => 
            default
        }
      case other => default
    }
  }
  
  def setInt(name:String,v:Int) = setKeyFirstValue(name,"integer",v.toString)
  
  def getLong(name:String,default:Boolean) = {
    this.getKey(name,"long") match {
      case Some(key) if (key.values.size>0) => 
        try {
          key.values(0).toString().toLong
        } catch {
          case e : Throwable => 
            default
        }
      case other => default
    }
  }
  
  def setLong(name:String,v:Long) = setKeyFirstValue(name,"long",v.toString)
  
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