package actors

import akka.actor.ActorRef
import models.PdJson
import play.api.libs.json.Json

import scala.collection.mutable.Set

/**
  * Created by behzadfarokhi on 8/08/17.
  */

object UserManager {

  val userSet = Set[ActorRef]()

  def addUser(actor: ActorRef): Unit = {
    UserManager.userSet += actor
    println("this is the set: " + UserManager.userSet)
  }

  def removeUser(actor: ActorRef): Unit = {
    UserManager.userSet -= actor
  }

  def sendToAll(): Unit ={
    for (user <- UserManager.userSet){
      user ! Json.toJson(PdJson("time", "change", "subject", "predicate", "object"))
    }
  }

}
