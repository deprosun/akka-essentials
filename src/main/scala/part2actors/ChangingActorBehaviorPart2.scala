package part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import part2actors.ChangingActorBehaviorPart2.Counter.{Decrement, Increment, Print}
//import part2actors.ChangingActorBehaviorPart2.Mom.MomStart


object ChangingActorBehaviorPart2 extends App {

  //  object FussyKid {
  //
  //    case object KidAccept
  //
  //    case object KidReject
  //
  //    val HAPPY = "Happy"
  //    val SAD = "Sad"
  //  }
  //
  //  class FussyKid extends Actor {
  //
  //    import Mom._
  //    import FussyKid._
  //
  //    var state: String = HAPPY
  //
  //    override def receive: Receive = {
  //      case Food(VEGETABLE) => state = SAD
  //      case Food(CHOCOLATE) => state = HAPPY
  //      case Ask(_) if state == HAPPY => sender() ! KidAccept
  //      case Ask(_) if state == SAD => sender() ! KidReject
  //    }
  //  }
  //
  //  object Mom {
  //    val VEGETABLE = "Vegetable"
  //    val CHOCOLATE = "Chocolate"
  //
  //    case class Food(name: String)
  //
  //    case class Ask(message: String)
  //
  //    case class MomStart(kidRef: ActorRef)
  //
  //  }
  //
  //  class Mom extends Actor {
  //
  //    import Mom._
  //    import FussyKid._
  //
  //    override def receive: Receive = {
  //      case MomStart(kidRef) =>
  //        kidRef ! Food(VEGETABLE)
  //        kidRef ! Food(VEGETABLE)
  //        kidRef ! Food(CHOCOLATE)
  //        kidRef ! Food(CHOCOLATE)
  //        kidRef ! Ask("do you want to play?")
  //      case KidReject => println("My kid is sad, but at least he is healthy!")
  //      case KidAccept => println("Yay, my kid is happy!")
  //    }
  //  }
  //
  //  class StatelessFussyKid extends Actor {
  //
  //    import FussyKid._
  //    import Mom._
  //
  //    override def receive: Receive = happyReceive
  //
  //    def happyReceive: Receive = {
  //      case Food(VEGETABLE) => context.become(sadReceive, discardOld = false)
  //      case Food(CHOCOLATE) =>
  //      case Ask(_) => sender() ! KidAccept
  //    }
  //
  //    def sadReceive: Receive = {
  //      case Food(VEGETABLE) => context.become(sadReceive, discardOld = false)
  //      case Food(CHOCOLATE) => context.unbecome()
  //      case Ask(_) => sender() ! KidReject
  //    }
  //  }
  //
  val actorSystem = ActorSystem("changingActorBehaviorDemo")

  //
  //  val fussyKid: ActorRef = actorSystem.actorOf(Props[FussyKid])
  //  val statelessFussyKid: ActorRef = actorSystem.actorOf(Props[StatelessFussyKid])
  //  val mom: ActorRef = actorSystem.actorOf(Props[Mom])
  //
  //  mom ! MomStart(statelessFussyKid)

  /**
    * Mom receives MomStart
    *   - kid receives Food(Veg) -> kid will change the handler to sadReceive
    *   - kid receives Food(Choco) -> kid will change the handler to happyReceive
    */

  /**
    * context.become
    *
    * Food(Veg) -> stack.push(sadReceive)
    * Food(Choco) -> stack.push(happyReceive)
    *
    * Stack:
    * 1. happyReceive
    * 2. sadReceive
    * 3. happyReceive
    *
    * NOTE: if the stack is empty, akka will call the plain receive handler
    *
    */

  /**
    * new behavior
    * Food(Veg)
    * Food(Veg)
    * Food(Choco)
    * Food(Choco)
    *
    * Stack:
    * |happyReceive|
    * |            |
    *
    * -- Food(Veg)
    *   1. Execute whats on top of stack, here happyReceive
    * a] happyReceive: Push sadReceive
    * Stack:
    * |sadReceive  |
    * |happyReceive|
    * |            |
    * -- Food(Veg)
    *   1. Execute whats on top of stack, here sadReceive
    * a] sadReceive: Push sadReceive
    * Stack:
    * |sadReceive  |
    * |sadReceive  |
    * |happyReceive|
    * |            |
    *
    * -- Food(Choco)
    *   1. Execute whats on top of stack, here sadReceive
    * a] sadReceive: Pop whats on stack
    * Stack:
    * |sadReceive  |
    * |happyReceive|
    * |            |
    *
    * -- Food(Choco)
    *   1. Execute whats on top of stack, here sadReceive
    * a] sadReceive: Pop whats on stack
    * Stack:
    * |happyReceive|
    * |            |
    *
    * -- Ask()
    *   1. Execute whats on top of stack, here happyReceive
    * a] happyReceive: send accept
    * Stack:
    * |happyReceive|
    * |            |
    *
    *
    *
    * Learning:
    *   1. You can change the state of the actors behavior using Stack.  The Stack enables you to push different receive
    *      functions.  Therefore, next message sent to the actor will behave different for the same
    * message because a different receive function is operating this time.
    *   2. stack.push = context.become(happyReceive, discardOld = false)
    *   3. stack.pop = context.unbecome()
    *
    * Observation:
    *   1. If multiple sender actors are sending messages to the same receiver actor, you could have a really complex
    * stack.
    */

