//package com.github.deprosun.demo
//
//import java.time.Duration
//import java.util
//
//import akka.actor.{Actor, ActorLogging}
//import org.apache.kafka.clients.consumer.{ConsumerRecord, ConsumerRecords, KafkaConsumer}
//import org.apache.kafka.common.TopicPartition
//
//import scala.collection.JavaConversions._
//
//object Consumer {
//
//  case object ConsumerInitializedResponse
//
//  case object ConsumerCloseRequest
//
//  case object ConsumerClosedResponse
//
//  case class StartConsumer(heartBeatMs: Long)
//
//  case class ConsumerUninitialized(startConsumer: StartConsumer)
//
//  case class ConsumedData(records: ConsumerRecords[String, String])
//
//}
//
//class Consumer extends Actor with ActorLogging {
//
//  import Consumer._
//
//  override def receive: Receive = {
//    case Initialize(prop) =>
//      val consumer = new KafkaConsumer[String, String](prop)
//      context.become(initializeClient(consumer))
//      sender() ! ConsumerInitializedResponse
//    case s: StartConsumer =>
//      log.warning("Cannot start consumer on uninitialized stage.")
//      sender() ! ConsumerUninitialized(s)
//  }
//
//  def initializeClient(client: KafkaConsumer[String, String]): Receive = {
//    case StartConsumer(heartBeatMs) =>
//
//      val currentTime = System.currentTimeMillis()
//
//      def consume(): Unit = {
//
//        val records: ConsumerRecords[String, String] = client.poll(Duration.ofMillis(10))
//
//        if (!records.isEmpty) sender() ! ConsumedData(records)
//
//        if (System.currentTimeMillis() - currentTime >= heartBeatMs)
//          self ! StartConsumer(heartBeatMs)
//        else
//          consume()
//      }
//
//      //start consumer
//      consume()
//    case Close =>
//      client.close()
//      context.become(receive)
//    case ConsumerCloseRequest =>
//      self ! Close
//      sender() ! ConsumerClosedResponse
//
//
//  }
//}
