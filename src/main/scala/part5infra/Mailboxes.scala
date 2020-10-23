package part5infra

import akka.actor.{Actor, ActorLogging, ActorSystem, PoisonPill, Props}
import akka.dispatch.{ControlMessage, PriorityGenerator, UnboundedPriorityMailbox}
import com.typesafe.config.{Config, ConfigFactory}

object Mailboxes extends App {

  val system = ActorSystem("MailboxDemo", ConfigFactory.load().getConfig("mailboxesDemo"))

  class SimpleActor extends Actor with ActorLogging {
    override def receive: Receive = {
      case message => log.info(message.toString)
    }
  }

  /**
    * Custom priority mailbox
    *
    * P0 -> important
    * P1 ->
    * P2 ->
    * P3 ->
    */

  //step 1 - mailbox definition
  class SupportTickerPriorityMailbox(settings: ActorSystem.Settings, config: Config)
    extends UnboundedPriorityMailbox(
      PriorityGenerator {
        case message: String if message.startsWith("[P0]") => 0
        case message: String if message.startsWith("[P1]") => 1
        case message: String if message.startsWith("[P2]") => 2
        case message: String if message.startsWith("[P3]") => 3
        case _ => 4
      }
    )

  //step 2 - make it known in a config
  //step 3 - attach the dispatcher to an actor

  val supportStickerLogger = system.actorOf(Props[SimpleActor].withDispatcher("support-ticket-dispatcher"))

  //  supportStickerLogger ! PoisonPill
  //  Thread.sleep(1000)
  //  supportStickerLogger ! "[P3] This thing would be nice to have"
  //  supportStickerLogger ! "[P0] This needs to be solved NOW!"
  //  supportStickerLogger ! "[P1] do this when you have time"

  //after which time can I send another message and be prioritized accordingly? you cant set this

  /**
    * Interesting case #2: control-aware-mailbox
    * we'll use UnboundedControlAwareMailbox
    */

  //step 1: mark important messages as control messages
  case object ManagementTicket extends ControlMessage

  //step 2: configure who gets the mailbox
  //  - make the actor attach to the mailbox

  //method #1
  val controlAwareActor = system.actorOf(Props[SimpleActor].withDispatcher("control-mailbox"))
  //  controlAwareActor ! "[P0] This needs to be solved NOW!"
  //  controlAwareActor ! "[P1] do this when you have time"
  //  controlAwareActor ! ManagementTicket

  //method #2 - using deployment config
  val altConrolAwareActor = system.actorOf(Props[SimpleActor], "altConrolAwareActor")

  altConrolAwareActor ! "[P0] This needs to be solved NOW!"
  altConrolAwareActor ! "[P1] do this when you have time"
  altConrolAwareActor ! ManagementTicket


}
