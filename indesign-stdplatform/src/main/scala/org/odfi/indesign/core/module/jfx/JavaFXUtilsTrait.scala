package org.odfi.indesign.core.module.jfx

import javafx.application.Platform
import java.util.concurrent.Semaphore
import javafx.scene.control.Button
import javafx.scene.control.CheckBox
import javafx.beans.value.ObservableValue
import javafx.beans.value.ChangeListener
import javafx.event.EventHandler
import javafx.scene.input.MouseEvent
import javafx.beans.property.ReadOnlyObjectProperty
import java.net.URL
import javafx.scene.control.Label
import javafx.scene.image.ImageView
import javafx.scene.image.Image
import javafx.scene.Node
import javafx.beans.property.ObjectProperty
import javafx.scene.control.Control
import javafx.scene.layout.Pane
import javafx.beans.property.ReadOnlyBooleanProperty
import javafx.event.Event
import javafx.beans.property.ReadOnlyDoubleProperty
import javafx.scene.input.KeyEvent
import javafx.scene.control.MenuItem
import javafx.event.ActionEvent
import javafx.stage.FileChooser.ExtensionFilter
import javafx.scene.control.TableColumnBase
import javafx.scene.control.TableColumn
import javafx.util.Callback
import javafx.scene.control.TableColumn.CellDataFeatures
import scala.language.implicitConversions

trait JavaFXUtilsTrait {

    // Utils
    //------------
    def onJFXThread(cl: => Any): Unit = {

        //JavaFXRun.onJavaFX({ cl })
        Platform.isFxApplicationThread() match {
            case true =>
                cl
            case false =>
                Platform.runLater(new Runnable() {
                    def run : Unit = {
                        cl

                    }
                })
        }

    }
    def onJFXThreadLater(cl: => Any): Unit = {

        //JavaFXRun.onJavaFX({ cl })

        Platform.runLater(new Runnable() {
            def run : Unit  = {
                cl

            }
        })

    }

    def onJFXThreadBlocking(cl: => Any): Any = {

        //JavaFXRun.onJavaFX({ cl })
        Platform.isFxApplicationThread() match {
            case true =>
                cl
            case false =>
                var res: Any = null
                var err: Throwable = null
                var s = new Semaphore(0, true)
                Platform.runLater(new Runnable() {
                    def run = {
                        try {
                            res = cl
                        } catch {
                            case e: Throwable => err = e
                        } finally {
                            s.release()
                        }

                    }
                })
                s.acquire

                //-- Return result or propagate error
                (res, err) match {
                    case (r, null) => r
                    case _ =>
                        err.printStackTrace()
                        throw err
                }
        }

    }

    // Listeners
    //-----------------

    def onJFXEvent[ET <: Event](cl: ET => Unit): EventHandler[ET] = {

        new EventHandler[ET] {
            def handle(event: ET) = {
                cl(event)
            }
        }

    }

    def onJFXSelected(node: CheckBox)(cl: (Boolean => Unit)) = {
        node.selectedProperty().addListener(new ChangeListener[java.lang.Boolean] {
            def changed(b: ObservableValue[_ <: java.lang.Boolean], old: java.lang.Boolean, n: java.lang.Boolean) = {
                cl(n)

            }
        })
    }

    def onJFXClick(node: Button)(cl: => Unit) = {

        node.setOnMouseClicked(new EventHandler[MouseEvent] {
            def handle(event: MouseEvent) = {
                cl
            }
        })

    }

    def onJFXClickThread(node: Button, runText: Option[String] = None)(cl: => Unit) = {

        node.setOnMouseClicked(new EventHandler[MouseEvent] {
            def handle(event: MouseEvent) = {

                val savedText = node.getText
                if (runText.isDefined) {
                    node.setText(runText.get)
                }
                node.setDisable(true)

                val th = new Thread {
                    override def run = {

                        try {
                            cl
                        } finally {
                            onJFXThreadBlocking {

                                node.setText(savedText)
                                node.setDisable(false)
                            }
                        }

                    }
                }

                th.start

            }
        })

    }

    def onJFXKeyTyped(h: ObjectProperty[EventHandler[KeyEvent]])(cl: (KeyEvent => Unit)) = {

        h.setValue(new EventHandler[KeyEvent] {
            def handle(event: KeyEvent) = {
                cl(event)
            }
        })

    }

