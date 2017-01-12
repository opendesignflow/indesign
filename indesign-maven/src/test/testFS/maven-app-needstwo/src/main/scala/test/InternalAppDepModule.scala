package appInternalDep.test

import org.odfi.indesign.core.module._

object InternalAppDepModule extends IndesignModule {
  
  this.onInit {
    println("Loading Module InternalAppDep: "+hashCode)
    println("AppDepModule: "+appDep.test.AppDepModule) 
  }
  
}