package part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import part2actors.ChildActorExercise.WordCounterMaster.{Initialize, WordCountReply, WordCountTask}

import scala.language.postfixOps

object ChildActorExercise extends App {

  object WordCounterMaster {

    case class Initialize(nChildren: Int)

    case class WordCountTask(id: Int, text: String)

    case class WordCountReply(id: Int, count: Int)

  }

  class WordCounterMaster extends Actor {

    import WordCounterMaster._

    override def receive: Receive = {
      case Initialize(n) =>

        println(s"[master] initializing")

        val workers = 1 to n map (_ => context.actorOf(Props[WordCounterWorker])) toList

        context.become(runTasks(workers, 0, 0, Map()), discardOld = true)
    }

    def runTasks(workers: List[ActorRef], currentChildIndex: Int, currentId: Int, requestMap: Map[Int, ActorRef]): Receive = {
      case text: String =>
        println(s"[master] I have received: $text - I will send it to child $currentChildIndex")
        val originalSender = sender()
        workers(currentChildIndex) ! WordCountTask(currentId, text)

        val newChildIndex = (currentChildIndex + 1) % workers.length
        context.become(runTasks(workers, newChildIndex, currentId + 1, requestMap + (currentId -> originalSender)), discardOld = true)

      case WordCountReply(id, c) =>
        println(s"[master] I have received a reply for task id $id with $c")
        val originalSender = requestMap(id)
        originalSender ! c
        context.become(runTasks(workers, currentChildIndex, currentId, requestMap - id), discardOld = true)

    }
  }

  class WordCounterWorker extends Actor {
    override def receive: Receive = {
      case WordCountTask(id, text) =>
        println(s"${self.path} I have received a task $id with $text")
        sender() ! WordCountReply(id, text.split(" ").length)
    }
  }

  case class Request(WordCounterMaster: ActorRef, text: String)

  class Requester extends Actor {
    override def receive: Receive = {
      case Request(master, text) =>
        master ! text
      case c: Int => println(s"the count is $c")
    }
  }

  val actorSystem = ActorSystem("actorSystem")

  val requester = actorSystem.actorOf(Props[Requester])

  val counterMaster = actorSystem.actorOf(Props[WordCounterMaster])
  counterMaster ! Initialize(10)

  requester ! Request(counterMaster, "Akka is awesome")
  requester ! Request(counterMaster, "Akka is awesomeAkka is awesome")
  //  requester ! Request(counterMaster, "Akka is awesome")
  //  requester ! Request(counterMaster, "Akka is awesomeAkka is awesome")
  //  requester ! Request(counterMaster, "Akka is awesome")
  //  requester ! Request(counterMaster, "Akka is awesomeAkka is awesome")
  //  requester ! Request(counterMaster, "Akka is awesome")
  //  requester ! Request(counterMaster, "Akka is awesomeAkka is awesome")
  //  requester ! Request(counterMaster, "Akka is awesome")
  //  requester ! Request(counterMaster, "Akka is awesomeAkka is awesome")

}
