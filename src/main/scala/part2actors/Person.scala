package part2actors

import akka.actor.{Actor, Props}

object Person {
  def props(name: String): Props = {
    Props(new Person(name))
  }
}

class Person(name: String) extends Actor {
  override def receive: Receive = {
    case "hi" => println(s"Hi, my name is $name")
    case _ =>
  }
}
