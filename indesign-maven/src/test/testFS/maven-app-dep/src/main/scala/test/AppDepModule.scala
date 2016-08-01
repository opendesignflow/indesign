package appDep.test

import edu.kit.ipe.adl.indesign.core.module._

object AppDepModule extends IndesignModule  {
  
  this.onInit {
    println("Loading Module AppDep: "+this)
  }
}