package edu.kit.ipe.adl.indesign.core.module.ui.www.edit

import edu.kit.ipe.adl.indesign.core.module.ui.www.edit.ace.ACEEditorBuilder
import edu.kit.ipe.adl.indesign.core.harvest.fs.HarvestedTextFile
import edu.kit.ipe.adl.indesign.core.module.ui.www.layout.UILayoutBuilder

trait FileEditBuilder extends ACEEditorBuilder with UILayoutBuilder {

  def fileEditor(targetFile: HarvestedTextFile, language: String, rightPane: Boolean = false, eid: String = "main-editor")(cl: => Any) = {

    "indesign-edit-fileditor" :: div {

      +@("cansave" -> targetFile.canWrite.toString())
      // Use ACE Editor
      //----------------------

      // Control
      "control" :: div {

        //-- Save File
        targetFile.canWrite match {
          case true =>
            "ui button" :: button("Save File") {

            }
          case false =>
            "ui warning message" :: div(text("Text in Read Only Mode"))
        }

        //-- Edit Control
        "ui button" :: button("Lock Editor") {

        }

      }

      // Control specific
      "control-extra" :: div {

      }

      // Editor
      var leftWidth = rightPane match {
        case false => "sixteen"
        case true => "ten"
      }
      var rightWidth = rightPane match {
        case true => "six"
        case false => "0"
      }
      "ui grid" :: div {

        s"$leftWidth wide column" :: div {
          
          var aceEditor = aceFastEditor(language, targetFile.getTextContent) {
            classes("indesign-layout-consumeRemainingBodyHeight")
            id(eid)
          }
          
         

         

        }
        rightPane match {
          case true =>
            "six wide column right-pane" :: div {

            }
          case false =>
        }

      }

      // Finish with local closure
      cl

    }

  }

}

