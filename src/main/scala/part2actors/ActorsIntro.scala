package part2actors

import akka.actor.{Actor, ActorSystem, Props}

object ActorsIntro extends App {

  //part1 - actor system
  val actorSystem = ActorSystem("firstActorSystem")

  println(actorSystem.name)

  //part2 - create actors
  // word count actor
  class WordCountActor extends Actor {

    var totalWords = 0

    override def receive: Receive = {
      case message: String =>
        println(s"[word counter] I have received: ${message.toString}")
        totalWords += message.split(" ").length
      case x => println(s"[word counter] I cannot understand ${x.toString}")
    }
  }

  //part3 - instantiate our actor
  val wordCounter = actorSystem.actorOf(Props[WordCountActor], "wordCounter")
  val anotherWordCounter = actorSystem.actorOf(Props[WordCountActor], "anotherWordCounter")


  //part 4 - communicate!
  wordCounter ! "I am learning Akka and it's pretty damn cool"
  anotherWordCounter ! "A different message"
  //asynchronous

  //actor creation with classes containing constructor parameters
  val person = Person.props("Bob")
  val personActor = actorSystem.actorOf(person)
  personActor ! "hi"
}

class Person(name: String) extends Actor {
  override def receive: Receive = {
    case "hi" => println(s"Hi, my name is $name")
    case _ =>
  }
}


object Person {
  def props(name: String): Props = {
    Props(new Person(name))
  }
}

