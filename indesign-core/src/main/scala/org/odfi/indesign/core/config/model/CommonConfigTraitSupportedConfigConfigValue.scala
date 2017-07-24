package org.odfi.indesign.core.config.model

import com.idyria.osi.ooxoo.core.buffers.datatypes.DoubleBuffer

class CommonConfigTraitSupportedConfigConfigValue extends CommonConfigTraitSupportedConfigConfigValueTrait {

  def isRange = this.hint != null && this.hint.toString == "range"
  def isString = this.keyType.toString=="string"
  def isDouble = this.keyType.toString=="double"
  def isBoolean = this.keyType.toString=="boolean"
  def isInteger = this.keyType.toString=="integer"
  def isLong = this.keyType.toString=="long"
  
  def getDoubleDefault = isDouble match {
    case true if (default!=null) => default.toString.toDouble
    case other => sys.error(s"Not a double type or no default (default=$default)")
  }
  def getIntDefault = isInteger match {
    case true if (default!=null) => default.toString.toInt
    case other => sys.error(s"Not an integer type or no default (default=$default)")
  }
  def getLongDefault = isLong match {
    case true if (default!=null) => default.toString.toLong
    case other => sys.error(s"Not a long type or no default (default=$default)")
  }
  def getBooleanDefault = isBoolean match {
    case true if (default!=null) => default.toString.toBoolean
    case other => sys.error(s"Not a boolean type or no default (default=$default)")
  }
  def getStringDefault = isString match {
    case true if (default!=null) => default.toString
    case other => sys.error(s"Not a string type or no default (default=$default)")
  }
  
  
  def getDoubleRange = isRange match {
    case false => None
    case true =>
      Some((DoubleBuffer.convertFromString(this.parameters.find(p => p.name.toString == "min").toString()),
        DoubleBuffer.convertFromString(this.parameters.find(p => p.name.toString == "max").toString())))
  }
  
  
}