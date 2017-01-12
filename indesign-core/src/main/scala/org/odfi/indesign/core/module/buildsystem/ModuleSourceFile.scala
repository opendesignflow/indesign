package org.odfi.indesign.core.module.buildsystem

trait ModuleSourceFile extends SourceFile {
  
  def getDiscoveredModules : List[String]
}