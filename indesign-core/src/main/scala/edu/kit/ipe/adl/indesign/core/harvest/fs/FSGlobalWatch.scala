package edu.kit.ipe.adl.indesign.core.harvest.fs

import com.idyria.osi.tea.files.FileWatcherAdvanced

object FSGlobalWatch {
  
  var watcher = new FileWatcherAdvanced
  
  watcher.start
  
}