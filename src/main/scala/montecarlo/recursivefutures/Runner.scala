package montecarlo.recursivefutures

import java.util.concurrent.ThreadLocalRandom

import montecarlo.parallelcollections.Runner.isWithinBounds
import org.scalameter.{Quantity, Warmer, withWarmer}

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

object Runner extends App {

  val numberOfPoints = if (args.length > 0) {
    args(0).toInt
  } else {
    3 // Some default
  }

  implicit val executionContext: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  def isWithinCircle(pair: (Double, Double)) = pair._1 * pair._1 + pair._2 * pair._2 < 1

  def piPredictor(numberOfPoints: Int) = {
    val rng = ThreadLocalRandom.current()
    (1 to numberOfPoints).map(_ => (rng.nextDouble(), rng.nextDouble())).count(isWithinBounds) * 4.0 / numberOfPoints
  }


  def predict(threshold: Int)(numberOfPoints: Int): Future[Double] = {
    val _predict = predict(threshold) _
    if (numberOfPoints < threshold) {
      Future(piPredictor(numberOfPoints))
    } else {
      for {
        one <- _predict(numberOfPoints / 2)
        two <- _predict(numberOfPoints / 2)
      } yield (one + two) / 2.0
    }
  }

  def runExperiment(numberOfPoints: Int) = withWarmer(new Warmer.Default) measure {
    Await.result(predict(1000)(numberOfPoints), 10.seconds)
  }

  def reporter(t: (Quantity[Double], Int)): Unit = println(s"It took ${t._1} for ${t._2} points")

  def raise10To(exponent: Int) = Math.pow(10, exponent).toInt

  (1 to numberOfPoints).map(raise10To).map(numberOfPoints => (runExperiment(numberOfPoints), numberOfPoints)).foreach(reporter)

}
