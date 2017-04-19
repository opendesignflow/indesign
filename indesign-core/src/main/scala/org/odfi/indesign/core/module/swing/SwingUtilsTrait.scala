package org.odfi.indesign.core.module.swing

import java.awt.Menu
import java.awt.MenuItem
import java.awt.event.ActionListener
import java.awt.event.ActionEvent
import javax.swing.MenuElement
import javax.swing.JMenuItem
import javax.swing.JMenu
import javax.swing.JPopupMenu
import javax.swing.JComponent
import javax.swing.JButton
import javax.swing.SwingUtilities
import javax.swing.JFrame
import java.awt.GraphicsEnvironment

trait SwingUtilsTrait {

  def onSwingThreadAndWait(cl: => Unit) = {
    SwingUtilities.invokeAndWait(new Runnable {
      def run = {
        cl
      }
    })
  }

  def onSwingThreadLater(cl: => Unit) = {
    SwingUtilities.invokeLater(new Runnable {
      def run = {
        cl
      }
    })
  }

  def onSwingClick(c: JButton)(cl: => Unit) = {

    c.addActionListener(new ActionListener {
      def actionPerformed(ev: ActionEvent) = {
        cl

      }
    })

  }

  def addActionMenu(start: MenuElement)(path: String)(cl: => Unit) = {

    createPathMenu(start)(path) match {
      case Some(menuItem) =>
        menuItem.addActionListener(new ActionListener {
          def actionPerformed(ev: ActionEvent) = {
            cl

          }
        })
      case None =>
        sys.error("Cannot find/create menu for path: " + path)
    }

  }

  def createPathMenu(start: MenuElement)(path: String) = {

    // Split path and take existing menus or create new one
    var currentMenu = start
    var resultItem: Option[JMenuItem] = None
    var splittedPaths = path.split("->").filterNot { _.length() == 0 }.map(_.trim)
    splittedPaths.zipWithIndex.foreach {
      case (menuText, i) =>

        currentMenu.getSubElements.collect { case i: JMenuItem => i }.find(_.getText == menuText) match {

          //-- Found existing
          case Some(foundMenu: JMenu) =>

            currentMenu = foundMenu
            resultItem = Some(foundMenu)

          //-- Create new one as Menu or Menu Item
          case None if (i == splittedPaths.size - 1) =>

            var item = new JMenuItem(menuText)

            currentMenu match {
              case m: JPopupMenu => m.add(item)
              case other: JMenuItem => other.add(item)
            }
            resultItem = Some(item)

          case None =>

            var item = new JMenu(menuText)
            currentMenu match {
              case m: JPopupMenu => m.add(item)
              case other: JMenuItem => other.add(item)
            }
            currentMenu = item

          // Don' keep going and stop
          case Some(_) => throw new RuntimeException("stop")
        }

    }

    resultItem

  }

  // Frame util
  //----------------
  def centerOnScreen(f: JFrame) = {

    var point = GraphicsEnvironment.getLocalGraphicsEnvironment.getCenterPoint
    
    f.setLocation((point.getX-f.getWidth/2).toInt,(point.getY-f.getHeight/2).toInt)
  }

}