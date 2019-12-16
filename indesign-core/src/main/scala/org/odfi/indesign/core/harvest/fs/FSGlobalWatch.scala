package org.odfi.indesign.core.harvest.fs

import org.odfi.tea.files.FileWatcherAdvanced
import org.odfi.indesign.core.module.IndesignModule

object FSGlobalWatch extends IndesignModule {
  
  var watcher = new FileWatcherAdvanced
  
  var idWatcher = new IDFileWatcher
  
  this.onStart {
    
    println("Starting FS Global Watch")
    
    watcher.start
    idWatcher.start
  }
  
  this.onStop {
    watcher.stop
    idWatcher.stop
  }
  
  
}