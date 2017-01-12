package appDep.test

import org.odfi.indesign.core.module._

object AppDepModule extends IndesignModule  {
  
  this.onInit {
    println("Loading Module AppDep: "+this)
  }
}