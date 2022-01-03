package org.odfi.indesign.core.module.stream
import org.odfi.ooxoo.model.ModelBuilder
import org.odfi.ooxoo.model.producer
import org.odfi.ooxoo.model.producers
import org.odfi.ooxoo.model.out.markdown.MDProducer
import org.odfi.ooxoo.model.out.scala.ScalaProducer
import org.odfi.ooxoo.core.buffers.structural.io.sax.STAXSyncTrait

@producers(Array(
  new producer(value = classOf[ScalaProducer]),
  new producer(value = classOf[MDProducer])))
class StreamModel extends ModelBuilder {
  
  "StreamUpdate" is {
     attribute("streamID")
     
     "Content" ofType("cdata")
  }
  
  "StreamOpen" is {
    attribute("streamID")
  }
  "StreamClose" is {
    attribute("streamID")
  }
  
  
}