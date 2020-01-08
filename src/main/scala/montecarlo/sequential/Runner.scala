package montecarlo.sequential

import java.lang.Math.random

object Runner extends App {

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

  (1 to 3).map(raise10To).map(numberOfPoints => (runExperiment(numberOfPoints), numberOfPoints)).foreach(reporter)

}
