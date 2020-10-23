package part4faulttolerance

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Kill, PoisonPill, Props, Terminated}

object StartingStoppingActors extends App {

  val system = ActorSystem("StoppingActorsDemo")

  object Parent {

    case class StartChild(name: String)

    case class StopChild(name: String)

    case object Stop

  }

  class Parent extends Actor with ActorLogging {

    import Parent._

    override def receive: Receive = withChildren(Map())

    def withChildren(children: Map[String, ActorRef]): Receive = {
      case StartChild(name) =>
        log.info("Starting child actor")
        val newChild = name -> context.actorOf(Props[Child], name)
        context.become(withChildren(children + newChild))
      case StopChild(name) =>
        log.info(s"Stopping child with name $name")

        children.get(name) match {
          case None => log.warning(s"Child with name $name doesn't exist")
          case Some(childRef) => context.stop(childRef)
        }
      case Stop =>
        log.info(s"Stopping myself")
        context.stop(self)
      case message => log.info(message.toString)
    }
  }

  class Child extends Actor with ActorLogging {
    override def receive: Receive = {
      case message => log.info(message.toString)
    }
  }

  import Parent._

  /**
    * method #1 context.stop
    * when you stop a parent, all the children will stop first and then the parent
    */
  //  val parent = system.actorOf(Props[Parent], "parent")
  //  parent ! StartChild("child1")
  //  val child = system.actorSelection("/user/parent/child1")
  //  child ! "Hi kid!"
  //
  //
  //  //stop signal in actor happen async.  Therefore, child can still process few messages in its queue after get the signal to stop
  //  parent ! StopChild("child1")
  //  //  1 to 50 foreach { _ =>
  //  //    child ! "I am still here"
  //  //  }
  //
  //  parent ! StartChild("child2")
  //  val child2 = system.actorSelection("/user/parent/child2")
  //  child2 ! "Hi child2!"
  //  parent ! Stop
  //
  //  1 to 50 foreach { _ =>
  //    parent ! "Parent still here"
  //  }
  //
  //  1 to 100 foreach { _ =>
  //    child2 ! "Child still here"
  //  }


  /**
    * method #2 - using special messages
    */
  //  val looseActor = system.actorOf(Props[Child])
  //
  //  looseActor ! "hello, loose actor"
  //  looseActor ! PoisonPill
  //  looseActor ! "hello, loose actor. are you still there?"
  //
  //  val abruptlyTerminatedActor = system.actorOf(Props[Child])
  //  abruptlyTerminatedActor ! "you are about to be terminated"
  //  abruptlyTerminatedActor ! Kill
  //  abruptlyTerminatedActor ! "you have been terminated"

  /**
    * Death Watch
    */

  class Watcher extends Actor with ActorLogging {

    import Parent._

    override def receive: Receive = {
      case StartChild(name) =>
        val child = context.actorOf(Props[Child], name)
        log.info("Started and watching child")
        context.watch(child)
      case Terminated(ref) =>
        log.info(s"The reference that I am watching $ref has been stopped")
    }
  }

  val watcher = system.actorOf(Props[Watcher], "watcher")
  watcher ! StartChild("watchedChild")
  val watchedChild = system.actorSelection("/user/watcher/watchedChild")
  Thread.sleep(500)
  watchedChild ! PoisonPill

}
