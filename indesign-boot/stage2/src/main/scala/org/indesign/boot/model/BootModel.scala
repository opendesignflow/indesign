package org.indesign.boot.model

import org.odfi.ooxoo.core.buffers.id.IdAndRefIdModelBuilder
import org.odfi.ooxoo.model.ModelBuilder
import org.odfi.ooxoo.model.producers
import org.odfi.ooxoo.model.producer
import org.odfi.ooxoo.model.out.scala.ScalaProducer


@producers(Array(
    new producer(value = classOf[ScalaProducer])
))
object BootModel extends ModelBuilder with IdAndRefIdModelBuilder  {
 
  "Boot" is {
      
      "Project" multiple {
          
          "artifactId" ofType("string")
          "groupId" ofType("string")
          "version" ofType("string")
          
          
      }
      
  }
    
    
}