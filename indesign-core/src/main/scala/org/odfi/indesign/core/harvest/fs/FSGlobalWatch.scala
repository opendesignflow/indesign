package org.odfi.indesign.core.harvest.fs

import com.idyria.osi.tea.files.FileWatcherAdvanced

object FSGlobalWatch {
  
  var watcher = new FileWatcherAdvanced
  
  watcher.start
  
}