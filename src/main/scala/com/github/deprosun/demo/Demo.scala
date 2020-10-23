//package com.github.deprosun.demo
//
//import java.util.{Properties, UUID}
//
//import akka.actor.{ActorSystem, Props}
////import com.github.deprosun.sentimentanalysis.tutorial.Demo.Producer._
//import org.apache.kafka.clients.producer.ProducerConfig
//import org.apache.kafka.common.serialization.StringSerializer
//
//import scala.language.postfixOps
//
//object Demo extends App {
//
//  import Orchestrator._
//
//  val system = ActorSystem("supervisor")
//
//  val orchestrator = system.actorOf(Props[Orchestrator])
//
//  val props = new Properties()
//
//  props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
//  props.put(ProducerConfig.CLIENT_ID_CONFIG, "TestId")
//  props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer].getName)
//  props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer].getName)
//
//  val producers = 0 until 5 map { i =>
//    system.actorOf(Props[Producer], "Producer" + i.toString)
//  } toList
//
//  // initialize producers
//  producers foreach { producer => producer ! Initialize(props) }
//
//  //initialize the orchestrator
//  orchestrator ! OrchestrateProducers(producers)
//
//  //test
//  0 to 100 foreach { _ =>
//    orchestrator ! Message("test", KafkaMessage(UUID.randomUUID().toString, UUID.randomUUID().toString) :: Nil)
//  }
//
//}
//
//
//case class Initialize(producerProp: Properties)
//
//case class Message(topic: String, message: List[KafkaMessage])
//
//case class KafkaMessage(key: String, message: String)
//
//case object Close