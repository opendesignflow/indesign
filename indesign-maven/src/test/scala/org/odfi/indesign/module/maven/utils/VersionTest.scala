package org.odfi.indesign.module.maven.utils

import org.scalatest.FunSuite

class VersionTest extends FunSuite {

  test("Simple Version Parse Test") {

    val baseVersion = "3.0.5.2015-SNAPSHOT"

    var v = Version(baseVersion)

    //-- Test components
    assertResult(3)(v.getMajor)
    assertResult(0)(v.getMinor)
    assertResult(5)(v.getPatch)
    assertResult(2015)(v.getRelease)
    assertResult("-SNAPSHOT")(v.getTailComponent)

  }

  test("Simple Version Compare Test") {

    var v1 = Version("3.0.5")
    var v2 = Version("3.0.5.2015")

    //-- Test components
    assertResult(true)(v2 > v1)

    //-- Next v
    v1 = Version("3.0.0.1473155614587")
    v2 = Version("3.0.0.-SNAPSHOT")

    assertResult(3)(v1.getMajor)
    assertResult(0)(v1.getMinor)
    assertResult(0)(v1.getPatch)
    assertResult(1473155614587L)(v1.getRelease)

    assertResult(3)(v2.getMajor)
    assertResult(0)(v2.getMinor)
    assertResult(0)(v2.getPatch)

    assertResult(true)(v1 > v2)

  }

}