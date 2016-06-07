package edu.kit.ipe.adl.indesign.core.module.buildsystem

trait ModuleSourceFile extends SourceFile {
  
  def getDiscoveredModules : List[String]
}