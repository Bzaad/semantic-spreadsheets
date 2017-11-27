package actors

import akka.actor._
import play.api.Logger
import play.api.libs.json._
import play.api.libs.json.Reads._
import models.{PDStoreModel, PdChangeJson, PdObj, PdQuery}

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
    PDStoreModel.actorsAndTheirTriples.remove(theActor)
    Logger.debug(s"User actor $userName with actor reference $theActor has stopped!" )
  }

  def listenToPattern(c: PdObj): Unit = {
    //PDStoreModel.registeredListeners
  }

  def userTriples(pdObj: PdObj): Unit = {
    /*
    remove the actor from the list and re add it again
     */
    if(PDStoreModel.actorsAndTheirTriples.keySet.exists(_ == pdObj.actor))
      PDStoreModel.actorsAndTheirTriples.remove(pdObj.actor)
    PDStoreModel.actorsAndTheirTriples += (pdObj.actor -> pdObj.pdChangeList)
    /*
    for (p <- pdObj.pdChangeList){
      if
    }
    */
    val requestedTableName = pdObj.pdChangeList.filter(s => "has_type".equals(s.pred) && "table".equals(s.obj))
    for (u <- PDStoreModel.actorsAndTheirTriples){
      if (!u._1.equals(pdObj.actor) && u._2.exists( p => requestedTableName(0).sub.equals(p.sub))){
        Logger.debug("these are accessing same tables!")
      }
    }

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

          val cBundle = new PdObj((js \ "reqValue").as[List[PdChangeJson]], theActor, (js \ "listenTo").as[Boolean])
          ((js \ "reqType").as[String]) match {
            case "aTable" =>
              UserManager.queryAllTables(cBundle)
            case "cTable" =>
              UserManager.createTable(cBundle)
            case "qTable" =>
              UserManager.queryTable(cBundle)
            case "cChange" =>
              UserManager.applyPdChange(cBundle)
            case "qChange" =>
              UserManager.queryPdChange(cBundle)
            case "listen" =>
              listenToPattern(cBundle)
            case "tableTriples" =>
              userTriples(cBundle)
          }
        }
        /**
          * if message type is not "PdQuery"
          */
        case e: JsError => Logger.error("type mismatch!");
      }
  }
}