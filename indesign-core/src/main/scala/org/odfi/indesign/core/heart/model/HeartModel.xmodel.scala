package org.odfi.indesign.core.heart.model
import com.idyria.osi.ooxoo.model.ModelBuilder
import com.idyria.osi.ooxoo.model.producer
import com.idyria.osi.ooxoo.model.producers
import com.idyria.osi.ooxoo.model.out.markdown.MDProducer
import com.idyria.osi.ooxoo.model.out.scala.ScalaProducer

@producers(Array(
  new producer(value = classOf[ScalaProducer]),
  new producer(value = classOf[MDProducer])))
class HeartModel extends ModelBuilder {
  
  
  "HeartTaskStatus" is {
    
    "ID" ofType("string")
    "State" ofType ("string")
    
  }
  
}