package org.odfi.indesign.core.module.jmx

import java.io.File
import org.odfi.tea.os.OSDetector
import org.odfi.tea.compile.ClassDomainSupport
import scala.sys.process._
import scala.io.Source
import javax.management.remote.JMXServiceURL
import javax.management.remote.JMXConnectorFactory
import javax.management.MBeanServerConnection
import javax.management.remote.JMXConnector

/**
 * Connection to com.sun.tools.attach.VirtualMachine
 */
class VMConnection(val obj: AnyRef) {

  var localManagementConnection: Option[JMXConnector] = None

  def kill = {

  }

  private def startManagement = {

    val m = obj.getClass.getMethod("startLocalManagementAgent")

    m.invoke(obj).toString

  }

  def getManagementServer = localManagementConnection match {

    case None =>

      val jmx = new JMXServiceURL(startManagement)
      val conn = JMXConnectorFactory.connect(jmx)
      conn.connect()
      localManagementConnection = Some(conn)
      conn.getMBeanServerConnection
      
    case Some(conn) => conn.getMBeanServerConnection

  }

  def close = {

  }

}

class ToolVirtualMachine(val cl: Class[_]) {

  def attachToVMForNetworkPort(port: Int) = {

    // Find PID
    val foundRes = """netstat -a -n -o -p tcp""".!!<
    /*Source.fromString(foundRes).getLines().find(line => line.contains(":8554")) match {
      case Some(l) =>
        println("Found line: " + l)
      case None =>
    }*/

    Source.fromString(foundRes).getLines().collectFirst {
      case line if (line.contains(":" + port)) =>
        line.trim.split("\\s").last.trim.toInt
    } match {
      case Some(pid) =>
        attach(pid)
      case None => None
    }

  }

  def attach(pid: Int) = {

    //-- Get attach method
    var attachMethod = cl.getMethod("attach", classOf[String])

    //-- Attach
    try {
      attachMethod.invoke(null, pid.toString) match {
        case null =>
          None
        case other => Some(new VMConnection(other))
      }
    } catch {
      case e: Throwable =>
        e.printStackTrace()
        None
    }

  }

}

object VMConnection extends ClassDomainSupport {

  def getOracleToolVirtualMachine = {
    //-- Get VirtualMachine
    try {

      //-- Analyse class path
      //-- Look for something that looks like tool.jar
      val libToLoad = sys.props("java.class.path").contains("lib" + File.separator + "tools.jar") match {
        case true =>
          None
        // NO tools.jar, search for JDK
        case false =>

          var exeExtension = OSDetector.isWindows() match { case true => ".exe"; case false => ""; }
          var javaHome = new File(sys.props("java.home"))

          var toolsLib = javaHome.getName.contains("jre") match {

            //-- Inside a JRE, look for ../bin/javaws
            case true if (new File(javaHome, ".." + File.separator + "bin" + File.separator + "javaw" + exeExtension).exists()) =>

              //-- Look for tools
              var jdkPath = new File(javaHome, ".." + File.separator)
              var tools = new File(jdkPath, "lib" + File.separator + "tools.jar")
              tools.exists() match {
                case true =>
                  Some(tools)
                case false =>
                  System.err.println(s"Cannot find  tools.jar in jdk: " + tools.getCanonicalPath)
                  None
              }

            //-- Inside a JRE , look for ../jdk
            case true =>

              //-- get JRE version and try to find a JDK
              var jreVersion = new File(sys.props("java.home")).getName.replace("jre", "")
              var jdkPath = new File(new File(sys.props("java.home")).getParentFile, "jdk" + jreVersion)
              jdkPath.exists() match {
                case true =>
                  Some(new File(jdkPath, List("lib", "tools.jar").mkString(File.separator)))

                //System.err.println(s"Adding")
                //agentMainCL.addURL(new File(jdkPath, List("lib", "tools.jar").mkString(File.separator)).toURI().toURL())
                case false =>
                  System.err.println(s"Cannot find JDK matching JRE: " + jreVersion)
                  None
              }

            //-- Inside JDK
            case false =>
              Some(new File(javaHome, List("lib", "tools.jar").mkString(File.separator)))
            //agentMainCL.addURL(new File(new File(sys.props("java.home")), List("lib", "tools.jar").mkString(File.separator)).toURI().toURL())
          }

          // Check Tools was found
          //----------
          toolsLib match {
            case Some(toolsFile) =>
              Some(toolsFile)

            case None =>
              sys.error("Could not find tools.jar, java home is: " + javaHome.getCanonicalPath)
          }
      }

      /*println(s"Tool path: " + toolsPath)
          sys.props("java.home").contains("jdk")*/

      libToLoad match {
        case Some(lt) =>
          withURLInClassloader(lt.toURI().toURL()) {
            new ToolVirtualMachine(Thread.currentThread().getContextClassLoader.loadClass("com.sun.tools.attach.VirtualMachine"))
          }
        case None =>
          new ToolVirtualMachine(Thread.currentThread().getContextClassLoader.loadClass("com.sun.tools.attach.VirtualMachine"))
      }

      //-- Load Class
      //var vmClass = 

      /*
          //-- Get attach method
          var attachMethod = vmClass.getMethod("attach", classOf[String])

          //-- Attach
          var vm = attachMethod.invoke(null, pid)

          //-- Call loadAgent
          var loadAgentMethod = vm.getClass.getMethod("loadAgent", classOf[String])
          loadAgentMethod.invoke(vm, agentFile.getCanonicalPath)

        

          //vm.loadAgent();*/

    } catch {
      case e: ClassNotFoundException =>
        sys.error(s"Cannot load Agent because com.sun.tools.attach.VirtualMachine was not fund, feature only supported on Oracle VM")

      case e: Throwable =>
        sys.error(s"Cannot load Agent because an error occured: " + e.getLocalizedMessage)

    }
  }

  def apply(pid: Int) = {

    val toolVM = getOracleToolVirtualMachine

    toolVM.attach(pid)

  }

}