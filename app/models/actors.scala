package models

/**
  * Created by behzadfarokhi on 29/06/17.
  */

import scala.concurrent.duration._
import akka.actor.{Actor, ActorLogging, ActorRef, Props, Terminated}
import models.UserManager._


object UserGroup {

  def props(groupId: String): Props = Props(new UserGroup(groupId))

  final case class RequestUserList(requestId: Long)
  final case class ReplyUserList(requestId: Long, ids: Set[String])

  final case class RequestAllData(requestId: Long)
  final case class RespondAllData(requestId: Long, theData: Map[String, DataReading])

  // TODO: Change the value to JsValue
  sealed trait DataReading
  final case class TheData(value: Double) extends DataReading
  case object DataNotAvailable extends DataReading
  case object UserNotAvailable extends DataReading
  case object UserTimedOut extends DataReading
}

object UserGroupQuery {
  case object CollectionTimeout

  def props(
    actorToUserId: Map[ActorRef, String],
    requestId:     Long,
    requester:     ActorRef,
    timeout:       FiniteDuration
  ): Props = {
    Props(new UserGroupQuery(actorToUserId, requestId, requester, timeout))
  }
}

class UserGroupQuery(
  actorToUserId:  Map[ActorRef, String],
  requestId:      Long,
  requester:      ActorRef,
  timeout:        FiniteDuration
) extends Actor with ActorLogging {
  import UserGroupQuery._
  import context.dispatcher
  val queryTimeoutTimer = context.system.scheduler.scheduleOnce(timeout, self, CollectionTimeout)

  override def preStart(): Unit = {
    actorToUserId.keysIterator.foreach { userActor =>
      context.watch(userActor)
      userActor ! User.ReadData(0)
    }
  }

  override def postStop(): Unit = {
    queryTimeoutTimer.cancel()
  }

  override def receive: Receive = waitingForReplies(Map.empty, actorToUserId.keySet)

  def waitingForReplies(repliesSoFar: Map[String, UserGroup.DataReading], stillWaiting: Set[ActorRef]): Receive = {

    case User.RespondData(0, valueOption) =>
      val userActor = sender()
      val reading = valueOption match {
        case Some(value) => UserGroup.TheData(value)
        case None => UserGroup.DataNotAvailable
      }

      receivedResponse(userActor, reading, stillWaiting, repliesSoFar)

    case Terminated(userActor) =>
      receivedResponse(userActor, UserGroup.UserNotAvailable, stillWaiting, repliesSoFar)

    case CollectionTimeout =>
      val timedOutReplies = stillWaiting.map { userActor =>
        val userId = actorToUserId(userActor)
        userId -> UserGroup.UserTimedOut
      }
    requester ! UserGroup.RespondAllData(requestId, repliesSoFar ++ timedOutReplies)
      context.stop(self)
  }

  def receivedResponse(
    userActor:    ActorRef,
    reading:      UserGroup.DataReading,
    stillWaiting: Set[ActorRef],
    repliesSoFar: Map[String, UserGroup.DataReading]
  ): Unit = {
    context.unwatch(userActor)
    val userId = actorToUserId(userActor)
    val newStillWaiting = stillWaiting - userActor

    val newRepliesSoFar = repliesSoFar + (userId -> reading)
    if (newStillWaiting.isEmpty) {
      requester ! UserGroup.RespondAllData(requestId, newRepliesSoFar)
      context.stop(self)
    } else {
      context.become(waitingForReplies(newRepliesSoFar, newStillWaiting))
    }
  }
}

class UserGroup(groupId: String) extends Actor with ActorLogging {
  import UserGroup._

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

    case RequestAllData(requestId) =>
      context.actorOf(UserGroupQuery.props(
        actorToUserId = actorToUserId,
        requestId = requestId,
        requester = sender(),
        3.seconds
      ))
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
  import User._

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