package part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object ActorCapabilities extends App {

  class SimpleActor extends Actor {
    override def receive: Receive = {
      case "Hi" => context.sender() ! "Hello, there!"
      case message: String => println(s"[$self] I have received $message")
      case number: Int => println(s"[simple actor] I have received a NUMBER: $number")
      case SpecialMessage(content) => println(s"[simple actor] I have received something SPECIAL: $content")
      case SendMessageToYourself(content) => self ! content
      case SayHiTo(ref) => ref ! "Hi" //self is the sender being sent as implicit
      case WirelessPhoneMessage(content, ref) => ref forward  (content + "s") //ref.sender() is the sender, NOT self. whooever ref is ..
    }
  }

  val actorSystem = ActorSystem("actorCapabilitiesDemo")
  val simpleActor = actorSystem.actorOf(Props[SimpleActor], "simpleActor")

  //  simpleActor ! "hello, actor"

  //1 messages can be of any type
  //a) messages must be IMMUTABLE
  //b) messages must be SERIALIZABLE
  //in practice use case classes and case objects
  //  simpleActor ! 42

  //2 actors have information about their context and about themselves
  //context.self === `this` in OOP

  case class SendMessageToYourself(content: String)

  //  simpleActor ! SendMessageToYourself("I am an actor and I am proud of it")

  //3 - actors can REPLY to messages
  val alice: ActorRef = actorSystem.actorOf(Props[SimpleActor], "alice")
  val bob = actorSystem.actorOf(Props[SimpleActor], "bob")

  case class SayHiTo(ref: ActorRef)

  //  alice ! SayHiTo(bob)

  //4 - dead letters because self is null as implicit
  //  alice ! "Hi"

  //5 - forward messages
  //D -> A -> B
  //forwarding = sending a message with the ORIGINAL sender

  case class WirelessPhoneMessage(content: String, ref: ActorRef)

  alice ! WirelessPhoneMessage("Hi", bob)

}

case class SpecialMessage(content: String)
