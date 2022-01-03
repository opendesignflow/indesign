package org.odfi.indesign.module.git.gitlab

import org.odfi.ooxoo.model.ModelBuilder
import org.odfi.ooxoo.model.producer
import org.odfi.ooxoo.model.producers
import org.odfi.ooxoo.model.out.markdown.MDProducer
import org.odfi.ooxoo.model.out.scala.ScalaProducer
import org.odfi.ooxoo.core.buffers.structural.io.sax.STAXSyncTrait

@producers(Array(
  new producer(value = classOf[ScalaProducer]),
  new producer(value = classOf[MDProducer])))
class GitLabModel extends ModelBuilder {
  
  "Gitlab" is {
    elementsStack.head.makeTraitAndUseCustomImplementation
  
    "GitlabURL" ofType("url")
    "APIBase" ofType("string") default("https://gitlab.example.com/api/v3/")
    "PrivateToken" ofType("string")
    
    "GitlabProject" multiple {
      attribute("id")
    }
  }
  
  
}