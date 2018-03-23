package actors

import akka.actor.ActorRef
import models._
import play.api.Logger
import play.api.libs.json.Json

import scala.collection.mutable.{Set, ListBuffer}

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
    /*
    if (!PDStoreModel.listeningActors.contains(theActor)){
      PDStoreModel.listeningActors += theActor -> Set()
    }
    */
  }

  /**
    * removes actor reference from the list of currently active users
    * as well as all the listeners it has been registered to upon
    * disconnection from the server (when websocket closes)
    * @param userName
    * @param theActor
    */
  def removeUser(userName: String, theActor: ActorRef): Unit = {
    /*
    Logger.error("removing the actor!")
    PDStoreModel.listeningActors -= theActor
    */
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

  def queryCsv(p: PdObj): Unit = {
    val returnMessage = new PdQuery("qCsv", false, PDStoreModel.query(p).reqValue)
    p.actor ! Json.toJson(returnMessage)
  }

  def applyPdChange(p: PdObj): Unit = {
    p.actor ! Json.toJson(PDStoreModel.applyPdc(p))
  }

  def queryTable(p: PdObj): Unit = {
    val queryResult = PDStoreModel.queryTable(p)
    p.actor ! Json.toJson(queryResult)
    val pdChangeListen = queryResult.reqValue ++ p.pdChangeList
  }
  def exportCsv(pdObj: PdObj): Unit = {
    var csvTableList = ListBuffer[CsvTable]()
    for (p <- pdObj.pdChangeList){
       csvTableList += CsvTable(p.sub, PDStoreModel.queryTable(new PdObj(List(p), pdObj.actor, pdObj.listenTo)).reqValue)
    }
    pdObj.actor ! Json.toJson(CsvPdQuery("exportCsv", false, csvTableList.toList))
  }
  /**
    * create the table and send the result back to the actor.
    * @param p
    */
  def createTable(p: PdObj): Unit = {
    val tables = PDStoreModel.createTable(p)
    p.actor ! Json.toJson(tables)
  }

  def removeTable(p: PdObj): Unit ={
    val message = PDStoreModel.removeTable(p)
    p.actor ! Json.toJson(message)
  }

  def queryAllTables(p: PdObj): Unit = {
    val pdQuery = new PdQuery("aTable", false, PDStoreModel.query(p).reqValue)
    p.actor ! Json.toJson(pdQuery)
  }
}
