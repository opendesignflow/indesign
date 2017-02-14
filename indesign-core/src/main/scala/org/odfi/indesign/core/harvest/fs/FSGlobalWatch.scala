package org.odfi.indesign.core.harvest.fs

import com.idyria.osi.tea.files.FileWatcherAdvanced
import org.odfi.indesign.core.module.IndesignModule

object FSGlobalWatch extends IndesignModule {
  
  var watcher = new FileWatcherAdvanced
  
  this.onStart {
    watcher.start
  }
  
  this.onStop {
    watcher.stop
  }
  
  
}