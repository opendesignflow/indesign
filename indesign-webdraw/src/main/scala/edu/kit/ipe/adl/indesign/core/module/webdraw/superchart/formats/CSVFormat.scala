package edu.kit.ipe.adl.indesign.core.module.webdraw.superchart.formats

import java.net.URL
import scala.io.Source
import java.net.URI
import scala.collection.mutable.ArrayBuffer

class CSVFormat(var source: URI) {

  var series = new scala.collection.mutable.ArrayBuffer[(String, scala.collection.mutable.ArrayBuffer[Double])]

  // Index of the parametric serie
  var parametricSourceIndex: Option[Int] = None
  var parametricSeries: Option[
    scala.collection.mutable.ArrayBuffer[(Double, ArrayBuffer[(String, ArrayBuffer[(Double, Double)] )]) ]] = None
  var pointsCount = 0
  def isParametric = this.parametricSourceIndex.isDefined

  def getParametricValuesImmutable = {
    this.getParametricValues.toIterable
  }
  
  def getParametricValues = {

    parametricSeries.getOrElse {

      println(s"Sorting Parametric")
      //-- Get Number of parameter values
      var parametersCount = this.series(this.parametricSourceIndex.get)._2.distinct.size

      //-- Prepare list with parameter value as 1 and (index,value) pairs as 2
      var seriesByParameter = this.series(this.parametricSourceIndex.get)._2.distinct.map {
        pv =>
          // For each parameter, create the according lists for each series in the document 
          var sortedSeries = series.drop(2).map {
            case (name,values) => (name,new scala.collection.mutable.ArrayBuffer[(Double, Double)]())
          }
          (pv, sortedSeries)
      }

      series.drop(2).zipWithIndex.foreach {
        case ((name, values),serieIndex) =>
          values.grouped(parametersCount).zipWithIndex.foreach {
            case (valuesForParameters, groupIteration) =>
              valuesForParameters.zipWithIndex.foreach {
                case (v, localIteration) =>
                  var globalIndex = (groupIteration * parametersCount) + localIteration
                  seriesByParameter(localIteration)._2.apply(serieIndex)._2 += (series(0)._2.apply(globalIndex) -> v)
              }
          }
      }
      
      /*var first = seriesByParameter.apply(0)
      println(s"For: ${first._1}")
      first._2.foreach {
        case (i,v)=>
          println(s"$i -> $v");
      }
      */

      /*this.series(this.parametricSourceIndex.get)._2.grouped(parametersCount).foreach {
        parametersGroup => 
          parametersGroup.foreach { 
            parameterValue => 
            
          }
      }
      this.series(this.parametricSourceIndex.get)._2.distinct.zipWithIndex.map {
        case (parameterValue, iteration) =>
          
          var pointIndexValue = series(0)._2.apply(iteration)
          
          //-- For each remaining series:
          //--  Take values parametersCount times and add each value to matching series in rebuild list
          series.drop(2).foreach {
            case (name, values) =>
              values.grouped(parametersCount).foreach {
                gv =>
                  gv.zipWithIndex.foreach {
                    case (v, i) => seriesByParameter(i)._2 += (pointIndexValue -> v)
                  }
              }
          }

      }*/
      parametricSeries = Some(seriesByParameter)
      seriesByParameter
    }

  }

  def parse = {

    //-- Get Content 
    var src = Source.fromFile(source)
    var reader = src.bufferedReader()
    //var lines = src.getLines

    //println(s"Number of lines: "+lines.size)

    //-- Get Header
    var headers = reader.readLine().split(",")
    headers.foreach {
      h =>
        // println(s"Found header: " + h)
        series += (h.trim -> new scala.collection.mutable.ArrayBuffer[Double]())
    }

    var line = reader.readLine
    while (line != null) {

      line.split(",").zipWithIndex.foreach {
        case (vString, i) =>
          series.apply(i)._2 += vString.trim.toDouble * 100.0 / 100.0
          
        
      }

      line = reader.readLine()
    }

    //-- Parse Rest
    /*reader.re
    lines.drop(1).foreach {
      line => 
        println(s"Found Line: "+line)
        line.split(",").zipWithIndex.foreach {
          case (vString,i) => 
            series.apply(i)._2.append(vString.toDouble)
        }
    }*/

    //-- Analyse
    //----------------------
    println(s"Done Parsing")

    // Parametric
    //-------------------

    //-- Get Index Series
    println(s"Index column is: " + series(0)._1)
    var groupByIndex = series(0)._2.groupBy { index => index }

    // For every index, the number of times it is repeated
    var indexCounts = groupByIndex.values.map { v => v.size }
    var maxAndMin = (indexCounts.max, indexCounts.min)
    maxAndMin match {
      //-- Indexes are repeated, and more than one -> parametric
      case (max, min) if (max > 1 && min == max) =>
        println(s"Parametric")
        parametricSourceIndex = Some(1)
        pointsCount = max

        // Checks
        if (series.size < 3) {
          sys.error("Parametric File requires at least 3 columns: Index,Parameter values,Measurement Values")
        }
      case (max, min) =>
        println(s"Normal")
        pointsCount = series(0)._2.size
    }
  }

}