package actors

import akka.actor._
import play.api.Logger
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import models.PdJson

/**
  * Created by behzadfarokhi on 20/07/17.
  */

object User {
  def props(user: String)(theActor: ActorRef) = Props(new User(user, theActor))
}

class User(userName: String, theActor: ActorRef) extends Actor with ActorLogging {

  override def preStart(): Unit = {
    UserManager.addUser(userName, theActor)
    Logger.debug(s"User actor $userName with actor reference $theActor has started!")
    // TODO: add the actor to list of all available actors
  }

  override def postStop(): Unit = {

    UserManager.removeUser(userName, theActor)
    Logger.debug(s"User actor $userName with actor reference $theActor has stopped!" )
  }

  override def receive: Receive = {
    case msg: JsValue =>
      msg.validate[PdJson] match {
        case s: JsSuccess[PdJson] => {
          val pdJson: PdJson = s.get
          UserManager.registerListener(theActor, pdJson)
        }
        case e: JsError => {
          Logger.debug("msg does not conform to PdJson object structure!")
        }
      }
  }
}