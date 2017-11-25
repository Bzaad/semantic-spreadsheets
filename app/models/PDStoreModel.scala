package models

import pdstore._
import PDStore._
import akka.actor.{ActorRef, ActorSystem }
import scala.concurrent.duration._
import play.api.Logger
import scala.collection.mutable
import scala.collection.mutable.{ArrayBuffer, ListBuffer, Set}

case class PDStoreModel()

object PDStoreModel {

  val store = PDStore("pdstore_dd")
  val system = ActorSystem("aSystem")
  import system.dispatcher

  var registeredListeners = new ListBuffer[LTriple]()

  var listeningActors: Map[ActorRef, Set[LTriple]] = Map()

  def query(pdObj: PdObj): PdQuery = {
    var queryResult = ArrayBuffer.empty[PdChangeJson]

    for (p <- pdObj.pdChangeList) {
      if (p.sub == "?" && p.obj != "?") {
        val qResult = store.query((v"x", store.getGUIDwithName(p.pred), store.getGUIDwithName(p.obj)))

        while (qResult.hasNext) {
          val t = store.begin
          val queriedSub = qResult.next().get(v"x")
          val result = PdChangeJson("ts", "e", store.getName(queriedSub), p.pred, p.obj)
          queryResult += result
          store.commit
        }
      } else if (p.sub != "?" && p.obj == "?") {
        val qResult = store.query((store.getGUIDwithName(p.sub), store.getGUIDwithName(p.pred), v"x"))
        while (qResult.hasNext) {
          val t = store.begin
          val queriedObj = qResult.next().get(v"x").toString
          val result = new PdChangeJson("ts", "e", p.sub, p.pred, queriedObj)
          val lTriple = "?_" + p.pred + "_" + queriedObj
          actors.UserManager.addToListeners(lTriple, pdObj.actor, result)

          queryResult += result
        }
      } else {
        Logger.error("not a valid query")
      }
    }

    if (queryResult.nonEmpty) {
      PdQuery("success", false, queryResult.toList)
    } else {
      PdQuery("success", false, List[PdChangeJson]())
    }
  }

  def createTable(p: PdObj): PdQuery = {
    for (t <- p.pdChangeList) {
      if (t.pred == "has_type" && t.obj == "table") {
        store.addLink(store.getGUIDwithName(t.sub), store.getGUIDwithName(t.pred), store.getGUIDwithName(t.obj))
      }
    }
    store.commit
    PdQuery("success", false, p.pdChangeList)
  }

  /*
  TODO: this can be combined with create table method
  simply called manageTable or something!
   */
  def removeTable(p: PdObj): PdQuery = ???

  var tableListeners = ArrayBuffer[String]()

  def queryTable(p: PdObj): PdQuery = {
    var queryResult = ListBuffer.empty[PdChangeJson]

    for (t <- p.pdChangeList) {
      if (t.pred == "has_type" && t.obj == "table") {
        val rows = store.query((store.getGUIDwithName(t.sub), store.getGUIDwithName("has_row"), v"row"), (v"row", store.getGUIDwithName("has_value"), v"value")).toList
        val columns = store.query((store.getGUIDwithName(t.sub), store.getGUIDwithName("has_column"), v"column"), (v"column", store.getGUIDwithName("has_value"), v"value")).toList

        for (r <- rows){
          queryResult += new PdChangeJson("ts", "e", "is_row" , store.getName(r.get(v"row")), store.getName(r.get(v"value")))
        }
        for (c <- columns) {
          queryResult += new PdChangeJson("ts", "e", "is_column", store.getName(c.get(v"column")), store.getName(c.get(v"value")))
        }
        for(r <- rows){
          for(c <- columns){
            val rowName = store.getGUIDwithName(store.getName(r.get(v"value")))
            val colName = store.getGUIDwithName(store.getName(c.get(v"value")))

            //registering listeners for all the possible row-column combinations
            registerListener(new PdChangeJson("ts", "e", store.getName(rowName), store.getName(colName), "_"), p.actor)

            val cellVal = store.query((rowName, colName, v"x"))
            for(cv <- cellVal){
              queryResult += new PdChangeJson("ts", "e", store.getName(rowName) , store.getName(colName), cv.get(v"x").toString)
            }
          }
        }
      }
    }
    PdQuery("success", false, queryResult.toList)
  }

