package actors

import akka.actor.ActorRef
import models.PdJson
import play.api.Logger


import scala.collection.mutable.Set

/**
  * Created by behzadfarokhi on 8/08/17.
  */

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

  def registerListener(theActor: ActorRef, msg: PdJson): Unit ={

      userSet(theActor) += msg.pred

      if(!roleSet.contains(msg.pred))
        roleSet += msg.pred -> Set(theActor)
      else
        roleSet(msg.pred) += theActor

      updateListeningActors(msg.pred, msg)
  }

  def updateListeningActors(pred: String, msg: PdJson): Unit = {
    if (roleSet.contains(pred)) sendToAll(roleSet(pred), msg)
  }

  def sendToAll(receivers: Set[ActorRef], msg: PdJson): Unit = {
    for (r <- receivers ){
      Logger.debug(r.toString() + msg.toString())
    }
  }
}
