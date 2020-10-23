package part4faulttolerance

import akka.actor.{Actor, ActorLogging, ActorSystem, PoisonPill, Props}

object ActorLifecycle extends App {

  object StartChild

  class LifeCycleActor extends Actor with ActorLogging {
    override def preStart(): Unit = log.info("I am starting")

    override def postStop(): Unit = log.info("I have stopped")

    override def receive: Receive = {
      case StartChild => context.actorOf(Props[LifeCycleActor], "child")
    }
  }

  val system = ActorSystem("LifecycleDemo")

  //  val parent = system.actorOf(Props[LifeCycleActor], "parent")
  //
  //  parent ! StartChild
  //
  //  parent ! PoisonPill

  /**
    * restart
    */
  object Fail

  object FailChild

  object CheckChild

  object Check

  class Parent extends Actor with ActorLogging {
    private val child = context.actorOf(Props[Child], "supervisedChild")

    override def receive: Receive = {
      case FailChild => child ! Fail
      case CheckChild => child ! Check
    }

  }

  class Child extends Actor with ActorLogging {

    override def preStart(): Unit = log.info("supervised child started")

    override def postStop(): Unit = log.info("supervised child stopped")

    // this is being called by the old actor instance before it is swapped
    override def preRestart(reason: Throwable, message: Option[Any]): Unit =
      log.info(s"supervised child restarting because of ${reason.getMessage}")

    // this is being called by the new actor instance that has been inserted
    override def postRestart(reason: Throwable): Unit =
      log.info(s"supervised actor restarted")

    override def receive: Receive = {
      case Fail =>
        log.warning("Child will fail now")
        throw new RuntimeException("I failed.")
      case Check => log.info("alive and kicking")
    }

  }

  val supervisor = system.actorOf(Props[Parent], "supervisor")
  supervisor ! FailChild
  supervisor ! CheckChild

  // even though the child actor threw an exception, it was able to restart itself

  // default supervision strategy: if an actor threw an exception during processing a message,
  // the message is removed from the queue and not put back and the mailbox is untouched!

}
