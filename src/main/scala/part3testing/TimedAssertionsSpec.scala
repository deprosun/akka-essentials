package part3testing

import java.util.Random

import akka.actor.{Actor, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import com.typesafe.config.ConfigFactory
import org.scalatest.BeforeAndAfterAll
import org.scalatest.wordspec.AnyWordSpecLike

import scala.concurrent.duration._
import scala.language.postfixOps

class TimedAssertionsSpec extends TestKit(
  ActorSystem("TimedAssertionsSpec", ConfigFactory.load().getConfig("specialTimeAssertionsConfig")))
  with ImplicitSender
  with AnyWordSpecLike
  with BeforeAndAfterAll {

  override def afterAll(): Unit = TestKit.shutdownActorSystem(system)

  import TimedAssertionsSpec._

  "A worker actor" should {
    val workerActor = system.actorOf(Props[WorkerActor])

    "reply the meaning of life in a timely manner" in {
      within(500 millis, 1 second) {
        workerActor ! "work"

        expectMsg(WorkResult(42))
      }
    }

    "reply with valid work at a reasonable cadence" in {
      within(1 second) {
        workerActor ! "workSequence"

        //receiveWhile will only get messages that follow this time cadence and should contain no more thant 10
        val results: Seq[Int] = receiveWhile[Int](2 seconds, 500 millis, 10) {
          case WorkResult(result) => result
        }

        assert(results.sum > 5)

      }
    }

    "reply to a test probo in a timely manner" in {
      within(1 second) {

        val probe = TestProbe()
        probe.send(workerActor, "work") //longs the computation
        probe.expectMsg(WorkResult(42)) //timeout of 0.3 seconds from application.conf
      }
    }
  }

}

object TimedAssertionsSpec {

  //testing scenario

  case class WorkResult(result: Int)

  class WorkerActor extends Actor {
    override def receive: Receive = {
      case "work" =>
        //long computation
        Thread.sleep(500)
        sender() ! WorkResult(42)

      case "workSequence" =>
        val r = new Random()

        for (_ <- 1 to 10) {
          Thread.sleep(r.nextInt(50))
          sender() ! WorkResult(1)
        }
    }
  }

}
