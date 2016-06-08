package edu.kit.ipe.adl.indesign.module.git.gitlab

import edu.kit.ipe.adl.indesign.core.harvest.Harvester
import edu.kit.ipe.adl.indesign.core.harvest.HarvestedResource

class GitlabHarvester extends Harvester {
  
  
}

class GitlabProjectResource(model:GitlabTraitGitlabProject) extends HarvestedResource {
  
  def getId = model.id
  
  
  
}