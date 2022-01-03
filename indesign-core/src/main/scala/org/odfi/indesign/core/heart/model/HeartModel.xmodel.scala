package org.odfi.indesign.core.heart.model
import org.odfi.ooxoo.model.ModelBuilder
import org.odfi.ooxoo.model.producer
import org.odfi.ooxoo.model.producers
import org.odfi.ooxoo.model.out.markdown.MDProducer
import org.odfi.ooxoo.model.out.scala.ScalaProducer

@producers(Array(
  new producer(value = classOf[ScalaProducer]),
  new producer(value = classOf[MDProducer])))
class HeartModel extends ModelBuilder {
  
  
  "HeartTaskStatus" is {
    
    "ID" ofType("string")
    "State" ofType ("string")
    
  }
  
}