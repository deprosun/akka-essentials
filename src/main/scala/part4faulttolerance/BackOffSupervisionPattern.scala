package part4faulttolerance

import java.io.File

import akka.actor.SupervisorStrategy.Stop
import akka.actor.{Actor, ActorInitializationException, ActorLogging, ActorSystem, OneForOneStrategy, Props}
import akka.pattern.{BackoffOpts, BackoffSupervisor}

import scala.concurrent.duration._
import scala.io.Source
import scala.language.postfixOps

object BackOffSupervisionPattern extends App {

  case object ReadFile

  class FileBasedPersistentActor extends Actor with ActorLogging {
    var dataSource: Source = _

    override def preStart(): Unit = {
      log.info(s"Persistent actor starting")
    }

    override def postStop(): Unit = {
      log.warning(s"Persistent actor has stopped")
    }

    override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
      log.warning("Persistent actor restarting")
    }

    override def receive: Receive = {
      case ReadFile =>

        dataSource = Option(dataSource) getOrElse {
          Source.fromFile(new File("src/main/resources/testfiles/important_data.txt"))
        }

        log.info("I've just read some IMPORTANT data: " + dataSource.getLines().toList)
    }
  }

  val system = ActorSystem("BackoffSupervisionDemo")
  //  val simpleActor = system.actorOf(Props[FileBasedPersistentActor], "simpleActor")
  //
  //  simpleActor ! ReadFile

  val simpleSupervisionProps = BackoffSupervisor.props(
    BackoffOpts.onFailure(
      Props[FileBasedPersistentActor],
      "simpleBackoffActor",
      3 seconds,
      30 seconds,
      0.2
    )
  )

  //  val simpleBackoffSupervisor = system.actorOf(simpleSupervisionProps, "simpleSupervisor")
  //  simpleBackoffSupervisor ! ReadFile

  /**
    * simpleSupervisor
    * - child called simpleBackoffSupervisor (props of type FileBasedPersistentActor)
    * - supervision strategy
    *   - first attempt after 3 seconds
    *   - second attempt is 2x the previous attempt
    */

  val stopSupervisorProps = BackoffSupervisor.props(
    BackoffOpts.onStop(
      Props[FileBasedPersistentActor],
      "stopBackoffActor",
      3 seconds,
      30 seconds,
      0.2
    ).withSupervisorStrategy(
      OneForOneStrategy() {
        case _ => Stop
      }
    )
  )

  //  val stopSupervisor = system.actorOf(stopSupervisorProps, "stopSupervisor")
  //  stopSupervisor ! ReadFile

  class EagerFBPActor extends FileBasedPersistentActor {
    override def preStart(): Unit = {
      log.info("Eager actor starting")
      dataSource = Source.fromFile(new File("src/main/resources/testfiles/important_data.txt"))
    }
  }

  //ActorInitializationException => STOP

  val repeatedSupervisorProps = BackoffSupervisor.props(
    BackoffOpts.onStop(
      Props[EagerFBPActor],
      "eagerActor",
      1 second,
      30 seconds,
      0.1
    )
  )

  val repatedSupervisor = system.actorOf(repeatedSupervisorProps, "eagerSupervisor")
  //repatedSupervisor ! ReadFile <---- we dont need go do this the above line will execute preStart

}
