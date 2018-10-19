package org.odfi.indesign.core.config.model

import com.idyria.osi.ooxoo.db.store.DocumentContainer
import com.idyria.osi.ooxoo.db.traits.DBContainerReference
import com.idyria.osi.ooxoo.core.buffers.datatypes.DoubleBuffer
import com.idyria.osi.ooxoo.core.buffers.datatypes.BooleanBuffer
import com.idyria.osi.ooxoo.core.buffers.datatypes.IntegerBuffer
import com.idyria.osi.ooxoo.core.buffers.datatypes.XSDStringBuffer
import com.idyria.osi.ooxoo.core.buffers.structural.AbstractDataBuffer
import scala.reflect.ClassTag

trait CommonConfig extends CommonConfigTrait with DBContainerReference {

  // Save and autosave
  //-----------

  /**
   * Per default always resync config to file as default behaviour
   * In special cases, like if the commonConfig is used in a non file model, autosave can be disabled to avoid errors
   */
  var autosave = true

  /**
   * Don't autosave if the file is not defined
   */
  override def resyncToFile = {
    if (autosave) {
      try {
        super.resyncToFile
      } catch {
        case e: Throwable =>
          this
      }
    } else {
      this
    }
  }

  // Supported Config
  //-----------------------

  /**
   * Clean Supported Config set from  software
   */
  override def postStreamIn = {
    val values = this.supportedConfig.configValues.toList

    values.foreach {

      case v if (v.softwareSet.toBool) =>
        this.supportedConfig.configValues -= v
      case other =>
      /*if (v.softwareSet.toBool) {
           this.supportedConfig.configValues -= v
        }*/

    }
    /*this.supportedConfig.configValues = this.supportedConfig.configValues.filter {
      case v if (v.softwareSet.toBool) =>
        false
      case other =>
        true
    }*/
    super.postStreamIn
  }

  def supportConfigClean = {
    this.supportedConfig.configValues.clear
  }

  /**
   * MUST Use this method to set Support Config
   * It will cleanup existing keys with same name
   */
  def supportConfigKey(keyType: String, name: String, default: String, description: String) = {
    val supportedValue = this.supportedConfig.configValues.add
    supportedValue.keyType = keyType
    supportedValue.name = name
    supportedValue.description = description
    supportedValue.default = default
    supportedValue.softwareSet = true
    supportedValue
  }

  def supportStringKey(name: String, default: String, description: String) = {
    supportConfigKey("string", name, default, description)

  }
  def supportBooleanKey(name: String, default: Boolean, description: String) = {
    supportConfigKey("boolean", name, default.toString, description)

  }
  def supportIntKey(name: String, default: Int, description: String) = {
    supportConfigKey("integer", name, default.toString, description)

  }
  def supportDoubleKey(name: String, default: Double, description: String) = {
    supportConfigKey("double", name, default.toString, description)

  }

  def supportRangeDouble(name: String, description: String, mininum: Double, maximum: Double): Unit = {
    val sk = supportDoubleKey(name, 0.0, description)
    sk.hint = "range"
    val minp = sk.parameters.add
    minp.name = "min"
    minp.data = mininum.toString
    val maxp = sk.parameters.add
    maxp.name = "max"
    maxp.data = mininum.toString
    // supportedKey.par

  }

  def supportGetKeysOfType(t: String) = {
    this.supportedConfig.configValues.filter {
      case configKey if (configKey.keyType != null) =>
        configKey.keyType.toString == t
      case other => false
    }
  }

  /**
   * Get Support config definition from supported config
   */
  def supportGetKey(name: String) = {
    this.supportedConfig.configValues.find {
      supported =>
        supported.name.toString == name
    }
  }

  /**
   * Get Cnfigured value or defined default
   */
  def supportGetValue(name: String, keyType: String) = {

    supportGetKey(name) match {
      case None => None
      case Some(supportedKey) =>

        this.getKey(name, keyType) match {
          case Some(foundDefinedKey) =>
            Some(foundDefinedKey.values(0).toString)
          case None =>
            Some(supportedKey.default.toString())
        }
    }
  }

  /**
   * Get Cnfigured value or defined default
   */
  def supportGetString(name: String) = {
    supportGetKey(name) match {
      case None => None
      case Some(supportedKey) =>

        Some(this.getString(name, supportedKey.getStringDefault))
    }
  }

