package org.odfi.indesign.core.module.java

object TestSearchJava extends App {
  
  
  JavaHarvester.harvest
  JavaHarvester.getResourcesOfType[JavaInstallation].foreach {
    inst => 
      println("Found java at: "+inst)
  }
  
  
}