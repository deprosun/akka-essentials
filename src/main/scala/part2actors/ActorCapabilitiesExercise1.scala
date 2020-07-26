package part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object ActorCapabilitiesExercise1 extends App {

  val actorSystem = ActorSystem("Exercise")

  val counter: ActorRef = actorSystem.actorOf(Props[Counter], "counter")

  val bankAccount: ActorRef = actorSystem.actorOf(BankAccount.fromCounter(counter), "bankAccount")

  val bob = actorSystem.actorOf(PersonA.fromBankAccount(bankAccount), "bob")

  //  bob ! Deposit(50)
  bob ! Deposit(50)
  //  bob ! WithDraw(50)
  //  bob ! WithDraw(50)
  bob ! WithDraw(50)
  bob ! "Print"
}

class PersonA(bankAccount: ActorRef) extends Actor {
  override def receive: Receive = {
    case d: Deposit => bankAccount ! d
    case w: WithDraw => bankAccount ! w
    case Failure => println("Insufficient funds to withdraw")
    case Success => println("Transaction Completed!")
    case "Print" => bankAccount ! "Print"
  }
}

object PersonA {
  def fromBankAccount(ref: ActorRef) = Props(new PersonA(ref))
}

class Counter extends Actor {
  var count = 0

  override def receive: Receive = {
    case Deposit(number) => count += number
    case WithDraw(number) if count - number < 0 => // cannot decrement because withdraw money greater than current balance
      sender() ! Failure
    case WithDraw(number) =>
      count -= number
      sender() ! Success
    case "Print" => println(s"Balance: USD [$count]")
  }
}

class BankAccount(counter: ActorRef) extends Actor {
  override def receive: Receive = {
    case Failure => println("Insufficient funds to withdraw")
    case d: Deposit => counter forward d
    case w: WithDraw => counter forward w
    case "Print" => counter forward "Print"
  }
}

object BankAccount {
  def fromCounter(ref: ActorRef) = Props(new BankAccount(ref))
}

trait Result

case object Success extends Result

case object Failure extends Result

case class Deposit(number: Int)

case class WithDraw(number: Int)

//case class IncrementBy(number: Int)
//
//case class DecrementBy(number: Int)