  def supportGetInt(name: String) = {
    supportGetKey(name) match {
      case None => None
      case Some(supportedKey) =>

        Some(this.getInt(name, supportedKey.getIntDefault))
    }
  }

  /**
   * Get Ints from keys, error if not found
   */
  def supportGetIntsOrError(names: Tuple2[String, String]): Tuple2[Int, Int] = {

    (
      supportGetInt(names._1).getOrElse(sys.error(s"Key ${names._1} not in supported list")),
      supportGetInt(names._2).getOrElse(sys.error(s"Key ${names._2} not in supported list")))

  }

  def supportGetDouble(name: String) = {
    supportGetKey(name) match {
      case None => None
      case Some(supportedKey) =>

        Some(this.getDouble(name, supportedKey.getDoubleDefault))
    }
  }

  def supportGetBoolean(name: String) = {
    supportGetKey(name) match {
      case None => None
      case Some(supportedKey) =>

        Some(this.getBoolean(name, supportedKey.getBooleanDefault))
    }
  }

  def supportGetLong(name: String) = {
    supportGetKey(name) match {
      case None => None
      case Some(supportedKey) =>

        Some(this.getLong(name, supportedKey.getLongDefault))
    }
  }

  // Values management
  //-------------------

  var valuesBufferCache = Map[String, AbstractDataBuffer[_]]()

  def getValueBufferFromCache[T <: AbstractDataBuffer[_]](str: String)(implicit tag: ClassTag[T]) = {
    this.valuesBufferCache.get(str) match {
      case Some(c: T) =>
        c
      case other =>
        var newBuffer = tag.runtimeClass.newInstance().asInstanceOf[T]
        this.valuesBufferCache = this.valuesBufferCache + (str -> newBuffer)
        newBuffer
    }

  }

  def isInConfig(keyType: String, value: String) = {
    this.values.keys.find {
      key => key.keyType === keyType && key.values.find { v => v === value }.isDefined
    }.isDefined
  }

  /**
   * Get first match for key name
   */
  def getKey(name: String): Option[CommonConfigTraitValuesKey] = {

    this.values.keys.find(_.name.toString() == name)

  }

  /**
   * Return key if a value is defined
   */
  def getKey(name: String, kType: String): Option[CommonConfigTraitValuesKey] = {
    this.values.keys.find(k => k.name.toString() == name && k.keyType.toString() == kType && k.values.size > 0)
  }

  def getKeyCreate(name: String, ktype: String) = getKey(name, ktype) match {
    case None =>
      addKey(name, ktype)
    case Some(k) => k
  }

  def getKeyValues(name: String, ktype: String) = getKey(name, ktype) match {
    case Some(k) => k.values.toList
    case None => List()
  }

  def setKeyFirstValue(name: String, kType: String, v: String) = {
    this.getKey(name, kType) match {
      case Some(key) if (key.values.size > 0) =>
        key.values(0).set(v)
      case Some(key) =>
        key.values.add.set(v)
      case None =>
        this.addKey(name, kType).values.add.set(v)
    }

    resyncToFile

  }

  def getKeyFirstValue(name: String, kType: String) = {
    this.getKey(name, kType) match {
      case Some(key) if (key.values.size > 0) =>
        Some(key.values(0).data)
      case other =>
        None
    }
  }

  def setUniqueKeyFirstValue(keyType: String, v: String) = {
    this.values.keys.find { k => k.keyType != null && k.keyType.toString() == keyType && k.values.size > 0 && k.values(0).toString == v } match {
      case None =>
        var k = this.values.keys.add
        k.keyType = keyType
        k.values.add.set(v)
      case other =>
    }
  }

  def hasString(name: String) = this.getKey(name, "string").isDefined

  def getString(name: String): Option[String] = getKeyFirstValue(name, "string")

  def getString(name: String, default: String): String = {
    this.getKey(name, "string") match {
      case Some(key) if (key.values.size > 0) =>
        try {
          key.values(0).toString()
        } catch {
          case e: Throwable =>
            default
        }
      case other => default
    }
  }

  def getStringAsBuffer(name: String, default: String) = {

    val value = this.getString(name, default)

    var b = new XSDStringBuffer
    b.set(value)
    b.onDataUpdate {
      setString(name, b.data)
    }

    b

  }

  def setString(name: String, v: String) = setKeyFirstValue(name, "string", v.toString)

