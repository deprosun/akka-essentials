//package com.github.deprosun.demo
//
//import akka.actor.{Actor, ActorLogging, ActorRef}
//import Consumer.StartConsumer
//
//object Orchestrator {
//
//  case class TestProducer(producer: ActorRef)
//
//  case class OrchestrateAll(consumers: List[ActorRef], producers: List[ActorRef])
//
//  case class OrchestrateProducers(producers: List[ActorRef])
//
//}
//
//class Orchestrator extends Actor with ActorLogging {
//
//  import Orchestrator._
//  import Producer._
//
//  override def receive: Receive = {
//    case OrchestrateAll(consumerRefs, producerRefs) =>
//
//      //start all the consumers
//      consumerRefs foreach {
//        _ ! StartConsumer(heartBeatMs = 10000)
//      }
//
//      context.become(initialized(consumerRefs, producerRefs))
//
//
//    case OrchestrateProducers(refs) => context.become(producerInitialized(refs))
//    case TestProducer(ref) => ref ! Message("test", "someKey", "someValue")
//    case ProducerSendSuccessful(t, p, o) =>
//      log.info("The offset of the record for topic {} and partition {} we just sent is: {}", t, p, o)
//  }
//
//  def initialized(consumers: List[ActorRef], producers: List[ActorRef]): Receive = {
//    case m: Message => producers match {
//      case x :: xs => x ! m
//        context.become(initialized(consumers, xs :+ x))
//    }
//  }
//
//  def producerInitialized(producers: List[ActorRef]): Receive = {
//    case m: Message => producers match {
//      case x :: xs => x ! m
//        context.become(producerInitialized(xs :+ x))
//    }
//  }
//}
