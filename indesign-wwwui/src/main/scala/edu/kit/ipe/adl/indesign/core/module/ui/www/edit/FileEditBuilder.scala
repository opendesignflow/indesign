package edu.kit.ipe.adl.indesign.core.module.ui.www.edit

import edu.kit.ipe.adl.indesign.core.module.ui.www.edit.ace.ACEEditorBuilder
import edu.kit.ipe.adl.indesign.core.harvest.fs.HarvestedTextFile

trait FileEditBuilder extends ACEEditorBuilder {

  def fileEditor(targetFile: Option[HarvestedTextFile], height: Int, language: String) = {

    "indesign-edit-fileditor" :: div {

      // Use ACE Editor
      //----------------------

      // Control
      "control" :: div {

        "ui button" :: button("Save File") {

        }
      }

      // Control specific
      "control-extra" :: div {

      }

      // Editor
      "ui grid" :: div {

        "ten wide column" :: div {
          aceEditor(language, "100%", height.toString, targetFile.get.getTextContent) {

          }
        }
        
        "six wide column right-pane" :: div {
          
        }

      }

    }

  }

}