  def getBoolean(name: String, default: Boolean) = {
    this.getKey(name, "boolean") match {
      case Some(key) if (key.values.size > 0) =>
        try {
          key.values(0).toString().toBoolean
        } catch {
          case e: Throwable =>
            default
        }
      case other => default
    }
  }

  def getBooleanAsBuffer(name: String, default: Boolean) = {

    val value = this.getKeyFirstValue(name, "boolean") match {
      case Some(v) => v.toBoolean
      case other => default
    }

    var b = new BooleanBuffer
    b.set(value)
    b.onDataUpdate {
      setBoolean(name, b.data)
    }

    b

  }

  def setBoolean(name: String, v: Boolean) = setKeyFirstValue(name, "boolean", v.toString)

  def hasInt(name: String) = this.getKey(name, "integer").isDefined

  def getInt(name: String): Option[Int] = getKeyFirstValue(name, "integer") match {
    case Some(is) => Some(is.toInt)
    case None => None
  }

  def getInt(name: String, default: Int): Int = {
    this.getKey(name, "integer") match {
      case Some(key) if (key.values.size > 0) =>
        try {
          key.values(0).toString().toInt
        } catch {
          case e: Throwable =>
            default
        }
      case other => default
    }
  }

  def getIntAsBuffer(name: String, default: Int, updateConfig: Boolean = false) = {
 
    /*val int =  this.hasInt(name) match {
     case true =>
       this.getInt(name, default)
     case false =>
   }
   */
    val value = this.getInt(name, default)

    var b = getValueBufferFromCache[IntegerBuffer](name)
    b.set(value)
    b.deregisterAllListenersFor("data.update")
    if (updateConfig) {
      b.onDataUpdate {

        setInt(name, b.data)
      }
    }
    /**/

    b

  }

  def setIntWatched(name: String, v: IntegerBuffer) = {
    this.setInt(name, v.data)
    v.onDataUpdate {
      println("Updating COnfig int")
      this.setInt(name, v.data)
    }
  }
  def setInt(name: String, v: Int) = setKeyFirstValue(name, "integer", v.toString)

  def getLong(name: String, default: Long) = {
    this.getKey(name, "long") match {
      case Some(key) if (key.values.size > 0) =>
        try {
          key.values(0).toString().toLong
        } catch {
          case e: Throwable =>
            default
        }
      case other => default
    }
  }

  def setLong(name: String, v: Long) = setKeyFirstValue(name, "long", v.toString)

  def getDouble(name: String, default: Double) = {

    this.getKeyFirstValue(name, "double") match {
      case Some(v) => v.toDouble
      case other => default
    }
  }

  def setDouble(name: String, v: Double) = setKeyFirstValue(name, "double", v.toString)

  def getDoubleAsBuffer(name: String, default: Double) = {

    val value = this.getKeyFirstValue(name, "double") match {
      case Some(v) => v.toDouble
      case other => default
    }

    var b = new DoubleBuffer
    b.set(value)
    b.onDataUpdate {

      //println("Value of config updated")
      setDouble(name, b.data)
    }

    b

  }

  def getDoubleRangeAsBuffer(name: String, minDefault: Double, maxDefault: Double) = {

  }

  /**
   * Get All keys answering to name and type
   */
  def getKeys(name: String, t: String) = {
    this.values.keys.filter {
      case key if (key.name != null && key.name === name && key.keyType != null && key.keyType === t) => true
      case _ => false
    }
  }

  def getKeysByType(t: String) = {

    this.values.keys.filter {
      k => k.keyType != null && k.keyType.toString() == t
    }

  }

  //def getKeysValues(name:String,t:String)

  def removeFromConfig(keyType: String, value: String): Boolean = {
    this.values.keys.find {
      key => key.keyType === keyType
    } match {
      case Some(key) =>
        key.values.find { v => v === value } match {
          case Some(valueNode) =>
            key.values -= valueNode
            true
          case None => false
        }
      case None => false

    }
  }

  def addKey(name: String, t: String) = {

    var key = this.values.keys.add
    key.keyType = t
    key.name = name
    key

  }

  /**
   * Replace key
   */
  def setKeyAndValues(name: String, keyType: String, values: String*) = {

    //-- Create key or clean
    val key = this.values.keys.find(_.name.toString() == name) match {
      case Some(k) =>
        k.keyType = keyType
        k.values.clear()
        k
      case None =>
        this.addKey(name, keyType)
    }

    //-- Add values
    values.foreach {
      v =>

        val keyValue = key.values.add
        keyValue.data = v
    }

  }
}