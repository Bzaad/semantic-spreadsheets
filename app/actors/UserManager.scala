package actors

import akka.actor.ActorRef
import models.{PDStoreModel, PdChangeJson, PdQuery, PdObj}
import play.api.Logger
import play.api.libs.json.Json
import scala.collection.mutable.Set

/**
  * Created by behzadfarokhi on 8/08/17.
  */

case class UserManager()

object UserManager {

  var userMap: Map[String, ActorRef] = Map()
  var roleSet: Map[String, Set[ActorRef]] = Map()
  var userSet: Map[ActorRef, Set[String]] = Map()

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

  def updateListeningActors(pred: String, msg: PdChangeJson): Unit = {
    if (roleSet.contains(pred)) sendToAll(roleSet(pred), msg)
  }

  def sendToAll(receivers: Set[ActorRef], msg: PdChangeJson): Unit = {
    for (r <- receivers ){
      Logger.debug(r.toString() + msg.toString())
    }
  }

  def queryPdChange(p: PdObj): Unit = {
    PDStoreModel.query
    Logger.debug(p.pdChangeSeq.toString())
  }

  def applyPdChange(p: PdObj): Unit = {
    Logger.debug("apply pdChange")
    Logger.debug(p.pdChangeSeq.toString())
  }

  def queryTable(p: PdObj): Unit = {
    Logger.debug("query table")
  }

  def createTable(p: PdObj): Unit = {
    Logger.debug("create table")
    PDStoreModel.addChanges(p.pdChangeSeq)
  }

  def queryAllTables(p: PdObj): Unit = {
    Logger.debug("getting all the tables!")
    val pdQuery = new PdQuery("aTable", false, PDStoreModel.getAllTables(p.pdChangeSeq))
    p.actor ! Json.toJson(pdQuery)
  }
}
