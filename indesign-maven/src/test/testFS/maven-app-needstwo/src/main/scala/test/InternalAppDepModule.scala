package appInternalDep.test

import edu.kit.ipe.adl.indesign.core.module._

object InternalAppDepModule extends IndesignModule {
  
  this.onInit {
    println("Loading Module InternalAppDep: "+hashCode)
    println("AppDepModule: "+appDep.test.AppDepModule) 
  }
  
}