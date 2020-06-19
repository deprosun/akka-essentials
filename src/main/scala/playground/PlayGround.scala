package playground

import akka.actor.ActorSystem

object PlayGround extends App {
  val actor = ActorSystem("HelloAkka")
  println(actor.name)
}
