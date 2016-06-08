package edu.kit.ipe.adl.indesign.module.git.gitlab

import edu.kit.ipe.adl.indesign.module.git.GitModule
import edu.kit.ipe.adl.indesign.core.config.Config
import edu.kit.ipe.adl.indesign.core.config.DefaultMemoryConfigImplementation

object TryKITGitlab extends App {
  
  //-- Set Dummy Config
  Config.setImplementation(new DefaultMemoryConfigImplementation)
  
  //-- Git Module
  var config = GitModule.config.get
  var gitlab = new Gitlab
  gitlab.gitlabURL = "http://ipe-iperic-srv1:8082/"
  gitlab.privateToken = "6GzyPsbJH7uSJbexsc7s"
  
  config.custom.content += gitlab
  
  gitlab.listProjects
  
  
}