package models

import akka.actor.{Actor, ActorLogging, ActorRef, Props, Terminated}
import models.User._
import models.UserGroup._
import models.UserManager._

/**
  * Created by behzadfarokhi on 29/06/17.
  */

object UserGroup {

  def props(groupId: String): Props = Props(new UserGroup(groupId))

  final case class RequestUserList(requestId: Long)
  final case class ReplyUserList(requestId: Long, ids: Set[String])
}

class UserGroup(groupId: String) extends Actor with ActorLogging {

  var userIdToActor = Map.empty[String, ActorRef]
  var actorToUserId = Map.empty[ActorRef, String]

  override def preStart(): Unit = log.info("UserGroup {} started!", groupId)

  override def postStop(): Unit = log.info("UserGroup {} stopped!", groupId)

  override def receive: Receive = {

    case trackMsg @ RequestTrackUser(`groupId`, _) =>
      userIdToActor.get(trackMsg.userId) match {
        case Some(userActor) =>
          userActor forward trackMsg
        case None =>
          log.info("Creating user actor for {}!", trackMsg.userId)
          val userActor = context.actorOf(User.props(groupId, trackMsg.userId), s"device-${trackMsg.userId}")
          context.watch(userActor)
          actorToUserId += userActor -> trackMsg.userId
          userIdToActor += trackMsg.userId -> userActor
          userActor forward trackMsg
      }

    case RequestTrackUser(groupId, userId) =>
      log.warning(
        "Ignoring TrackUser request for {}. This actor is responsible for {}.",
        groupId, this.groupId
      )

    case RequestUserList(requestId) =>
      sender() ! ReplyUserList(requestId, userIdToActor.keySet)

    case Terminated(userActor) =>
      val userId = actorToUserId(userActor)
      log.info("Device actor for {} has been terminated!", userId)
      actorToUserId -= userActor
      userIdToActor -= userId
  }
}
object UserManager {

  def props: Props = Props(new UserManager)

  final case class RequestTrackUser(groupId: String, userId: String)
  case object UserRegistered
}

class UserManager extends Actor with ActorLogging {

  var groupIdToActor = Map.empty[String, ActorRef]
  var actorToGroupId = Map.empty[ActorRef, String]

  override def preStart(): Unit = log.info("User Manager Started!")

  override def postStop(): Unit = log.info("User Manager Stopped!")

  override def receive: Receive = {

    case trackMsg @ RequestTrackUser(groupId, _) =>
      groupIdToActor.get(groupId) match {
        case Some(ref) =>
          ref forward trackMsg
        case None =>
          log.info("Creating user Group actor for {}!", groupId)
          val groupActor = context.actorOf(UserGroup.props(groupId), "group-" + groupId)
          context.watch(groupActor)
          groupActor forward trackMsg
          groupIdToActor += groupId -> groupActor
          actorToGroupId += groupActor -> groupId
      }

    case Terminated(groupActor) =>
      val groupId = actorToGroupId(groupActor)
      log.info("User group actor for {} has been terminated!", groupId)
      actorToGroupId -= groupActor
      groupIdToActor -= groupId
  }

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
      sender() ! UserRegistered

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