package part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import part2actors.ChildActors.Parent.{CreateChild, TellChild}

object ChildActors extends App {

  object Parent {

    case class CreateChild(name: String)

    case class TellChild(message: String)

  }

  class Parent extends Actor {
    override def receive: Receive = {
      case CreateChild(name) =>
        println(s"[${self.path}] creating child")
        val child = context.actorOf(Props[Child], name)
        context.become(withChild(child), discardOld = true)
    }

    def withChild(child: ActorRef): Receive = {
      case TellChild(message) => child forward message
    }
  }

  class Child extends Actor {
    override def receive: Receive = {
      case message: String => println(s"[${self.path}] I got: $message")
    }
  }

  val actorSystem = ActorSystem("childActorDemo")

  val parent = actorSystem.actorOf(Props[Parent], "parent")
  parent ! CreateChild("child")
  parent ! TellChild("hey kid!")

  //actor hierarchies
  //parent -> child -> grandchild
  //       -> child2 ->
  // Who owns parent?

  /*
    * Guardian actors (top-level)
    *   - /system = system guardian
    *   - /user   = user-level guardian
    *   - /       = root guardian
   */

  /**
    * Actor Selection
    */
  val childSelection = actorSystem.actorSelection("/user/parent/child")
  childSelection ! "I found you!"

  /**
    * DANGER!
    *
    * NEVER PASS MUTABLE ACTOR STATE, OR `this` REFERENCE,TO CHILD ACTORS.
    *
    * NEVER IN YOUR LIFE!
    */

}
