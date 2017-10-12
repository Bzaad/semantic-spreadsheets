package actors

import akka.actor.ActorRef
import models.{PDStoreModel, PdChangeJson, PdQuery, PdObj, LTriple}
import play.api.Logger
import play.api.libs.json.Json
import scala.collection.mutable.Set

/**
  * Created by behzadfarokhi on 8/08/17.
  */

case class UserManager()

object UserManager {

  // Listeners
  var userMap: Map[String, ActorRef] = Map()
  var tripleSet: Map[String, Set[ActorRef]] = Map()
  var tripleSet2: Map[LTriple, Set[ActorRef]] = Map()
  var userSet: Map[ActorRef, Set[String]] = Map()

  /**
    * adds actor reference to the list of currently active users
    * when user first connect to server through websocket
    * (when websocket opens)
    * @param userName
    * @param theActor
    */
  def addUser(userName: String, theActor: ActorRef): Unit = {
    userMap += userName -> theActor
    userSet += theActor -> Set()
  }

  /**
    * removes actor reference from the list of currently active users
    * as well as all the listeners it has been registered to upon
    * disconnection from the server (when websocket closes)
    * @param userName
    * @param theActor
    */
  def removeUser(userName: String, theActor: ActorRef): Unit = {
    userMap -= userName
    removeFromListeners(theActor)
  }

  /**
    * remove the actor reference from active user list as well as
    * the list of all listeners the actor was registered to
    * @param theActor
    */
  def removeFromListeners(theActor: ActorRef): Unit ={
    if(userSet.contains(theActor)){
      for (a <- userSet(theActor)){
        tripleSet(a) -= theActor
      }
      userSet -= theActor
    }
  }

  /**
    * checks if the actor has been registered to a table listener before.
    * if yes remove the actor from that particular table
    * and adds it to the new table
    * the registration to all the triples on that particular table must alos be done here.
    * @param table: the table containing triples
    * @param tableTriples: the triples inside the table
    */
  def changeTableListener(table: PdObj, tableTriples: PdQuery): Unit = {
    for (t <- table.pdChangeList){
      /**
        * all tables have two "has_row" and "has_column" predicates by default
        * thus we need to register a listener for these too.
        * registration of triples is done inside PDStoreModel were a new listener is registered for
        * all the combinations even if the cell values are currently empty
        */
      PDStoreModel.registerListener(t)
      PDStoreModel.registerListener(new PdChangeJson("t", "e", t.sub, "has_row", "?"))
      PDStoreModel.registerListener(new PdChangeJson("t", "e", t.sub, "has_column", "?"))
    }
    /*
    for (tt <- tableTriples.reqValue){
      PDStoreModel.registerListener(tt)
    }
    */
  }

  /**
    * registering and removing listeners for single triples while user is working on a particular table
    * @param triples
    */
  def changeListener(triples: PdObj): Unit ={
    for (t <- triples.pdChangeList){
      PDStoreModel.registerListener(t)
    }
  }


  def addToListeners(lTriple: String, theActor: ActorRef, result: PdChangeJson): Unit ={
    if (tripleSet.contains(lTriple)){
      tripleSet(lTriple) += theActor
    }
    else{
      tripleSet += lTriple -> Set(theActor)
    }
  }

  /**
    * updates all the users that have accessed the triple pattern of change
    * this method is usually called by a registered listener that is listening
    * to changes
    * @param pdChangeJson
    */
  def updateListeningActors(pdChangeJson: PdChangeJson): Unit = {
    Logger.error(pdChangeJson.toString)
    if(tripleSet2.exists( t => t._1.lSub.toString == pdChangeJson.sub && t._1.lPred == pdChangeJson.pred)){
      val targetTriple = new LTriple(pdChangeJson.sub, pdChangeJson.pred, pdChangeJson.obj)
      val tListeners = tripleSet2.get(targetTriple).toList
      for(tl <- tripleSet2(targetTriple)){
        tl ! Json.toJson(PdQuery("listener", true, List[PdChangeJson](pdChangeJson)))
      }
    }
  }

  /**
    * send a message to all the currently active users that are listening to the pattern
    * except the sender user that has queried the triple
    * each user will recieve a call for each of the triples they are listening to.
    * @param receivers
    * @param msg
    */
  def sendToAll(sender: ActorRef, receivers: Set[ActorRef], msg: PdChangeJson): Unit = {
    val lmessage = new PdQuery(reqType = "lQuery", listenTo = false, reqValue = List[PdChangeJson](msg))
    for (r <- receivers ){
      if(r != sender) r ! Json.toJson(lmessage)
    }
  }

  // Queries
  def queryPdChange(p: PdObj): Unit = {
    val returnMessage = PDStoreModel.query(p)
    p.actor ! Json.toJson(returnMessage)
  }

  def applyPdChange(p: PdObj): Unit = {
    p.actor ! Json.toJson(PDStoreModel.applyPdc(p))
    changeListener(p)
  }

  def queryTable(p: PdObj): Unit = {
    val queryResult = PDStoreModel.queryTable(p)
    p.actor ! Json.toJson(queryResult)
    changeTableListener(p, queryResult)
  }

  /**
    * create the table and send the result back to the actor.
    * @param p
    */
  def createTable(p: PdObj): Unit = {
    val tables = PDStoreModel.createTable(p)
    p.actor ! Json.toJson(tables)
  }

  def queryAllTables(p: PdObj): Unit = {
    val pdQuery = new PdQuery("aTable", false, PDStoreModel.query(p).reqValue)
    p.actor ! Json.toJson(pdQuery)
  }
}
