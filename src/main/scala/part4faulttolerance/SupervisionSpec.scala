package part4faulttolerance

import akka.actor.SupervisorStrategy.{Escalate, Restart, Resume, Stop}
import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, AllForOneStrategy, OneForOneStrategy, Props, SupervisorStrategy, Terminated}
import akka.testkit.{EventFilter, ImplicitSender, TestKit}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.wordspec.AnyWordSpecLike

class SupervisionSpec extends TestKit(ActorSystem("SupervisionSpec"))
  with ImplicitSender
  with AnyWordSpecLike
  with BeforeAndAfterAll {

  override def afterAll(): Unit = TestKit.shutdownActorSystem(system)

  import SupervisionSpec._

  //IMPORTANT: when a child fails, parent decides what to do with that child
  //it can:
  //- STOP (state is lost)
  //- RESTART (default) (state is lost and then swapped with a fresh actor instance)
  //- RESUME (continues and ignores the exception, not hurting the state)
  //- ESCALATE (sends the failure to the parent to throw)

  "A supervisor" should {

    //IMPORTANT: when a child fails, parent decides what to do with that child
    //it can:
    //- STOP (state is lost)
    //- RESTART (default) (state is lost and then swapped with a fresh actor instance)
    //- RESUME (continues and ignores the exception, not hurting the state)
    //- ESCALATE (sends the failure to the parent to throw)

    "resume a child incase of a minor fault" in {
      val supervisor = system.actorOf(Props[Supervisor])
      supervisor ! Props[FussyWordCounter]

      val child = expectMsgType[ActorRef]

      child ! "I love akka"
      child ! Report
      expectMsg(3)

      //this will cause an runtime exception
      //which will then cause the supervisor (parent) to apply the supervision strategy
      //the strategy of this error is to simply Resume the child.
      child ! "Akka is awesome because I am learning in a whole new way"

      //when Resume, the state stays intacts
      child ! "I love akka"
      child ! Report
      expectMsg(6)


    }

    //IMPORTANT: when a child fails, parent decides what to do with that child
    //it can:
    //- STOP (state is lost)
    //- RESTART (default) (state is lost and then swapped with a fresh actor instance)
    //- RESUME (continues and ignores the exception, not hurting the state)
    //- ESCALATE (sends the failure to the parent to throw)
    "restart its child in case of an empty sentence" in {
      val supervisor = system.actorOf(Props[Supervisor], "supervisorA")
      supervisor ! Props[FussyWordCounter]

      val child = expectMsgType[ActorRef]

      child ! "I love akka"
      child ! Report
      expectMsg(3)

      //this will cause a null pointer exception
      //which will then cause the supervisor (parent) to apply the supervision strategy
      //the strategy of this error is to simply Restart the child.
      child ! ""

      //when Restart, the state is lost and all of children underneath simply stop.
      child ! Report
      expectMsg(0)

    }

    //IMPORTANT: when a child fails, parent decides what to do with that child
    //it can:
    //- STOP (state is lost)
    //- RESTART (default) (state is lost and then swapped with a fresh actor instance)
    //- RESUME (continues and ignores the exception, not hurting the state)
    //- ESCALATE (sends the failure to the parent to throw)
    "stop its child in case sentence does not start with an uppercase" in {
      val supervisor = system.actorOf(Props[Supervisor], "supervisorB")
      supervisor ! Props[FussyWordCounter]

      val child = expectMsgType[ActorRef]

      watch(child)

      child ! "I love akka"
      child ! Report
      expectMsg(3)

      //this will cause a illegal argument exception
      //which will then cause the supervisor (parent) to apply the supervision strategy
      //the strategy of this error is to simply Stop the child.
      child ! "i love akka"

      //when Stopped and you cannot talk to it anymore.
      val terminatedMessage = expectMsgType[Terminated]
      assert(terminatedMessage.actor == child)

    }

    //IMPORTANT: when a child fails, parent decides what to do with that child
    //it can:
    //- STOP (state is lost)
    //- RESTART (default) (state is lost and then swapped with a fresh actor instance)
    //- RESUME (continues and ignores the exception, not hurting the state)
    //- ESCALATE (sends the failure to the parent to throw)
    "escalate an error when it doesn't know what to do" in {
      val supervisor = system.actorOf(Props[Supervisor], "supervisorC")
      supervisor ! Props[FussyWordCounter]

      val child = expectMsgType[ActorRef]

      watch(child)

      //this will cause a unknown exception
      //which will then cause the supervisor (parent) to apply the supervision strategy
      //the strategy of this error is to simply Escalate the error to the parent.
      //this means that the exception is actually throw by the supervisor (parent).
      //Noting the axiom above, since the error is being throw by the supervisor,
      //and the supervisor parent's is User Guardian actor.  The default strategy for User Guardian
      //is to simply RESTART the child (supervisor).  RESTARTING the supervisor will STOP children
      //of supervisor (FussyWordCounter).  Therefor, when we watch the FussyWordCounter (child of supervisor)
      //it is already terminated and the message received by the TestActor is Terminated(ref) where ref is FussyWordCounter
      child ! 43
      val terminatedMessage = expectMsgType[Terminated]
      assert(terminatedMessage.actor == child)

    }
  }

  "A kinder supervisor" should {

    //IMPORTANT: when a child fails, parent decides what to do with that child
    //it can:
    //- STOP (state is lost)
    //- RESTART (default) (state is lost and then swapped with a fresh actor instance): preRestart method will, by
    //  default, kill (STOP) all the children.  If any children are not killed, they are simply RESTART(ed)
    //- RESUME (continues and ignores the exception, not hurting the state)
    //- ESCALATE (sends the failure to the parent to throw)
    "not kill children in case it's restarted or escalates failures" in {
      val supervisor = system.actorOf(Props[NoDeathOnRestartSupervisor], "kindSupervisor")
      supervisor ! Props[FussyWordCounter]

      val child = expectMsgType[ActorRef]

      watch(child)

      child ! "I love akka"
      child ! Report
      expectMsg(3)

      //this will cause a unknown exception
      //which will then cause the supervisor (parent) to apply the supervision strategy
      //the strategy of this error is to simply Escalate the error to the parent.
      //this means that the exception is actually throw by the supervisor (parent).
      //Noting the axiom above, since the error is being throw by the supervisor,
      //and the supervisor parent's is User Guardian actor.  The default strategy for User Guardian
      //is to simply RESTART the child (supervisor).  Since we do not kill any children of kind supervisor
      //in the preRestart method, the child (FussyWordCounter) is simply RESTART(ed).
      child ! 43

      child ! Report
      expectMsg(0)

    }
  }

  "an all for one supervisor" should {
    "apply all for one strategy" in {
      val supervisor = system.actorOf(Props[AllForOneSupervisor], "supervisor")
      supervisor ! Props[FussyWordCounter]
      val child = expectMsgType[ActorRef]

      supervisor ! Props[FussyWordCounter]
      val child2 = expectMsgType[ActorRef]

      watch(child)
      watch(child2)

      child ! "I love akka"
      child ! Report
      expectMsg(3)

      child2 ! "I love akka"
      child2 ! Report
      expectMsg(3)

      //cause an error on child one
      //we need to wait for the child to throw exception
      EventFilter[NullPointerException]() intercept {
        child ! ""
      }

      Thread.sleep(500)
      //child2 is RESTART(ed)
      child2 ! Report
      expectMsg(0)

    }
  }

}

