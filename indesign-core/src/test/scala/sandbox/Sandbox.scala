package sandbox

import java.nio.file.Files
import java.io.File
import java.nio.file.Path
import scala.compat.java8.FunctionConverters._

object Sandbox extends App {
  
  var st = Files.walk(new File("").toPath())
  st.forEach(println) 
  
}