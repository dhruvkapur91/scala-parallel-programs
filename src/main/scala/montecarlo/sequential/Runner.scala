package montecarlo.sequential

import java.lang.Math.random

object Runner extends App {

  val numberOfPoints = if (args.length > 0) {
    args(0).toInt
  } else {
    3 // Some default
  }

  import org.scalameter._

  def isWithinBounds(pair: (Double, Double)) = pair._1 * pair._1 + pair._2 * pair._2 < 1

  def piPredictor(numberOfPoints: Int) = {
    (1 to numberOfPoints).map(_ => (random(), random())).count(isWithinBounds) * 4.0 / numberOfPoints
  }

  def runExperiment(numberOfPoints: Int) = withWarmer(new Warmer.Default) measure {
    piPredictor(numberOfPoints)
  }

  def reporter(t: (Quantity[Double], Int)): Unit = println(s"It took ${t._1} for ${t._2} points")

  def raise10To(exponent: Int) = Math.pow(10, exponent).toInt

  (1 to numberOfPoints).map(raise10To).map(numberOfPoints => (runExperiment(numberOfPoints), numberOfPoints)).foreach(reporter)

}

/*
Sequential run reports on m4large
sbt:scala-parallel-programs> run 7
[info] running montecarlo.sequential.Runner 7
It took 0.050859 ms for 10 points
It took 0.057532 ms for 100 points
It took 0.198189 ms for 1000 points
It took 1.391727 ms for 10000 points
It took 10.373825 ms for 100000 points
It took 75.328267 ms for 1000000 points
It took 1162.63124 ms for 10000000 points
 */
