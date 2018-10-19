package org.indesign.boot.model

import com.idyria.osi.ooxoo.core.buffers.id.IdAndRefIdModelBuilder
import com.idyria.osi.ooxoo.model.ModelBuilder
import com.idyria.osi.ooxoo.model.producers
import com.idyria.osi.ooxoo.model.producer
import com.idyria.osi.ooxoo.model.out.scala.ScalaProducer


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