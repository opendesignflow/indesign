package org.odfi.indesign.core.module.buildsystem

import org.odfi.indesign.core.harvest.fs.HarvestedFile

trait SourceFile extends HarvestedFile {
  
   def onChange(cl: => Unit) : Unit
  
}

trait JavaSourceFile extends SourceFile {
  
  def ensureCompiled
  def loadClass: Class[_]
}