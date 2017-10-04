package actors

import akka.actor.ActorRef
import models.{PDStoreModel, PdChangeJson, PdQuery, PdObj, LTriple}
import play.api.Logger
import play.api.libs.json.Json
import scala.collection.mutable.{Set, ListBuffer}

/**
  * Created by behzadfarokhi on 8/08/17.
  */

case class UserManager()

object UserManager {

  // Listeners
  var userMap: Map[String, ActorRef] = Map()
  var tripleSet: Map[String, Set[ActorRef]] = Map()
  var userSet: Map[ActorRef, Set[String]] = Map()
  var registeredListeners = new ListBuffer[PdChangeJson]()

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
    //check if we are listening to the table!
    for (t <- table.pdChangeList){
      if(listenerExists(t)) {}
      else {
        PDStoreModel.registerListener(t)
        registeredListeners += t
        /**
          * all tables have two "has_row" and "has_column" predicates by default
          * thus we need to register a listener for these too.
          */
        PDStoreModel.registerListener(new PdChangeJson("t", "e", t.sub, "has_row", "?"))
        PDStoreModel.registerListener(new PdChangeJson("t", "e", t.sub, "has_column", "?"))
      }
    }
    for (tt <- tableTriples.reqValue){
      if(listenerExists(tt)) {}
      else {
        PDStoreModel.registerListener(tt)
        registeredListeners += tt
      }
    }
  }

  /**
    * registering and removing listeners for single triples while user is working on a particular table
    * @param triples
    */
  def changeListener(triples: PdObj): Unit ={
    for (t <- triples.pdChangeList){
      if(listenerExists(t)){}
      else {
        PDStoreModel.registerListener(t)
        registeredListeners += t
      }
    }
  }

  def listenerExists(pdChangeJson: PdChangeJson): Boolean = {
    if (registeredListeners.contains(pdChangeJson)) true
    else false
  }

  def addToListeners(lTriple: String, theActor: ActorRef, result: PdChangeJson): Unit ={
    if (tripleSet.contains(lTriple)){
      tripleSet(lTriple) += theActor
    }
    else{
      tripleSet += lTriple -> Set(theActor)
    }
    updateListeningActors(theActor, lTriple, result)
  }

  /**
    * updates all the users that have accessed the triple pattern of change
    * this method is usually called by a registered listener that is listening
    * to changes
    * @param lTriple
    * @param msg
    */
  def updateListeningActors(sender: ActorRef, lTriple: String, msg: PdChangeJson): Unit = {
    if (tripleSet.contains(lTriple)) sendToAll(sender, tripleSet(lTriple), msg)
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
