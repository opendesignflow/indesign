package org.odfi.indesign.core.module.stats

import org.odfi.ooxoo.model.ModelBuilder
import org.odfi.ooxoo.model.producer
import org.odfi.ooxoo.model.producers
import org.odfi.ooxoo.model.out.markdown.MDProducer
import org.odfi.ooxoo.model.out.scala.ScalaProducer
import org.odfi.ooxoo.core.buffers.structural.io.sax.STAXSyncTrait
import org.odfi.ooxoo.core.buffers.id.IdAndRefIdModelBuilder

@producers(Array(
  new producer(value = classOf[ScalaProducer]),
  new producer(value = classOf[MDProducer])))
class StatsModel extends ModelBuilder with IdAndRefIdModelBuilder {

    /**
     * Add standard elements for statistics gathering
     */
  "StatsSupport" is {
      makeTraitAndUseCustomImplementation
      
      "Stats" is {
          
          "Stat" multiple {
              withElementID
              attribute("lastUpdated") ofType("datetime")
              attribute("dataType")
              attribute("value")
              attribute("history") ofType("boolean") default("false")
              "History" multiple {
                   attribute("value")
                   attribute("savedDate") ofType("datetime")
              }
          }
      }
  }
}