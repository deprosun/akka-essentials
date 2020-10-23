package part5infra

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import com.typesafe.config.ConfigFactory

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.Random

object Dispatchers extends App {

  class Counter extends Actor with ActorLogging {
    var count = 0

    override def receive: Receive = {
      case message =>
        count += 1
        log.info(s"[$count]${message.toString}")
    }
  }

  val system = ActorSystem("DispatchersDemo") //, ConfigFactory.load().getConfig("dispatchersDemo"))

  //  // method #1 - programmatic / in code
  //  val actors = 1 to 10 map { i =>
  //    system.actorOf(Props[Counter].withDispatcher("my-dispatcher"), s"counter_$i")
  //  }
  //
  //  val r = new Random()
  //  0 to 1000 foreach { i =>
  //    actors(r.nextInt(10)) ! i
  //  }
  //method #2 from config
  val rtjvmActor = system.actorOf(Props[Counter], "rtjvm")

  /**
    * dispatcher implement execution context trait
    */

  class DBActor extends Actor with ActorLogging {

    //so the future will use execution context generated from the dispatcher.
    //the future will run on the context's dispatcher.  so kinda like if you need
    //to read the data from a database you use dispatcher effort
    //    implicit val executionContext: ExecutionContextExecutor = context.dispatcher

    //or you can use a dedicated dispatcher for blocking calls
    // solution #1
    implicit val executionContext: ExecutionContextExecutor = context.system.dispatchers.lookup("my-dispatcher")

    // solution #2
    //use a router


    override def receive: Receive = {
      case message =>
        Future {
          Thread.sleep(5000)
          log.info(s"Success: $message")
        }
    }
  }

  val dBActor = system.actorOf(Props[DBActor], "dbActor")

  dBActor ! "Meaning of life is 42"

  val nonBlockingActor = system.actorOf(Props[Counter])

  0 to 1000 foreach { i =>
    val message = s"important message $i"
    dBActor ! message
    nonBlockingActor ! message

  }

}
