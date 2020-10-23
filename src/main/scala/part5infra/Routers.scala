package part5infra

import akka.actor.{Actor, ActorLogging, ActorSystem, Props, Terminated}
import akka.routing.{ActorRefRoutee, Broadcast, FromConfig, RoundRobinGroup, RoundRobinPool, RoundRobinRoutingLogic, Router}
import com.typesafe.config.ConfigFactory

import scala.language.postfixOps

object Routers extends App {

  /**
    * #1 Manual Router
    */
  class Master extends Actor {
    //step 1 create routees
    // 5 actor routees based on slave actors
    private val slaves = 1 to 5 map { x =>
      val slave = context.actorOf(Props[Slave], s"worker$x")
      context.watch(slave)
      ActorRefRoutee(slave) //todo
    }

    //step 2: define the router
    private val router: Router = Router(RoundRobinRoutingLogic(), slaves)

    //step 3: handle some messages to these routees
    override def receive: Receive = process(router)

    def process(router: Router): Receive = {
      //step 4: handle the termination/lifecycle of the routees
      case Terminated(ref) =>
        //lets say we want to simply replace slave after termination
        //first, remove the routee from the router
        val removed = router.removeRoutee(ref)
        val newSlave = context.actorOf(Props[Slave], s"replaced_${ref.path.name}")
        val added = removed.addRoutee(newSlave)

        context.become(process(added))

      case message =>
        //this route function takes the sender so that slaves can directly reply to the requester
        //which is the parent of Master if you will
        router.route(message, sender())
    }
  }

  class Slave extends Actor with ActorLogging {
    override def receive: Receive = {
      case message => log.info(message.toString)
    }
  }

  val system = ActorSystem("RoutersDemo", /*TODO = */ config = ConfigFactory.load().getConfig("routersDemo"))
  val master = system.actorOf(Props[Master], "master")

  //  1 to 10 foreach { i =>
  //    master ! s"[$i] hello from the world"
  //  }

  /**
    * Method #2: a router with its own children
    * POOL router
    */
  //2.1 programatically (in code)
  val poolMaster = system.actorOf(RoundRobinPool(5).props(Props[Slave]), "simplePoolMaster")

  //  0 to 10 foreach { i =>
  //    poolMaster ! s"[$i] hello from the world"
  //  }

  //2.2 from configuration
  val poolMaster2 = system.actorOf(FromConfig.props(Props[Slave]), "poolMaster2")

  //  0 to 10 foreach { i =>
  //    poolMaster2 ! s"[$i] hello from the world"
  //  }

  /**
    * Method #3: router with actors created elsewhere
    * GROUP router
    */

  //in another part of my application
  val slaveList = 1 to 5 map { i =>
    system.actorOf(Props[Slave], s"slave_$i")
  } toList

  //need their paths
  val slavePaths = slaveList map {
    _.path.toString
  }

  //3.1
  // just `props()`, not a word more.  this is because we have already created the actors and started them above
  val groupMaster = system.actorOf(RoundRobinGroup(slavePaths).props())
  //  0 to 10 foreach { i =>
  //    groupMaster ! s"[$i] hello from the world"
  //  }

  //3.2 from configuration
  val groupMaster2 = system.actorOf(FromConfig.props(), "groupMaster2")
  0 to 10 foreach { i =>
    groupMaster2 ! s"[$i] hello from the world"
  }

  /**
    * Special Messages
    */
  groupMaster2 ! Broadcast("hello, everyone")

  // PoisonPill and Kill are NOT routed
  // AddRoutee, Remove, Get handled by the routing actor
}
