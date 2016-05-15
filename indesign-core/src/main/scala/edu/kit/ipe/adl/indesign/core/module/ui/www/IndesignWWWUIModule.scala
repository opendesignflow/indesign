package edu.kit.ipe.adl.indesign.core.module.ui.www

import edu.kit.ipe.adl.indesign.core.module.IndesignModule
import com.idyria.osi.wsb.webapp.localweb.LocalWebEngine
import javafx.stage.Stage
import javafx.scene.Scene
import javafx.scene.paint.Color
import javafx.scene.control.Hyperlink
import javafx.event.EventHandler
import javafx.stage.WindowEvent
import com.sun.deploy.uitoolkit.impl.fx.HostServicesFactory
import com.idyria.osi.wsb.webapp.localweb.LocalWebHTMLVIewCompiler
import com.idyria.osi.wsb.webapp.localweb.LocalWebHTMLVIew
import edu.kit.ipe.adl.indesign.core.harvest.Harvest

object IndesignWWWUIModule extends IndesignModule {

  def load = {

    //--- Create Engine
    var wwwEngine = LocalWebEngine

    //-- Create GUI TO help
    //JavaFXRun.on
    /*JavaFXRun.onJavaFX {

      var hostServices = HostServicesFactory.getInstance(JavaFXRun.application)

      //-- Create Stage 
      var stage = new Stage
      stage.setWidth(1024)
      stage.setHeight(768)
      var group = new javafx.scene.Group();
      var scene = new Scene(group, Color.WHITE);
      stage.setScene(scene)

      //-- Add Text With Link
      var link = new Hyperlink();
      link.setText(s"http://localhost:${wwwEngine.httpConnector.port}/")
      new JavaFXNodeDelegate[Hyperlink, JavaFXNodeDelegate[Hyperlink, _]](link).onClicked {
        x => 
          println(s"Clicked")
          hostServices.showDocument(link.getText)
      }
      group.getChildren.add(link)

      stage.show()
      stage.setOnCloseRequest(new EventHandler[WindowEvent] {
        def handle(e: WindowEvent) = {
          //stage.
          wwwEngine.lStop

        }
      })

      println(s"Done UI")

    }*/
    
    //-- Set Front View 
    /*var frontView = LocalWebHTMLVIewCompiler.createView(classOf[IndesignWWWView], true)
    frontView.onWith("view.replace") {
      newView  : LocalWebHTMLVIew => 
        println(s"Replacing View as Front View")
        wwwEngine.replaceFrontView(newView)
    }
    wwwEngine.setFrontView(frontView)*/
    
    wwwEngine.addViewHandler("/", classOf[IndesignWWWView])
    wwwEngine.lInit
    wwwEngine.lStart
    // Harvest
    //---------------------
    /*
     * We Want to harvest WWWView
     */
    Harvest.addHarvester(WWWViewHarvester)
    
  }

}