  def applyPdc(pdc: PdObj): PdQuery = {
    for (c <- pdc.pdChangeList) {
      if(c.ch == "+" && (c.pred == "has_row" || c.pred == "has_value" || c.pred == "has_column")){
        store.addLink(store.getGUIDwithName(c.sub), store.getGUIDwithName(c.pred), store.getGUIDwithName(c.obj))
      }
      else if (c.ch == "-" && (c.pred == "has_row" || c.pred == "has_value" || c.pred == "has_column")){
        store.removeLink(store.getGUIDwithName(c.sub), store.getGUIDwithName(c.pred), store.getGUIDwithName(c.obj))
      }
      else if (c.ch == "+" && (c.pred != "has_row" || c.pred != "has_value" || c.pred != "has_column")){
        store.addLink(store.getGUIDwithName(c.sub), store.getGUIDwithName(c.pred), c.obj)
        store.commit
      }
      else if (c.ch == "-" && (c.pred != "has_row" || c.pred != "has_value" || c.pred != "has_column")){
        store.removeLink(store.getGUIDwithName(c.sub), store.getGUIDwithName(c.pred), c.obj)
      }
      registerListener(c, pdc.actor)
    }
    store.commit
    PdQuery("success", false, pdc.pdChangeList)
  }

  def registerListener(pdc: PdChangeJson, actor: ActorRef): Unit = {

    var lTriple = new LTriple(pdc.sub.toString, pdc.pred.toString, "_")

    if(listeningActors.contains(actor)){
      if (!listeningActors(actor).exists(x => x.lSub.toString.equals(lTriple.lSub.toString) && x.lPred.toString.equals(lTriple.lPred.toString)))
        listeningActors(actor) += lTriple
    }else{
      listeningActors += actor -> Set[LTriple](lTriple)
    }
    if(!registeredListeners.exists( x => x.lSub.toString.equals(lTriple.lSub.toString) && x.lPred.toString.equals(lTriple.lPred.toString))){

      registeredListeners += lTriple

      store.listen((null, ChangeType.WILDCARD, store.getGUIDwithName(lTriple.lSub.toString), store.getGUIDwithName(lTriple.lPred.toString), null), (c: Change) => {
        system.scheduler.scheduleOnce(1 millisecond){
          // TODO: Address all the following conditions:
          /**
            * 1 - a row or column does not exist and we add a value to it
            * (transaction=Role GUID(0xd97181cb54753d87L, 0x35acb1ff817d91L), LINK_ADDED, instance1=Role "table_3_A2", role2=Role "has_value", instance2=Role "P1")
            * (transaction=Role GUID(0xd97181cb54753d87L, 0x35acb1ff817d91L), LINK_ADDED, instance1=Role "table_3", role2=Role "has_row", instance2=Role "table_3_A2")
            *
            * 2 - a row or column does exist we remove value from it
            * (transaction=Role GUID(0xd97181cb54753d87L, 0x35acb25f602f41L), LINK_REMOVED, instance1=Role "table_3_A2", role2=Role "has_value", instance2=Role "P2")
            * (transaction=Role GUID(0xd97181cb54753d87L, 0x35acb25f602f41L), LINK_REMOVED, instance1=Role "table_3", role2=Role "has_row", instance2=Role "table_3_A2")
            *
            * 3 - a row or column exists and we change its value
            * (transaction=Role GUID(0xd97181cb54753d87L, 0x35acb23a7bc1d1L), LINK_REMOVED, instance1=Role "table_3_A2", role2=Role "has_value", instance2=Role "P1")
            * (transaction=Role GUID(0xd97181cb54753d87L, 0x35acb23a7bc1d1L), LINK_ADDED, instance1=Role "table_3_A2", role2=Role "has_value", instance2=Role "P2")
            */

          if(store.getName(c.role2) == "has_row" || store.getName(c.role2) == "has_column"){
            Logger.debug(c.toRawString)
            for(la <- listeningActors){
              if (la._2.exists(x => x.lSub.toString.equals(store.getName(c.instance1)) && x.lPred.toString.equals(store.getName(c.role2)))){
                //TODO: We need to add all the clients who are listeneing to this to listen to the new Triple changes as well.
                Logger.error(la._1.toString)
                if(c.changeType == ChangeType.LINK_ADDED){
                  //TODO: Add it to the listening actors list
                }else if (c.changeType == ChangeType.LINK_NOW_REMOVED){
                  //TODO: Remove it from the listening actors list
                }
              }
            }
          }else{
            val theChange = new PdChangeJson(
              "ta",                                                                            //timestamp
              if (c.getChangeType.toString == "LINK_ADDED") "+" else  "-",                     //change type
              store.getName(c.instance1),                                                      //subject
              store.getName(c.role2),                                                          //predicate
              if (isString(c.instance2)) c.instance2.toString else store.getName(c.instance2)) //object

            var recievingActors : Set[ActorRef] = Set()

            for(la <- listeningActors){
              if (la._2.exists(x => x.lSub.toString.equals(theChange.sub) && x.lPred.toString.equals(theChange.pred) /*&& !la._1.equals(actor)*/)){
                recievingActors += la._1
              }
            }
            actors.UserManager.updateListeningActors(theChange, recievingActors)
          }
        }
      })
    }
  }

  def isString(cls: AnyRef): Boolean ={
    cls match {
      case s: String => true
      case _  => false
    }
  }
}