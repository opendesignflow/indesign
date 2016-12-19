package edu.kit.ipe.adl.indesign.core.module.ui.www.stream



import com.idyria.osi.ooxoo.model.ModelBuilder
import com.idyria.osi.ooxoo.model.producers
import com.idyria.osi.ooxoo.model.producer
import com.idyria.osi.ooxoo.model.out.markdown.MDProducer
import com.idyria.osi.ooxoo.model.out.scala.ScalaProducer

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