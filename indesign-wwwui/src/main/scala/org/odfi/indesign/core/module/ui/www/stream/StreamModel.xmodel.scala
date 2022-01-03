package org.odfi.indesign.core.module.ui.www.stream



import org.odfi.ooxoo.model.ModelBuilder
import org.odfi.ooxoo.model.producers
import org.odfi.ooxoo.model.producer
import org.odfi.ooxoo.model.out.markdown.MDProducer
import org.odfi.ooxoo.model.out.scala.ScalaProducer

@producers(Array(
  new producer(value = classOf[ScalaProducer]),
  new producer(value = classOf[MDProducer])))
object StreamModel extends ModelBuilder {
  
  "StreamMessageTrait" is {
    "ID" ofType "string"
  }
  
  "StreamCreate" is {
    withTrait("StreamMessageTrait")
    "Name" ofType "string"
  }
  
  "StreamUpdate" is {
    withTrait("StreamMessageTrait")
    attribute("line") ofType "boolean" default "false"
    "Text" ofType("cdata")
  }
  
   "StreamParameter" is {
    withTrait("StreamMessageTrait")
    "Name" ofType "string"
    "Value" ofType("string")
  }
  
}