package part3testing

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.wordspec.AnyWordSpecLike

class TestProbeSpec extends TestKit(ActorSystem("TestProbeSpec"))
  with ImplicitSender
  with AnyWordSpecLike
  with BeforeAndAfterAll {
  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  import TestProbeSpec._

  "A master actor" should {
    "register a slave" in {
      val master = system.actorOf(Props[Master])
      val slave = TestProbe("slave")
      master ! Register(slave.ref)
      expectMsg(RegsitrationAck)
    }

    "send the work to the slave actor" in {
      val master = system.actorOf(Props[Master])
      val slave = TestProbe("slave")
      master ! Register(slave.ref)
      expectMsg(RegsitrationAck)


      master ! Work("I love Akka")

      //interaction between master actor and the slave actor
      slave.expectMsgPF() {
        case SlaveWork("I love Akka", m) => assert(m == testActor)
        case _ => fail()
      }

      slave.reply(WorkCompleted(3, testActor))

      expectMsg(Report(3))
    }

    "aggregate data correctly" in {
      val master = system.actorOf(Props[Master])
      val slave = TestProbe("slave")
      master ! Register(slave.ref)
      expectMsg(RegsitrationAck)

      val workLoadString = "I love Akka"
      master ! Work(workLoadString)
      master ! Work(workLoadString)

      slave.receiveWhile() {
        case SlaveWork(`workLoadString`, `testActor`) => slave.reply(WorkCompleted(3, testActor))
      }

      expectMsg(Report(3))
      expectMsg(Report(6))

    }
  }
}

object TestProbeSpec {

  /**
    * scenario
    *
    * word counting actor hierarchy master - slave
    *
    * send some work to the master
    *   - master sends the slave a piece of work
    *   - slave processes the work and replies to the master
    *   - master aggregates the result
    * master sends the total count to the original requester
    */

  case object RegsitrationAck

  case class Register(slaveRef: ActorRef)

  case class Work(text: String)

  case class SlaveWork(text: String, requester: ActorRef)

  case class WorkCompleted(count: Int, originalRequested: ActorRef)

  case class Report(totalCount: Int)

  class Master extends Actor {
    override def receive: Receive = {
      case Register(slaveRef) =>
        sender() ! RegsitrationAck
        context.become(online(slaveRef, 0))
      case _ => //ignore
    }

    def online(slaveRef: ActorRef, totalWordCount: Int): Receive = {
      case Work(text) => slaveRef ! SlaveWork(text, sender())
      case WorkCompleted(count, originalRequester) =>
        val newTotalCount = totalWordCount + count
        originalRequester ! Report(newTotalCount)
        context.become(online(slaveRef, newTotalCount))
    }
  }

}