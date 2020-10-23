package part5infra

import akka.actor.SupervisorStrategy.Stop
import akka.actor.{Actor, ActorLogging, ActorSystem, Cancellable, PoisonPill, Props, Timers}

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._
import scala.language.postfixOps

object TimerSchedulers extends App {

  class SimpleActor extends Actor with ActorLogging {
    override def receive: Receive = {
      case message => log.info(message.toString)
    }
  }

  val system = ActorSystem("SchedulersTimersDemo")
  val simpleActor = system.actorOf(Props[SimpleActor])

  system.log.info("Scheduling reminder for simpleActor")

  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  //  system.scheduler.scheduleOnce(1 second)(
  //    simpleActor ! "reminder"
  //  )
  //
  //  val routine: Cancellable =
  //    system
  //      .scheduler
  //      .scheduleWithFixedDelay(1 second, 2 seconds, simpleActor, "heartbeat")
  //
  //  system.scheduler.scheduleOnce(5 seconds) {
  //    routine.cancel()
  //  }

  /**
    * Exercise: implement a self closing actor
    *
    * - if the actor receives a message (anything), you have 1 second to send it another message
    * - if the time window expires, the actor will stop itself
    * - if you send another message, the time window is reset
    */

  class SelfClosingActor extends Actor with ActorLogging {

    def createTimeWindow(): Cancellable = {
      context.system.scheduler.scheduleOnce(1 second) {
        self ! "timeout"
      }
    }

    override def receive: Receive = withScheduler(createTimeWindow())

    def withScheduler(scheduler: Cancellable): Receive = {
      case "timeout" =>
        log.info("Stopping myself")
        context.stop(self)
      case message =>
        log.info(s"Received $message, staying alive")
        scheduler.cancel()
        context.become(withScheduler(createTimeWindow()))

    }
  }

  //  val selfClosingActor = system.actorOf(Props[SelfClosingActor], "selfClosingActor")
  //
  //  system.scheduler.scheduleOnce(250 millis) {
  //    selfClosingActor ! "ping"
  //  }
  //
  //  system.scheduler.scheduleOnce(2 seconds) {
  //    system.log.info(s"sending pong to the self closing actor")
  //    selfClosingActor ! "pong"
  //  }

  /**
    * Timer
    */

  case object TimerKey

  case object Start

  case object Reminder

  case object Stop

  class TimeBasedHeartbeatActor extends Actor with ActorLogging with Timers {
    timers.startSingleTimer(TimerKey, Start, 500 millis)

    override def receive: Receive = {
      case Start =>
        log.info("Bootstrapping")
        timers.startTimerAtFixedRate(TimerKey, Reminder, 1 second)
      case Reminder =>
        log.info("I am alive")
      case Stop =>
        log.warning("Stopping!")
        timers.cancel(TimerKey)
        context.stop(self)
    }
  }

  val timerHeartbeatActor = system.actorOf(Props[TimeBasedHeartbeatActor], "timerActor")

  system.scheduler.scheduleOnce(5 seconds) {
    timerHeartbeatActor ! Stop
  }


}
