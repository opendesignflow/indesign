package edu.kit.ipe.adl.indesign.fastbuild

import edu.kit.ipe.adl.indesign.core.brain.Brain
import edu.kit.ipe.adl.indesign.core.main.IndesignPlatorm
import edu.kit.ipe.adl.indesign.core.harvest.fs.FileSystemHarvester
import java.io.File
import edu.kit.ipe.adl.indesign.core.harvest.Harvest
import edu.kit.ipe.adl.indesign.core.module.ui.www.IndesignWWWUIModule
import com.idyria.osi.tea.logging.TeaLogging
import com.idyria.osi.tea.logging.TLog
import com.idyria.osi.tea.files.FileWatcherAdvanced

object FastBuildTest extends App {
 

  
  //TLog.setLevel(classOf[FileWatcherAdvanced], TLog.Level.FULL)
  
  //-- Setup Inddesign Main 
  IndesignPlatorm.prepareDefault
  
  //--  add module
  Brain.deliverDirect(FBModule)
  
  
  //-- Add Test FS
  var fsh = new FileSystemHarvester
  fsh.addPath(new File("src/test/testFS"))
  Harvest.addHarvester(fsh)
  
  
  //-- Add UI
  Brain.deliverDirect(IndesignWWWUIModule)
  
  IndesignPlatorm.start
  
  
}