package part3testing

import akka.actor.{Actor, ActorSystem, Props}
import akka.testkit.{CallingThreadDispatcher, TestActorRef, TestProbe}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.wordspec.AnyWordSpec

import scala.concurrent.duration
import scala.concurrent.duration.Duration

class SynchronousTestingSpec extends AnyWordSpec with BeforeAndAfterAll {

  implicit val system: ActorSystem = ActorSystem("SynchronousTestingSpec")

  override def afterAll(): Unit = system.terminate()

  import SynchronousTestingSpec._

  "A counter" should {
    "Synchronously increase its counter" in {
      val counter = TestActorRef[Counter](Props[Counter])
      counter ! Inc //counter has ALREADY received the message

      assert(counter.underlyingActor.count == 1)
    }

    "synchronously increase its counter at the call of the receive function" in {
      val counter = TestActorRef[Counter](Props[Counter])
      counter.receive(Inc)
      assert(counter.underlyingActor.count == 1)

    }

    "work on the calling thread dispatcher" in {
      val counter = system.actorOf(Props[Counter].withDispatcher(CallingThreadDispatcher.Id))
      val probe = TestProbe()

      //because we are using calling threads dispatcher,
      // this current user thread is what that picks up the message from the mailbox and delivers to the counter thread.
      // in other words, the thread is busy and will not execute probe.expectMsg in the next line until the message is
      // delivered to counter and its receive has been run.. aka Counter happens on the calling thread
      probe.send(counter, Read)


      probe.expectMsg(Duration.Zero, 0) //probe has ALREADY received the message 0
    }
  }

}

object SynchronousTestingSpec {

  //testing scenarios

  case object Inc

  case object Read

  class Counter extends Actor {
    var count = 0

    override def receive: Receive = {
      case Inc =>
        count += 1
      case Read =>
        sender() ! count
    }
  }

}
