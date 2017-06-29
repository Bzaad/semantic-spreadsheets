package models

import akka.actor._
import models.User._

/**
  * Created by behzadfarokhi on 29/06/17.
  */
object User {

  def props(groupId: String, userId: String): Props = Props(new User(groupId, userId))

  final case class ReadData(requestId: Long)
  final case class RespondData(requestId: Long, value: Option[Double])
}

class User(groupId: String, userId: String) extends Actor with ActorLogging {

  val lastDataReading: Option[Double] = None

  override def preStart(): Unit = log.info("User Actor {}-{} Started!", groupId, userId)

  override def postStop(): Unit = log.info("User Actor {}-{} Stoped!", groupId, userId)

  override def receive: Receive = {
    case ReadData(id) =>
      sender() ! RespondData(id, lastDataReading)
  }
}

object SpreadsheetSupervisor {
  def props(): Props = Props(new SpreadsheetSupervisor)
}

class SpreadsheetSupervisor extends Actor with ActorLogging {

  override def preStart(): Unit = log.info("Spreadsheet Server Started!")

  override def postStop(): Unit = log.info("Spreadsheet Server Stopped!")

  override def receive: Receive = Actor.emptyBehavior

}