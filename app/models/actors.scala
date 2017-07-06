package models

import akka.actor._
import models.User._
import models.UserManager._

/**
  * Created by behzadfarokhi on 29/06/17.
  */

object UserGroup {
  def props(groupId: String): Props = Props(new UserGroup(groupId))
}

class UserGroup (groupId: String) extends Actor with ActorLogging {
  var userIdToActor = Map.empty[String, ActorRef]

  override def preStart(): Unit = log.info("UserGroup {} started!", groupId)

  override def postStop(): Unit = log.info("UserGroup {} stopped!", groupId)

  override def receive: Receive = ???
}

object UserManager {

  def props: Props = Props(new UserManager)

  final case class RequestTrackUser(groupId: String, userId: String)
  case object DeviceRegistered
}

class UserManager extends Actor with ActorLogging {

  var groupIdToActor = Map.empty[String, ActorRef]
  var actorToGroupId = Map.empty[ActorRef, String]

  override def preStart(): Unit = log.info("User Manager Started!")

  override def postStop(): Unit = log.info("User Manager Stopped!")

  override def receive: Receive = Actor.emptyBehavior

}

object User {

  def props(groupId: String, userId: String): Props = Props(new User(groupId, userId))

  final case class RecordData(requestId: Long, value: Double)
  final case class DataRecorded(requestId: Long)

  final case class ReadData(requestId: Long)
  final case class RespondData(requestId: Long, value: Option[Double])
}

class User(groupId: String, userId: String) extends Actor with ActorLogging {

  var lastDataReading: Option[Double] = None

  override def preStart(): Unit = log.info("User Actor {}-{} Started!", groupId, userId)

  override def postStop(): Unit = log.info("User Actor {}-{} Stopped!", groupId, userId)

  override def receive: Receive = {

    case RequestTrackUser(`groupId`, `userId`) =>
      sender() ! DeviceRegistered

    case RequestTrackUser(groupId, userId) =>
      log.warning(
        "Ignoring TrackUser request for {}-{}. This actor is responsible for {}-{}.",
        groupId, userId, this.groupId, this.userId
      )

    case RecordData (id, value) =>
      log.info("Recorded data reading {} with {}.", value, id)
      lastDataReading = Some(value)
      sender() ! DataRecorded(id)

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