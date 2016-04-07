package edu.kit.ipe.adl.indesign.module.odfi.rfg

import edu.kit.ipe.adl.indesign.core.harvest.Harvester
import edu.kit.ipe.adl.indesign.core.harvest.fs.HarvestedFile

class RFGScriptFileHarvester extends Harvester {


  /**
   * Reacts on pom.xml file
   */
  this.onDeliverFor[HarvestedFile] {

    case r =>

      //if (r.

      // Look for content 
      //----------------
      r.getLines.find { l => l.contains("odfi::rfg::registerFile") } match {

        // Store this file as containing a Register File 
        case Some(rfLine) =>

          true
        case None =>
          false
      }

  }
}