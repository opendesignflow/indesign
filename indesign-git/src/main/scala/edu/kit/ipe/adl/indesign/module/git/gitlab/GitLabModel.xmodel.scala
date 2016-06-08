package edu.kit.ipe.adl.indesign.module.git.gitlab

import com.idyria.osi.ooxoo.model.ModelBuilder
import com.idyria.osi.ooxoo.model.producer
import com.idyria.osi.ooxoo.model.producers
import com.idyria.osi.ooxoo.model.out.markdown.MDProducer
import com.idyria.osi.ooxoo.model.out.scala.ScalaProducer
import com.idyria.osi.ooxoo.core.buffers.structural.io.sax.STAXSyncTrait

@producers(Array(
  new producer(value = classOf[ScalaProducer]),
  new producer(value = classOf[MDProducer])))
object GitLabModel extends ModelBuilder {
  
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