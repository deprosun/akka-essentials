package part2actors

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import com.typesafe.config.ConfigFactory

object IntroAkkaConfig extends App {

  class SimpleLoggingActor extends Actor with ActorLogging {
    override def receive: Receive = {
      case message => log.info(message.toString)
    }
  }

  /**
    * 1 - inline configuration
    */

  val configString =
    """
      |akka {
      | loglevel = "ERROR"
      |}
    """.stripMargin

  val config = ConfigFactory.parseString(configString)
  val system = ActorSystem("ConfigurationDemo", ConfigFactory.load(config))
  val actor = system.actorOf(Props[SimpleLoggingActor])

  actor ! "Inline configuration"

  /**
    * 2 - default file configuration
    */

  val defaultConfigFileSystem = ActorSystem("DefaultConfigurationFileDemo")
  val defaultConfigActor = defaultConfigFileSystem.actorOf(Props[SimpleLoggingActor])

  defaultConfigActor ! "default file configuration"

  /**
    * 3 - separate config in the same file
    */

  val specialConfig = ConfigFactory.load().getConfig("mySpecialConfig")
  val specialConfigSystem = ActorSystem("SpecialConfigDemo", specialConfig)
  val specialConfigActor = specialConfigSystem.actorOf(Props[SimpleLoggingActor])

  specialConfigActor ! "separate config in the same file"

  /**
    * 4 - separate config file
    */
  val separateConfig = ConfigFactory.load("secrets/secretConfiguration.conf")
  println(s"separate config log level: ${separateConfig.getString("akka.loglevel")}")


  /**
    * 5. Json format
    */
  val jsonFormat = ConfigFactory.load("someConfig.json")
  println(s"json config log level: ${jsonFormat.getString("akka.loglevel")}")
}
