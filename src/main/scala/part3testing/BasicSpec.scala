package part3testing

import akka.actor.{Actor, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.wordspec.AnyWordSpecLike
import part3testing.BasicSpec.{BlackHoleActor, LabTestActor, SimpleActor}

import scala.collection.immutable
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.Random


class BasicSpec extends TestKit(ActorSystem("BasicSpec"))
  with ImplicitSender // this lets testActor to be pass implicitly to every single tell
  with AnyWordSpecLike
  with BeforeAndAfterAll {

  //setup
  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "A simple actor should" should {
    "send back the same message" in {
      val echoActor = system.actorOf(Props[SimpleActor])
      val message = "hello, test"
      echoActor ! message //testActor

      expectMsg(message) //here testActor receives the messages and applies the assert
    }
  }

  "A blackhole actor" should {
    "not send any message" in {
      val echoActor = system.actorOf(Props[BlackHoleActor])
      val message = "hello, test"
      echoActor ! message //testActor

      expectNoMessage(1 second) //here testActor receives the messages and applies the assert
    }
  }

  "a lab test actor" should {

    val labTestActor = system.actorOf(Props[LabTestActor])

    "turn a string into uppercase" in {
      labTestActor ! "I Love Akka"

      val reply = expectMsgType[String]

      assert(reply == "I LOVE AKKA")
    }

    "reply to a greeting" in {
      labTestActor ! "greeting"
      val reply = expectMsgType[String]

      assert(reply == "hi" || reply == "hello")
    }

    "reply with favorite tech in" in {
      labTestActor ! "favoriteTech"
      expectMsgAllOf("Scala", "Akka")
    }

    "reply with cool tech in a different way" in {
      labTestActor ! "favoriteTech"
      val messages: immutable.Seq[AnyRef] = receiveN(2) // Seq[Any
    }

    "reply with cool tech in a fancy way" in {
      labTestActor ! "favoriteTech"

      expectMsgPF(){
        case "Scala" => //only care that the PF is defined
        case "Akka" =>
      }
    }
  }

}

object BasicSpec {

  class SimpleActor extends Actor {
    override def receive: Receive = {
      case message => sender() ! message
    }
  }

  class BlackHoleActor extends Actor {
    override def receive: Receive = {
      case message => Actor.emptyBehavior
    }
  }

  class LabTestActor extends Actor {
    val random = new Random()

    override def receive: Receive = {
      case "greeting" => if (random.nextBoolean()) sender() ! "hi" else sender() ! "hello"
      case "favoriteTech" =>
        sender() ! "Scala"
        sender() ! "Akka"
      case message => sender() ! message.toString.toUpperCase
    }
  }

}