package org.odfi.indesign.core.module.artifactresolver

import org.eclipse.aether.artifact.Artifact
import org.odfi.indesign.core.brain.{Brain, BrainRegion}
import org.odfi.indesign.core.harvest.Harvest
import org.odfi.tea.compile.ClassDomainContainer
import org.odfi.tea.logging.TLogSource

trait ArtifactRegion extends ClassDomainContainer with TLogSource with BrainRegion {

  def getRegionArtifact: Artifact
  def getRegionDependencies: List[Artifact]
  var regionResolutionDone = false

  def rebuildDependencies = {
    
  }
  
  /**
   * Try to create a classdomain container hierarchy based on all the ArtifactRegions available
   */
  def resolveRegionClassDomainHierarchy: Unit = {

    regionResolutionDone match {
      case true =>
      case false =>

        //-- Get Artifact regions
        var foundArtifactRegions = Harvest.collectResourcesOnHarvesters[Brain, ArtifactRegion, ArtifactRegion] {
          case mp if (mp != this) => mp
        }
        logFine[ArtifactRegion](s"($this) Found Candidate Regions to be in parent ClassDomain" + foundArtifactRegions)

        //-- Look in local dependencies for one or more region

        var foundDependend = foundArtifactRegions.filter {
          region =>
            var localRegionArtifact = region.getRegionArtifact
            this.getRegionDependencies.find {
              dependency =>
                var res = (dependency.getArtifactId == localRegionArtifact.getArtifactId) &&
                  (dependency.getGroupId == localRegionArtifact.getGroupId) &&
                  (dependency.getVersion == localRegionArtifact.getVersion)
                logFine[ArtifactRegion](s"($this) Comparing $localRegionArtifact with $dependency -> $res")
                res

            }.isDefined
        }
        logFine[ArtifactRegion](s"($this) Found Dependend Regions to be in parent ClassDomain" + foundDependend)
        logFine[ArtifactRegion](s"($this) Region dependencies are " + this.getRegionDependencies)
        foundDependend.size match {
          // Stop here; nothing to be done
          case 0 =>
            logFine[ArtifactRegion](s"($this) No Dependend regions in dependencies")
          case _ =>

            //-- Make sure all found dependenecies have their own classdomain hierarchy resolved
            logFine[ArtifactRegion](s"($this) Resolve Hierarchy on candidate regions")
            foundDependend.foreach(_.resolveRegionClassDomainHierarchy)
            logFine[ArtifactRegion](s"($this) Done Resolve Hierarchy on candidate regions")

            //-- Now Filter in dependencies all that are parent of another one, we only want the leaves
            var leaves = foundDependend.filter { r => foundDependend.find { or => r.isChild(or) }.isEmpty }

            //-- Depending on remaining leaves, act
            leaves.size match {
              case 0 =>
                logFine[ArtifactRegion](s"($this) No Leaves in region dependency")
              case 1 =>
                this.changeParentClassDomainContainer(leaves.head)
              case other =>
                logWarn[ArtifactRegion](s"Region $this depends on more than one other regions, need to try to resolve them in tree")
              // Here: Try to put all regions in a tree, if two have parents of separate branches, then it can't be solved
            }
        }

        logFine[ArtifactRegion](s"($this) Current Class domain: " + this.classdomain)
        logFine[ArtifactRegion](s"($this) Parent  Class domain: " + this.getParentClassDomain)
        this.regionResolutionDone = true
      /*var foundDependend = foundMavenRegions.filter {
      region =>
        this.getDependencies.find {
          dependency =>
            region.projectModel.is(dependency)

        }.isDefined
    }
    logFine[ArtifactRegion](s"($this) Found Candidate Regions to be in parent ClassDomain" + foundDependend)
    foundDependend.size match {
      case 0 =>
      case 1 =>
        this.changeParentClassDomainContainer(foundDependend.head)
      case other =>
        logWarn[ArtifactRegion](s"($this) Region dependends on more than other other, solution not implemented yet")
    }*/

    }
  }

}