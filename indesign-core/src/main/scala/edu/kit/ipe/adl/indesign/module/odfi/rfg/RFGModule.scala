package edu.kit.ipe.adl.indesign.module.odfi.rfg

import edu.kit.ipe.adl.indesign.core.module.IndesignModule
import edu.kit.ipe.adl.indesign.core.harvest.fs.FileSystemHarvester
import edu.kit.ipe.adl.indesign.module.maven.POMFileHarvester
import edu.kit.ipe.adl.indesign.module.tcl.TCLFileHarvester
import edu.kit.ipe.adl.indesign.core.module.ui.www.WWWViewHarvester
import edu.kit.ipe.adl.indesign.module.odfi.rfg.www.RFGMainView

object RFGModule extends IndesignModule {
  
  def load = {
   // TCLFileHarvester.addChildHarvester(new RFGScriptFileHarvester)
  }
  
  
  this.onInit {
    //WWWViewHarvester.deliverDirect(new RFGMainView)
  }
  
}