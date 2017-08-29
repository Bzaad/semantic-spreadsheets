package actors

import akka.actor.ActorRef
import models.{PDStoreModel, PdChangeJson, PdQuery}
import play.api.Logger
import play.api.libs.json.Json
import scala.collection.mutable.Set
import pdstore.notify.PDListenerAdapter
import pdstore.log.PDCoreI

/**
  * Created by behzadfarokhi on 8/08/17.
  */
case class UserManager()

object UserManager {

  var userMap: Map[String, ActorRef] = Map()
  var roleSet: Map[String, Set[ActorRef]] = Map()
  var userSet: Map[ActorRef, Set[String]] = Map()

  def queryPdChange(pdChangeSeq: Seq[PdChangeJson]): Unit = {
    Logger.debug("query pdchange")
    Logger.debug(pdChangeSeq.toString())
  }
  def applyPdChange(pdChangeSeq: Seq[PdChangeJson]): Unit = {
    Logger.debug("apply pdChange")
    Logger.debug(pdChangeSeq.toString())
  }
  def queryTable(pdChangeSeq: Seq[PdChangeJson]): Unit = {
    Logger.debug("query table")
    //PDStoreModel.addChanges(pdChangeSeq)
  }

  def queryAllTables(pdChangeSeq: Seq[PdChangeJson], theActor: ActorRef): Unit = {
    Logger.debug("getting all the tables!")
    val pdQuery = new PdQuery("aTable", PDStoreModel.getAllTables(pdChangeSeq))
    theActor ! Json.toJson(pdQuery)
  }

  def createTable(pdChangeSeq: Seq[PdChangeJson]): Unit = {
    Logger.debug("create table")
    PDStoreModel.addChanges(pdChangeSeq)
  }

  def addUser(userName: String, theActor: ActorRef): Unit = {
    userMap += userName -> theActor
    userSet += theActor -> Set()
  }

  def removeUser(userName: String, theActor: ActorRef): Unit = {
    userMap -= userName
    removeFromListeners(theActor)
  }

  def removeFromListeners(theActor: ActorRef): Unit ={
    if(userSet.contains(theActor)){
      for (a <- userSet(theActor)){
        roleSet(a) -= theActor
      }
      userSet -= theActor
    }
  }

  def registerListener(theActor: ActorRef, msg: PdQuery): Unit ={
      Logger.debug(msg.toString)
      /*
      userSet(theActor) += msg.pred

      if(!roleSet.contains(msg.pred))
        roleSet += msg.pred -> Set(theActor)
      else
        roleSet(msg.pred) += theActor

      updateListeningActors(msg.pred, msg)
      */
  }

  def updateListeningActors(pred: String, msg: PdChangeJson): Unit = {
    if (roleSet.contains(pred)) sendToAll(roleSet(pred), msg)
  }

  def sendToAll(receivers: Set[ActorRef], msg: PdChangeJson): Unit = {
    for (r <- receivers ){
      Logger.debug(r.toString() + msg.toString())
    }
  }
}
