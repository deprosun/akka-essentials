//package com.github.deprosun.demo
//
//import akka.actor.{Actor, ActorLogging}
//import com.github.deprosun.dataflattener
//import com.github.deprosun.dataflattener.Table
//import com.github.deprosun.dataflattener.model.MapperContext
//import org.apache.kafka.clients.consumer.{ConsumerRecord, ConsumerRecords, OffsetAndMetadata}
//import org.apache.kafka.common.TopicPartition
//import org.json4s.JValue
//import org.json4s.JsonAST.JString
//import org.slf4j.{Logger, LoggerFactory}
//import org.json4s.native.JsonMethods._
//import scala.collection.JavaConversions._
//
//import scala.util.Random
//
//object Transformer {
//
//  case class Transform(records: ConsumerRecords[String, String], mapperContext: MapperContext)
//
//  case class Transformed(tables: List[Table], topicPartition: TopicPartition, offset: OffsetAndMetadata)
//
//}
//
//trait DataTransformer extends dataflattener.Transformer {
//
//  import Transformer._
//
//  def superSafeEncryptionApi(s: String): String = {
//    s.reverse.foldLeft("") { (acc, x) =>
//      acc + Random.nextPrintableChar() + x
//    }
//  }
//
//  private def encrypt(json: List[JValue]): JValue = json match {
//    case JString(s) :: Nil => JString(superSafeEncryptionApi(s))
//    case x =>
//      throw new UnknownError(s"Should not get here $x")
//  }
//
//  private def extractJson(json: JValue): Option[Any] = Option(json)
//
//  override val logger: Logger = LoggerFactory.getLogger("DataTransformer")
//
//  //for defining your own transformation logic
//  override val udfMap: Map[String, this.MapFunc] = Map(
//    "encrypt" -> encrypt
//  )
//
//  //for defining your own data types.  for example, if you wanted to store JSON string as data type
//  override val customDataType: Map[String, JValue => Option[Any]] = Map(
//    "JSON" -> extractJson
//  )
//
//  //for potentially retrieving/storing secrets from the vault during transformation
//  override val vaultAddress: String = ""
//  override val vaultToken: String = ""
//
//  def start(iterator: Iterator[ConsumerRecord[String, String]], mapper: MapperContext): List[Transformed] = {
//
//    def collect(collected: List[Transformed]): List[Transformed] = {
//      if (iterator.hasNext) {
//        val record = iterator.next()
//        val json = parse(record.value())
//        val offset = new OffsetAndMetadata(record.offset())
//        val topicPartition = new TopicPartition(record.topic(), record.partition())
//        val tables = transform(json, mapper)
//        collect(collected :+ Transformed(tables, topicPartition, offset))
//      } else collected
//    }
//
//    collect(Nil)
//  }
//}
//
//
//class Transformer extends Actor with ActorLogging with DataTransformer {
//
//  import Transformer._
//
//  override def receive: Receive = {
//    case Transform(records, mapper) =>
//
//      val transformed: List[Transformed] = start(records.iterator(), mapper)
//
//      //simply call transform
//      sender() ! transformed
//  }
//}
//
