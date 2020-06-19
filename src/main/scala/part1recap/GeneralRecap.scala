package part1recap

import scala.concurrent.Future

object GeneralRecap extends App {

  val partialFunction: PartialFunction[Int, Int] = {
    case 1 => 42
    case 2 => 65
    case 5 => 999
  }

  val pf = (x: Int) => x match {
    case 1 => 42
    case 2 => 65
    case 5 => 999
  }

  val function: Int => Int = partialFunction

  //lifting
  val lifted = partialFunction.lift
  lifted(2)
  lifted(5000)

  //orElse
  val pfChain = partialFunction.orElse[Int, Int] {
    case 60 => 9000
  }

  pfChain(5)
  pfChain(60)
  println(pfChain.lift(457))


  type ReceiveFunction = PartialFunction[Any, Unit]

  def receive: ReceiveFunction = {
    case 1 => println("Hello")
    case _ => println("confused....")
  }

  receive(1)

  //implicits
  implicit val timeout: Int = 5000

  def setTimeout(f: () => Unit)(implicit timeout: Int) = f()

  setTimeout(() => println("timeout"))

  //implicit conversions
  // 1) implicit defs
  case class Person(name: String) {
    def greet = s"Hi my name is $name"
  }

  implicit def fromStringToPerson(s: String): Person = Person(s)

  "Peter".greet

  // 2) implicit class
  implicit class Dog(name: String) {
    def bark = println(name)
  }

  "Lassie".bark

  //organize
  //local scope: the body of where its being called from
  implicit val reverseOrdering: Ordering[Int] = Ordering.fromLessThan(_ > _)
  println(List(1, 2, 3).sorted)

  //imported scope
  import scala.concurrent.ExecutionContext.Implicits.global

  val future = Future {
    println("hello, future")
  }

  //companion objects of the types included in the call
  object Person {
    implicit val personOrdering: Ordering[Person] = Ordering.fromLessThan((a, b) => a.name.compareTo(b.name) < 0)
  }

  println(List(Person("Alice"), Person("Bob")).sorted)


}
