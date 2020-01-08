package montecarlo.fixedfutures

import java.lang.Math.random

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

object Runner extends App {

  implicit val executionContext: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  val numberOfPoints = if (args.length > 0) {
    args(0).toInt
  } else {
    3 // Some default
  }

  val numberOfThreads = if (args.length > 1) {
    args(1).toInt
  } else {
    2 // 2 threads as default
  }

  import org.scalameter._

  def isWithinBounds(pair: (Double, Double)) = pair._1 * pair._1 + pair._2 * pair._2 < 1

  def piPredictor(numberOfPoints: Int) = {
    def compute(n: Int) = (1 to n).map(_ => (random(), random())).count(isWithinBounds)
    val partsF: Seq[Future[Int]] = (1 to numberOfThreads).map(_ => Future(compute(numberOfPoints / numberOfThreads)))
    partsF.map(x => Await.result(x, Duration.Inf)).sum * 4.0 / numberOfPoints
  }

  def runExperiment(numberOfPoints: Int) = withWarmer(new Warmer.Default) measure {
    piPredictor(numberOfPoints)
  }

  def reporter(t: (Quantity[Double], Int)): Unit = println(s"It took ${t._1} for ${t._2} points")

  def raise10To(exponent: Int) = Math.pow(10, exponent).toInt

  (1 to numberOfPoints).map(raise10To).map(numberOfPoints => (runExperiment(numberOfPoints), numberOfPoints)).foreach(reporter)

}
