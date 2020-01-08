package montecarlo.fixedfutures

import java.lang.Math.random
import java.util.concurrent.ThreadLocalRandom

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
    def compute(n: Int) = {
      val rng = ThreadLocalRandom.current()
      (1 to n).map(_ => (rng.nextDouble(), rng.nextDouble())).count(isWithinBounds)
    }
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

/*
Future implementation running times on m4large

1 thread - on average, slower than sequential...
[info] running montecarlo.fixedfutures.Runner 7 1
It took 0.245685 ms for 10 points
It took 0.260486 ms for 100 points
It took 0.366152 ms for 1000 points
It took 0.799212 ms for 10000 points
It took 6.76789 ms for 100000 points
It took 94.150547 ms for 1000000 points
It took 1090.711087 ms for 10000000 points

2 threads - wayyy slower than sequential... I don't know, it makes no sense...
[info] running montecarlo.fixedfutures.Runner 7 2
It took 0.226309 ms for 10 points
It took 0.192723 ms for 100 points
It took 0.241403 ms for 1000 points
It took 2.342587 ms for 10000 points
It took 22.278208 ms for 100000 points
It took 229.475656 ms for 1000000 points
It took 2400.402471 ms for 10000000 points

10 threads - whatt.... this is weird...
[info] running montecarlo.fixedfutures.Runner 7 10
It took 0.193699 ms for 10 points
It took 0.31988 ms for 100 points
It took 0.62585 ms for 1000 points
It took 3.040552 ms for 10000 points
It took 24.241802 ms for 100000 points
It took 310.822098 ms for 1000000 points / runMain 38s
It took 3088.061321 ms for 10000000 points
 */