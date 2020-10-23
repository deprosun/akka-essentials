//package com.github.deprosun.demo
//
//import akka.actor.{Actor, ActorLogging}
//import org.apache.kafka.clients.producer.{Callback, KafkaProducer, ProducerRecord, RecordMetadata}
//
//object Producer {
//
//  case object ProducerCloseRequest
//
//  case object ProducerClosedResponse
//
//  case object ProducerInitializedResponse
//
//  case class ProducerUninitialized(message: Message)
//
//  case class ProducerSendSuccessful(topic: String, partition: Int, offset: Long)
//
//}
//
//class Producer extends Actor with ActorLogging {
//
//  import Producer._
//
//  override def receive: Receive = {
//    case Initialize(prop) =>
//      val producer = new KafkaProducer[String, String](prop)
//      context.become(initializeClient(producer))
//      sender() ! ProducerInitializedResponse
//    case m: Message =>
//      log.error("Cannot produce. Producer is in uninitialized stage.")
//      sender() ! ProducerUninitialized(m)
//  }
//
//  def onCompletion(topic: String): Callback = new Callback {
//    override def onCompletion(metadata: RecordMetadata, e: Exception): Unit = Option(e) match {
//      case None =>
//        log.info("The offset of the record for topic {} we just sent is: {}", topic, metadata.offset)
//        sender() ! ProducerSendSuccessful(metadata.topic(), metadata.partition(), metadata.offset())
//      case Some(ex) => log.error(ex, ex.getMessage)
//    }
//  }
//
//  def initializeClient(client: KafkaProducer[String, String]): Receive = {
//    case Message(topic, messages) =>
//      val record = new ProducerRecord[String, String](topic, k, v)
//
//      messages foreach {
//      }
//
//
//      client.send(record,)
//    case Close =>
//      client.close()
//      context.become(receive)
//    case ProducerCloseRequest =>
//      self ! Close
//      sender() ! ProducerClosedResponse
//  }
//
//}