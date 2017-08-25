package actors

import akka.actor._
import play.api.Logger
import play.api.libs.json._
import play.api.libs.json.Reads._
import models.{PdChange, PdQuery}

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
  }

  override def postStop(): Unit = {
    UserManager.removeUser(userName, theActor)
    Logger.debug(s"User actor $userName with actor reference $theActor has stopped!" )
  }

  override def receive: Receive = {
    case js: JsValue =>

      /**
        * Validating the message type from client
        */
      val reqResult : JsResult[PdQuery] = (js).validate[PdQuery]
      reqResult match {
        /**
          * if message type is "PdQuery"
          */
        case s: JsSuccess[PdQuery] => {

          val pdChangeSeq = (js \ "reqValue").as[Seq[PdChange]]

          ((js \ "reqType").as[String]) match {
            case "aTable" =>
              UserManager.queryAllTables(pdChangeSeq, theActor)
            case "cTable" =>
              UserManager.createTable(pdChangeSeq)
            case "qTable" =>
              UserManager.queryTable(pdChangeSeq)
            case "cChange" =>
              UserManager.applyPdChange(pdChangeSeq)
            case "qChange" =>
              UserManager.queryPdChange(pdChangeSeq)
          }
        };
        /**
          * if message type is not "PdQuery"
          */
        case e: JsError => Logger.error("type mismatch!");
      }
  }
}