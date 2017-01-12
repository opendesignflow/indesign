package org.odfi.indesign.module.maven.utils

class Version {

  var versionComponents = List[VersionComponent]()

  def addVersionComponent(v: VersionComponent) = {
    this.versionComponents = this.versionComponents :+ v
  }

  def getMajor = this.versionComponents.headOption match {
    case Some(c) if (c.isDigit) => c.toDigit
    case _ => -1
  }

  def getMinor = this.versionComponents.drop(1).headOption match {
    case Some(c) if (c.isDigit) => c.toDigit
    case _ => -1
  }

  def getPatch = this.versionComponents.drop(2).headOption match {
    case Some(c) if (c.isDigit) => c.toDigit
    case _ => -1
  }

  def getRelease = this.versionComponents.drop(3).headOption match {
    case Some(c) if (c.isDigit) => c.toDigit
    case _ => -1
  }

  def getTailComponent = this.versionComponents.lastOption match {
    case Some(c) => c.base
    case _ => ""
  }

  def >(other: Version) = {

    other match {
      case other if (getMajor > other.getMajor) => true
      case other if (getMajor < other.getMajor) => false

      case other if (getMinor > other.getMinor) => true
      case other if (getMinor < other.getMinor) => false

      case other if (getPatch > other.getPatch) => true
      case other if (getPatch < other.getPatch) => false

      case other if (getRelease > other.getRelease) =>
        //println("True Testing release: " + getRelease + " against " + other.getRelease)
        true
      case other if (getRelease < other.getRelease) =>

       // println("False Testing release: " + getRelease + " against " + other.getRelease)
        false

      case other if (getTailComponent == "" && other.getTailComponent != "") => false
      case other if (getTailComponent != "" && other.getTailComponent == "") => true
      case other if (other.getTailComponent != "" && getTailComponent != "") => other.getTailComponent.compareTo(getTailComponent) match {
        case res if (res > 0) => true
        case _ => false
      }

      case _ => false
    }

  }

  override def toString = versionComponents.map(c => c.base).mkString(".")

}

class VersionComponent(val base: String) {

  def isDigit = try {
    base.toLong
    true
  } catch {
    case e: Throwable =>
      false
  }

  def toDigit = base.toLong
}

object Version {

  val versionString = """(?:([\d]+)\.?)+(.+)?""".r
  val versionComponent = """(?:([\d]+)\.?)|(.+)""".r

  def apply(str: String): Version = {

    //-- Parse String
    //println("s: "+str.trim.matches("""^([0-9]+.?)+(.+)?$""""))
    versionComponent.findAllMatchIn(str) match {
      case it if (!it.hasNext) => sys.error(s"Version string $str must be of format MAJOR.MINOR.PATCH.RELEASE....")
      case it =>

        var v = new Version

        it.foreach {
          case elt if (elt.group(1) == null) => v.addVersionComponent(new VersionComponent(elt.group(0)))
          case elt => v.addVersionComponent(new VersionComponent(elt.group(1)))

        }

        /*(1 to m.groupCount).foreach {
          i => 
            println("Found group: "+m.group(i))
             v.addVersionComponent(new VersionComponent(m.group(i)))
        }*/

        v

    }

  }

}