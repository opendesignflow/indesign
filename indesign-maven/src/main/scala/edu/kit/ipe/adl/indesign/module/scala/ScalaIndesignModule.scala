package edu.kit.ipe.adl.indesign.module.scala

import edu.kit.ipe.adl.indesign.core.harvest.Harvester
import edu.kit.ipe.adl.indesign.core.brain.Brain
import edu.kit.ipe.adl.indesign.core.module.buildsystem.ModuleSourceFile
import edu.kit.ipe.adl.indesign.module.maven.region.MavenExternalBrainRegion

class ScalaIndesignModuleHarvester extends Harvester {

  this.onDeliverFor[ScalaSourceFile] {

    case r =>

      //println(s"Delivering to scala harvester -> "+r.path+" -> "+r.path.toString.endsWith(".scala") )
      //println(s"Lines: "+r.getLines)

      r.getLines.find { line => """extends\s+IndesignModule""".r.findFirstIn(line).isDefined }.isDefined match {
        case true =>
          //logFine(s"Found Module")
          //println(s"Accepted: "+r)
          gather(new ScalaIndesignModule(r))
          true
        case _ =>
          //println(s"Rejected: "+r)
          false
      }

  }

}

class ScalaIndesignModule(s: ScalaSourceFile) extends ScalaSourceFile(s.path) with ModuleSourceFile {
  deriveFrom(s)

  override def getDisplayName = {
    this.getMainType.get
  }

  this.onGathered {
    case h =>

      //println("***Gathered InDesign Module 2, discovered: "+getDiscoveredModules);

      // Look for region with same ID as project.
      // If existing, record Module as available
      this.getUpchainCompilingProject match {
        case Some(project) =>

         // println("COmpiling Project: "+project.getId)
          Brain.getResourcesOfLazyType[MavenExternalBrainRegion].find {
            r => 
             // println("Maven region: "+r.getId)
              r.getId == project.getId 
          } match {
            case Some(region) =>
              
              //println("Found Region to deliver module to ");
             
              region.addDerivedResource(this)
            case None => 
          }

        case None =>
      }
      //println("Cimpiling Project: " + this.getUpchainCompilingProject)

  }
  
  def getDiscoveredModules : List[String] = {
    this.getDefinedObjects
  }

}