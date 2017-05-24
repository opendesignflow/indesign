package org.odfi.indesign.core.harvest.fs

import java.io.File


/**
 * This trait contains a few functions to easily listen on changes on a folder like a build output folder
 */
trait FolderChangeListener {
  
  
  var folderChangeListeners = Map[String,(Int,List[(Any,(File => Any))])]()
  
  def listenOnFolderChange(key:String,beautyTime:Int = 0,folder:File) = {
    FSGlobalWatch.watcher.watchDirectoryRecursive(this, folder) {
      f => 
        
    }
  }
  
}