  /**
    * Exercise 1:
    * recreate the Counter Actor with context.become and NO MUTABLE STATE
    * Hint: Receive function might need some parameters
    */

  object Counter {

    case object Increment

    case object Decrement

    case object Print

  }

  //  class Counter extends Actor {
  //    override def receive: Receive = update(0)
  //
  //    def update(current: Int): Receive = {
  //      case Increment => context.become(update(current + 1), discardOld = true)
  //      case Decrement => context.become(update(current - 1), discardOld = true)
  //      case Print => println(current)
  //    }
  //  }
  //
  //  import Counter._
  //
  //  val counter = actorSystem.actorOf(Props[Counter], "myCounter")
  //
  //  (1 to 5).foreach(_ => counter ! Increment)
  //  (1 to 3).foreach(_ => counter ! Decrement)
  //  counter ! Print

  /**
    * Exercise 2:
    * a simplified voting system
    *
    */

  /*
   *   When citizen C votes for candidate P, then C is marked with a state called HAVING VOTED with the candidate P such
   *   that the vote is exactly once.
   *
   *   VoteStatusRequest is the request VoteAggregator gives to the citizen to know who they voted for
   *   VoteAggregator asks the Citizen who they have voted for if the Citizen has done so.
   *
   *
   */

  //  object Vote {
  //    val HAVING_VOTED = "HAVING VOTED"
  //  }

  case class Vote(candidate: String)

  case object VoteStatusRequest

  case class VoteStatusReply(candidate: Option[String])

  class Citizen extends Actor {

    override def receive: Receive = {
      case Vote(c) => context.become(voted(Some(c)), discardOld = true)
      case VoteStatusRequest => sender() ! VoteStatusReply(None)
    }

    def voted(candidate: Option[String]): Receive = {
      case VoteStatusRequest => sender() ! VoteStatusReply(candidate)
    }

  }


  case class AggregateVotes(citizens: Set[ActorRef])

  class VoteAggregator extends Actor {
    override def receive: Receive = awaitingCommand

    def awaitingCommand: Receive = {
      case AggregateVotes(citizens) =>
        citizens foreach (_ ! VoteStatusRequest)
        context.become(aggregate(Map(), citizens))
    }

    def aggregate(current: Map[String, Int], stillWaiting: Set[ActorRef]): Receive = {

      case VoteStatusReply(Some(candidate)) =>

        val newVote = current.getOrElse(candidate, 0) + 1
        val newStats = current + (candidate -> newVote)
        val newStillWaiting = stillWaiting - sender()

        if (newStillWaiting.isEmpty) newStats foreach println
        else context.become(aggregate(newStats, newStillWaiting), discardOld = true)

      case VoteStatusReply(None) =>
        sender() ! VoteStatusRequest

      case Print =>
        //what we learnt here is that Print might look like a good feature
        //to have but since it just ANOTHER asynchronous call that gets queued to the
        //mailbox, the moment this call gets picked up by the thread it is impossible to understand
        //which "content.become" state it is running from previous cases
        current foreach println

    }
  }

  val alice = actorSystem.actorOf(Props[Citizen], "alice")
  val bob = actorSystem.actorOf(Props[Citizen], "bob")
  val charlie = actorSystem.actorOf(Props[Citizen], "charlie")
  val daniel = actorSystem.actorOf(Props[Citizen], "daniel")

  alice ! Vote("Martin") //founder of scala
  bob ! Vote("Jonas") // started the akka project
  charlie ! Vote("Roland") // main contributor akka project
  daniel ! Vote("Roland")

  val voteAggregator = actorSystem.actorOf(Props[VoteAggregator])
  voteAggregator ! AggregateVotes(Set(alice, bob, charlie, daniel))
  //  voteAggregator ! Print

  /*  Print the status of the votes
      printed to the console, every candidate and total number of votes each received

      Martin -> 1
      Jonas -> 1
      Roland -> 2
   */


}
