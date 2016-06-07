package edu.kit.ipe.adl.indesign.core.module.buildsystem

import edu.kit.ipe.adl.indesign.core.harvest.fs.HarvestedFile

trait SourceFile extends HarvestedFile {
  
   def onChange(cl: => Unit) : Unit
  
}

trait JavaSourceFile extends SourceFile {
  
  def ensureCompiled
  def loadClass: Class[_]
}