package part1recap

import scala.concurrent.Future
import scala.util.{Failure, Success}

object MultithreadingRecap extends App {

  //create threads on JVM

  val aThread = new Thread(() => println("I am running in parallel"))
  aThread.start()
  aThread.join()

  val threadHello = new Thread(() => (1 to 100) foreach (_ => println("hello")))
  val threadGoodbye = new Thread(() => (1 to 100) foreach (_ => println("goodbye")))

  threadHello.start()
  threadGoodbye.start()

  //different runs produce different results!

  class BankAccount(private var amount: Int) {
    override def toString: String = "" + amount

    def withDraw(money: Int) = this.amount -= money
  }

  /**
    * BankAccount(10000)
    *
    * T1 -> withDraw 1000
    * T2 -> withDraw 2000
    *
    * T1 -> this.amount = this.amount - .... //PREEMPTED by the os
    * T2 -> this.amount = this.amount - 2000 = 8000
    * T1 -> - 1000 = 9000
    *
    * => result = 9000
    *
    * this.amount = this.amount - 1000 is NOT ATOMIC (this means )
    */

  /**
    * OPTION 1: create a synchronised method
    * OPTION 2: add @volatile annotation to `amount` variable to do the same thing as Option 1 but only for primitive
    * data types
    */
  class BankAccount2(@volatile private var amount: Int) {
    override def toString: String = "" + amount

    def withDraw(money: Int): Unit = this.amount -= money

    def safeWithDraw(money: Int): Unit = this.synchronized {
      this.amount -= money
    }

  }

  //inter-thread communication on the JVM
  //wait - notify mechanism

  //scala Futures
  import scala.concurrent.ExecutionContext.Implicits.global

  val future = Future {
    42
  }

  //callbacks
  future.onComplete {
    case Success(42) => println("I have found the meaning of life")
    case Failure(_) => println("something happened with the meaning of life!")
  }

  val aProcessedFuture = future.map(_ + 1) //Future with 43
  val sFlatFuture = future.flatMap(value =>
    Future(value + 2)
  ) //Future with 44

  val filteredFuture = future.filter(_ % 2 == 0) //if not, NoSuchElementException

  //for comprehensions
  val aNonsenseFuture = for {
    meaningOfLife <- future
    filteredMeaning <- filteredFuture
  } yield meaningOfLife + filteredMeaning

  //andThen, recover/recoverWith

  //

}
