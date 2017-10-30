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

  var tripleSet: Map[String, Set[ActorRef]] = Map()
  var tripleSet2: Map[LTriple, Set[ActorRef]] = Map()

  /**
    * adds actor reference to the list of currently active users
    * when user first connect to server through websocket
    * (when websocket opens)
    * @param userName
    * @param theActor
    */
  def addUser(userName: String, theActor: ActorRef): Unit = {
    if (!PDStoreModel.listeningActors.contains(theActor)){
      PDStoreModel.listeningActors += theActor -> Set()
    }
  }

  /**
    * removes actor reference from the list of currently active users
    * as well as all the listeners it has been registered to upon
    * disconnection from the server (when websocket closes)
    * @param userName
    * @param theActor
    */
  def removeUser(userName: String, theActor: ActorRef): Unit = {
    Logger.error("removing the actor!")
    PDStoreModel.listeningActors -= theActor
  }

  /**
    * remove the actor reference from active user list as well as
    * the list of all listeners the actor was registered to
    * @param theActor
    */
  def removeFromListeners(theActor: ActorRef): Unit ={
    /*
    if(PDStoreModel.userSet.contains(theActor)){
      for (a <- PDStoreModel.userSet(theActor)){
        tripleSet(a) -= theActor
      }
      PDStoreModel.userSet -= theActor
    }
    */
  }

  /**
    * checks if the actor has been registered to a table listener before.
    * if yes remove the actor from that particular table
    * and adds it to the new table
    * the registration to all the triples on that particular table must alos be done here.
    * @param pdObjList: set of all triples containing the table and all its triples=
    * @param actor: the registering actor
    */
  def changeTableListener(pdObjList: List[PdChangeJson], actor: ActorRef): Unit = {

    PDStoreModel.listeningActors -= actor

    for (t <- pdObjList){
      /**
        * all tables have two "has_row" and "has_column" predicates by default
        * thus we need to register a listener for these too.
        * registration of triples is done inside PDStoreModel were a new listener is registered for
        * all the combinations even if the cell values are currently empty
        */
      // TODO: the data structure must change to conform with normal pdchange object rather than
      // creating special cases
      if("is_row".equals(t.sub) || "is_column".equals(t.sub)){
        PDStoreModel.registerListener(new PdChangeJson("t", "e", t.pred, "has_value" , "?"), actor)
      } else if ("has_type".equals(t.pred) && "table".equals(t.obj)){
        // TODO: listen to the table name and the table so if somebody changes table or delets the table
        // so you can be notified
        PDStoreModel.registerListener(new PdChangeJson("t", "e", t.sub, "has_row", "?"), actor)
        PDStoreModel.registerListener(new PdChangeJson("t", "e", t.sub, "has_column", "?"), actor)
        //PDStoreModel.registerListener(t, pdObj.actor)
      } else {
        PDStoreModel.registerListener(t, actor)
      }
    }
  }

  /**
    * registering and removing listeners for single triples while user is working on a particular table
    * @param pdObj
    */

  def changeListener(pdObj: PdObj): Unit ={
    for (t <- pdObj.pdChangeList){
      PDStoreModel.registerListener(t, pdObj.actor)
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
  def updateListeningActors(pdChangeJson: PdChangeJson, actors: Set[ActorRef]): Unit = {
    /*
    Logger.error(pdChangeJson.toString)
    Logger.error(actors.toString)
    */
    for(a <- actors){
      a ! Json.toJson(PdQuery("listener", true, List[PdChangeJson](pdChangeJson)))
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
    val pdChangeListen = queryResult.reqValue ++ p.pdChangeList
    changeTableListener(pdChangeListen, p.actor)
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
