package org.odfi.indesign.core.module.filestash

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
class FileStashModel extends ModelBuilder with IdAndRefIdModelBuilder {

  "FileStash" is {
    makeTraitAndUseCustomImplementation
    
    "BasePath" ofType "string"
    
    "Owner" multiple {
      makeTraitAndUseCustomImplementation
      requestContainerReference
      withElementID
      


      "Stash" multiple {
        
        requestContainerReference
        withElementID
        
        makeTraitAndUseCustomImplementation
        
        
        "CreationDate" ofType("datetime")
        "Validity" ofType("datetime")
        "FilesCount" ofType("integer") default("0")
        
        "AccessTicket" multiple {
          withElementID
          
          attribute("canWrite") ofType("boolean")
          attribute("validity") ofType("datetime")
          
        }
        
      }

    }

  }
}