    def onJFXMouseEvent(h: ObjectProperty[EventHandler[MouseEvent]])(cl: (MouseEvent => Unit)) = {

        h.setValue(new EventHandler[MouseEvent] {
            def handle(event: MouseEvent) = {
                cl(event)
            }
        })

    }

    def onJFXMouseClicked(n: Control)(cl: (MouseEvent => Unit)) = {
        n.setOnMouseClicked(new EventHandler[MouseEvent] {
            def handle(event: MouseEvent) = {
                cl(event)
            }
        })

    }

    def onJFXMenuAction(m: MenuItem)(cl: => Unit) = {
        m.setOnAction(new EventHandler[ActionEvent] {

            def handle(e: ActionEvent) = {
                cl
            }
        });
    }

    def onMouseClicked(n: Pane)(cl: (MouseEvent => Unit)) = {
        n.setOnMouseClicked(new EventHandler[MouseEvent] {
            def handle(event: MouseEvent) = {
                cl(event)
            }
        })

    }

    def onJFXIntPropertyChange(prop: ReadOnlyObjectProperty[Integer])(cl: Int => Unit) = {
        prop.addListener(new ChangeListener[java.lang.Integer] {
            def changed(b: ObservableValue[_ <: java.lang.Integer], old: java.lang.Integer, n: java.lang.Integer) = {
                cl(n)

            }
        })

    }

    def onJFXLongPropertyChange(prop: ReadOnlyObjectProperty[Long])(cl: Long => Unit) = {
        prop.addListener(new ChangeListener[Long] {
            def changed(b: ObservableValue[_ <: Long], old: Long, n: Long) = {
                cl(n)

            }
        })

    }

    def onJFXBooleanPropertyChange(prop: ReadOnlyBooleanProperty)(cl: Boolean => Unit) = {
        prop.addListener(new ChangeListener[java.lang.Boolean] {
            def changed(b: ObservableValue[_ <: java.lang.Boolean], old: java.lang.Boolean, n: java.lang.Boolean) = {
                cl(n)

            }
        })

    }

    def onJFXDoublePropertyChange(prop: ReadOnlyDoubleProperty)(cl: Double => Unit) = {
        prop.addListener(new ChangeListener[Number] {
            def changed(b: ObservableValue[_ <: Number], old: Number, n: Number) = {
                cl(n.doubleValue())

            }
        })

    }

    def onJFXReadonlyDoubleChange(prop: ReadOnlyDoubleProperty)(cl: Number => Unit) = {
        prop.addListener(new ChangeListener[Number] {
            def changed(b: ObservableValue[_ <: Number], old: Number, n: Number) = {
                cl(n)

            }
        })

    }

    def onJFXObjectPropertyChanged[OP](prop: ObjectProperty[OP])(cl: OP => Unit) = {
        prop.addListener(new ChangeListener[OP] {
            def changed(b: ObservableValue[_ <: OP], old: OP, n: OP) = {
                cl(n)

            }
        })

    }
    
    def onJFXReadOnlyObjectPropertyChanged[OP](prop: ReadOnlyObjectProperty[OP])(cl: OP => Unit) = {
        prop.addListener(new ChangeListener[OP] {
            def changed(b: ObservableValue[_ <: OP], old: OP, n: OP) = {
                cl(n)

            }
        })

    }

    implicit def convertClosureToEventHandler[ET <: Event](cl: ET => Unit) : EventHandler[ET] = {
        var eh = new EventHandler[ET] {

            def handle(event: ET) = {
                cl(event)
            }
        }
        eh
    }

    def jfxImageLabel(u: URL, text: String = ""): Label = {
        new Label(text, new ImageView(new Image(u.toExternalForm())))
    }

    def jfxImageLabel(resource: String): Label = {
        jfxImageLabel(getClass.getClassLoader.getResource(resource), "")
    }

    //  Table
    //------------

    def jfxColumnSimpleCellValue[M, V](c: TableColumn[M, V])(cl: M => ObservableValue[V]) = {
        c.setCellValueFactory(new Callback[CellDataFeatures[M, V], ObservableValue[V]]() {

            def call(p: CellDataFeatures[M, V]): ObservableValue[V] = {
                cl(p.getValue)
            }

        })
    }

}