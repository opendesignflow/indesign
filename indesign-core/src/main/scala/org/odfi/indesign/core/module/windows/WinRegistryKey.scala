package org.odfi.indesign.core.module.windows

import org.odfi.indesign.core.harvest.HarvestedResource

import scala.sys.process._

class WinRegistryKey(val path: String) extends HarvestedResource {

  def getId = path

  def create = {

    var cmd = s"""powershell.exe -Command "Start-Process reg 'add $path /f' -Verb RunAs""""
    //println("Using cmd: "+cmd)
    var p = Process(cmd)
    var res = p.!<(WinRegistryKey.nullLogger)
    res match {
      case 0 => true
      case other => false
    }
  }

  def exists = {
    var p = Process(s"""reg query $path""")
    var res = p.!<(WinRegistryKey.nullLogger)
    res match {
      case 0 => true
      case other => false
    }
  }

  def getDefault: Option[String] = {
    try {
      var lines = Process(s"""reg query $path /ve""").lineStream
      lines.find { line => line.contains("(Default)") } match {
        case Some(line) => Some(line.split(" ").last)
        case None => None
      }
    } catch {
      case e: Throwable => None
    }
  }

  def setDefault(str: String) = {

    //Process(s"""reg delete $path /t REG_SZ /v (Default) /f /d $str""")
    var cmd = s"""powershell.exe -Command "Start-Process reg 'add $path /t REG_SZ /ve /f /d \\"$str\"' -Verb RunAs""""
    println("Using cmd: " + cmd)
    var p = Process(cmd)
    var res = p.!<(WinRegistryKey.nullLogger)
    res match {
      case 0 => true
      case other => false
    }

  }

  def setValue(name: String, str: String) = {

    //Process(s"""reg delete $path /t REG_SZ /v (Default) /f /d $str""")
    var cmd = s"""powershell.exe -Command "Start-Process reg 'add $path /t REG_SZ /v $name /f /d \\"$str\"' -Verb RunAs""""
    println("Using cmd: " + cmd)
    var p = Process(cmd)
    var res = p.!<(WinRegistryKey.nullLogger)
    res match {
      case 0 => true
      case other => false
    }

  }

  /**
   * Returns list of strings
   */
  def queryAllValuesRecursive = {
    try {
      Process(s"""reg query $path /s""").lineStream.toList
    } catch {
      case e: Throwable => throw e
    }
  }

}

object WinRegistryKey {

  val nullLogger = ProcessLogger {
    line =>
  }

}
