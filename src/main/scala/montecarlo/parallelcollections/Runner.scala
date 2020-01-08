package montecarlo.parallelcollections

import java.lang.Math.random
import scala.collection.parallel.CollectionConverters._

object Runner extends App {

  val numberOfPoints = if (args.length > 0) {
    args(0).toInt
  } else {
    3 // Some default
  }

  import org.scalameter._

  def isWithinBounds(pair: (Double, Double)) = pair._1 * pair._1 + pair._2 * pair._2 < 1

  def piPredictor(numberOfPoints: Int) = {
    (1 to numberOfPoints).par.map(_ => (random(), random())).count(isWithinBounds) * 4.0 / numberOfPoints
  }

  def runExperiment(numberOfPoints: Int) = withWarmer(new Warmer.Default) measure {
    piPredictor(numberOfPoints)
  }

  def reporter(t: (Quantity[Double], Int)): Unit = println(s"It took ${t._1} for ${t._2} points")

  def raise10To(exponent: Int) = Math.pow(10, exponent).toInt

  (1 to numberOfPoints).map(raise10To).map(numberOfPoints => (runExperiment(numberOfPoints), numberOfPoints)).foreach(reporter)

}

/*
Parallel collections report - Very slow compared to sequential collecctions
info] running montecarlo.parallelcollections.Runner 7
It took 1.677748 ms for 10 points
It took 1.061964 ms for 100 points
It took 0.562431 ms for 1000 points
It took 3.582251 ms for 10000 points
It took 30.981812 ms for 100000 points
It took 332.464702 ms for 1000000 points / runMain 43s
It took 3252.623377 ms for 10000000 points
 */
