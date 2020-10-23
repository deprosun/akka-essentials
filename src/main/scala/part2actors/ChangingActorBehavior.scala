package part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import part2actors.ChangingActorBehavior.Mom.MomStart

object ChangingActorBehavior extends App {

  object FussyKid {

    case object KidAccept

    case object KidReject

    val HAPPY = "Happy"
    val SAD = "Sad"
  }

  class FussyKid extends Actor {

    import FussyKid._
    import Mom._

    var state: String = HAPPY

    override def receive: Receive = {
      case Food(VEGETABLE) => state = SAD
      case Food(CHOCOLATE) => state = HAPPY
      case Ask(_) if state == HAPPY => sender() ! KidAccept
      case Ask(_) if state == SAD => sender() ! KidReject
    }
  }

  object Mom {
    val VEGETABLE = "Vegetable"
    val CHOCOLATE = "Chocolate"

    case class Food(name: String)

    case class Ask(message: String)

    case class MomStart(kidRef: ActorRef)

  }

  class Mom extends Actor {

    import FussyKid._
    import Mom._

    override def receive: Receive = {
      case MomStart(kidRef) =>
        kidRef ! Food(VEGETABLE)
        kidRef ! Ask("do you want to play?")
      case KidReject => println("My kid is sad, but at least he is healthy!")
      case KidAccept => println("Yay, my kid is happy!")
    }
  }

  class StatelessFussyKid extends Actor {

    import FussyKid._
    import Mom._

    override def receive: Receive = happyReceive

    def happyReceive: Receive = {
      case Food(VEGETABLE) => context.become(sadReceive)
      case Food(CHOCOLATE) => //stay happy
      case Ask(_) => sender() ! KidAccept
    }

    def sadReceive: Receive = {
      case Food(VEGETABLE) => //stay sad
      case Food(CHOCOLATE) => context.become(happyReceive)
      case Ask(_) => sender() ! KidReject
    }
  }

  val actorSystem = ActorSystem("changingActorBehaviorDemo")

  val fussyKid = actorSystem.actorOf(Props[FussyKid])
  val statelessFussyKid = actorSystem.actorOf(Props[StatelessFussyKid])
  val mom = actorSystem.actorOf(Props[Mom])

  mom ! MomStart(statelessFussyKid)

  /**
    * Mom receives MomStart
    *   - kid receives Food(Veg) -> kid will change the handler to sadReceive
    *   - kid receives Food(Choco) -> kid will change the handler to happyReceive
    */

}