object SupervisionSpec {

  case object Report

  //regular supervisor
  class Supervisor extends Actor with ActorLogging {

    //OneForOneStrategy will apply the Directive to the child that caused the error
    override val supervisorStrategy: SupervisorStrategy = OneForOneStrategy() {
      case _: NullPointerException => Restart
      case _: IllegalArgumentException => Stop
      case _: RuntimeException => Resume
      case _: Exception => Escalate
    }

    override def receive: Receive = {
      case props: Props =>
        val childRef = context.actorOf(props)
        sender() ! childRef
    }
  }

  class NoDeathOnRestartSupervisor extends Supervisor {
    override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
      //empty
      // this supervisor is kinder than regular one
      // overriding this will make children stay alive
    }
  }


  //AllForOneStrategy will apply the Directive to all the children even if one causes the error
  class AllForOneSupervisor extends Supervisor {
    override val supervisorStrategy: SupervisorStrategy = AllForOneStrategy() {
      case _: NullPointerException => Restart
      case _: IllegalArgumentException => Stop
      case _: RuntimeException => Resume
      case _: Exception => Escalate
    }
  }

  class FussyWordCounter extends Actor with ActorLogging {
    var words = 0

    override def receive: Receive = {
      case Report => sender() ! words
      case "" => throw new NullPointerException("sentence is empty")
      case sentence: String =>
        if (sentence.length > 20) throw new RuntimeException("sentence is too big")
        else if (!Character.isUpperCase(sentence.head)) throw new IllegalArgumentException("sentence must start with an uppercase")
        else words += sentence.split(" ").length
      case _ => throw new Exception("can only receive strings")
    }
  }

}