package edu.kit.ipe.adl.indesign.core.harvest

import org.scalatest.FunSuite
import org.scalatest.GivenWhenThen
import org.scalatest.BeforeAndAfter

class HarvesterA extends Harvester[HarvestedResource,HarvestedResource] {
  
  def doHarvest = {
    
  }
}

class HarvesterB extends Harvester[HarvestedResource,HarvestedResource] {
  
  def doHarvest = {
    
  }
}

class HarvestTest extends FunSuite with GivenWhenThen with BeforeAndAfter {
  
  
  before {
    Harvest.resetAutoHarvester
  }
  
  test("Post AutoRegister Class") {
    
    
    Given("An Harvester")
    var a = new HarvesterA
    Harvest.addHarvester(a)
    
    Then("Register another one as autoclass child")
    Harvest.registerAutoHarvesterClass(classOf[HarvesterA],classOf[HarvesterB])
    Harvest.updateAutoHarvester
    
    assertResult(1,"Harvester A has a created Harvester B as child")(a.childHarvesters.size)
    assertResult(true,"Harvester A has a created Harvester B as child")(a.childHarvesters.head.isInstanceOf[HarvesterB])
    
    
  }
  
  test("Pre AutoRegister Class") {
    
    Given("An Autoregistered class")
    Harvest.registerAutoHarvesterClass(classOf[HarvesterA],classOf[HarvesterB])
    
    Then("Create an Harvester")
    var a = new HarvesterA
    Harvest.addHarvester(a)

    
    assertResult(1,"Harvester A has a created Harvester B as child")(a.childHarvesters.size)
    assertResult(true,"Harvester A has a created Harvester B as child")(a.childHarvesters.head.isInstanceOf[HarvesterB])
    
    
  }
  
  test("Post AutoRegister Object") {
    
    
    Given("An Harvester")
    var a = new HarvesterA
    Harvest.addHarvester(a)
    
    Then("Register another one as autoclass object")
    var b = new HarvesterB
    Harvest.registerAutoHarvesterObject(classOf[HarvesterA],b)
    Harvest.updateAutoHarvester
    
    assertResult(1,"Harvester A has a created Harvester B as child")(a.childHarvesters.size)
    assertResult(true,"Harvester A has a created Harvester B as child")(a.childHarvesters.head == b)
    
    
  }
  
  test("Pre AutoRegister Object") {
    
    Given("Register an autoclass object")
    var b = new HarvesterB
    Harvest.registerAutoHarvesterObject(classOf[HarvesterA],b)
    
    Then("Create An Harvester")
    var a = new HarvesterA
    Harvest.addHarvester(a)

    
    assertResult(1,"Harvester A has a created Harvester B as child")(a.childHarvesters.size)
    assertResult(true,"Harvester A has a created Harvester B as child")(a.childHarvesters.head == b)
    
    
  }
}