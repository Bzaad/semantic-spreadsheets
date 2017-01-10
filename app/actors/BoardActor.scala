package actors

/**
  * Created by behzadfarokhi on 10/01/17.
  */

import akka.actor.{Actor, ActorLogging, ActorRef, Props, Terminated}
import akka.event.LoggingReceive
import play.libs.Akka

/*
  BoardActor sends back all the messages to all instances of UserActor and send down the websocket to the clients.
*/

class BoardActor extends Actor with ActorLogging {
  var users = Set[ActorRef]()

    def receive = LoggingReceive {
      case m: Message =>
        users foreach { _ ! m }
      case Subscribe =>
        users += sender
        context watch sender
      case Terminated(user) =>
        users -= user
  }
}

object BoardActor {
  lazy val board = Akka.system().actorOf(Props[BoardActor])
  def apply() = board
}

case class Message(uuid: String, s: String)
object Subscribe

