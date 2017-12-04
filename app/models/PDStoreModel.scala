package models

import pdstore._
import PDStore._
import akka.actor.{ActorRef, ActorSystem}
import scala.concurrent.duration._
import play.api.Logger
import scala.collection.mutable
import scala.collection.mutable.{ArrayBuffer, ListBuffer, Set, HashMap}

case class PDStoreModel()

object PDStoreModel {

  val store = PDStore("pdstore_dd")
  val system = ActorSystem("aSystem")
  import system.dispatcher

  var registeredListeners = new ListBuffer[LTriple]()

  var listeningActors = HashMap.empty[ActorRef, Set[LTriple]]

  var sameTableHash = Map.empty[String, Set[ActorRef]]

  var actorsAndTheirTriples = new HashMap[ActorRef, Set[PdChangeJson]]

  var currentListenerList = Set.empty[PdChangeJson]


  def query(pdObj: PdObj): PdQuery = {
    var queryResult = ArrayBuffer.empty[PdChangeJson]

    // query all tables
    // ? has_type table

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
        store.addLink(store.getGUIDwithName(t.sub), store.getGUIDwithName("has_row"), store.getGUIDwithName(""))
        store.addLink(store.getGUIDwithName(t.sub), store.getGUIDwithName("has_column"), store.getGUIDwithName(""))
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

    // TODO: if somebody else is already working on a table, all their triples is available on their listener list
    // so we don't really need to query them all over again


    var queryResult = ListBuffer.empty[PdChangeJson]

    // pass the table name as a part of the messsage
    // this way we can create a complete table on the front end as change object
    // this layer is totaly independent form the presentation layer

    queryResult += p.pdChangeList(0)
    for (t <- p.pdChangeList) {
      if (t.pred == "has_type" && t.obj == "table") {
        val rows = store.query((store.getGUIDwithName(t.sub), store.getGUIDwithName("has_row"), v"row"), (v"row", store.getGUIDwithName("has_value"), v"value")).toList
        val columns = store.query((store.getGUIDwithName(t.sub), store.getGUIDwithName("has_column"), v"column"), (v"column", store.getGUIDwithName("has_value"), v"value")).toList

        for (r <- rows){
          queryResult += new PdChangeJson("ts", "e", p.pdChangeList(0).sub, "has_row", store.getName(r.get(v"row")))
          queryResult += new PdChangeJson("ts", "e", store.getName(r.get(v"row")), "has_value", store.getName(r.get(v"value")))
        }
        for (c <- columns) {
          queryResult += new PdChangeJson("ts", "e", p.pdChangeList(0).sub, "has_column", store.getName(c.get(v"column")))
          queryResult += new PdChangeJson("ts", "e", store.getName(c.get(v"column")), "has_value", store.getName(c.get(v"value")))
        }
        for(r <- rows){
          for(c <- columns){
            val rowName = store.getGUIDwithName(store.getName(r.get(v"value")))
            val colName = store.getGUIDwithName(store.getName(c.get(v"value")))
            val cellVal = store.query((rowName, colName, v"x"))
            for(cv <- cellVal){
              queryResult += new PdChangeJson("ts", "e", store.getName(rowName) , store.getName(colName), cv.get(v"x").toString)
            }
          }
        }
      }
    }
    tableListenerUpdate("load", p.actor, queryResult.toList)
    PdQuery("displayTable", false, queryResult.toList)
  }


  def tableListenerUpdate(action: String, actor: ActorRef, pdChangeList: List[PdChangeJson]): Unit = {
    action match {
      case "update" => {
        //update the actor listener list
        for(p <- actorsAndTheirTriples(actor)){
          for (pd <- pdChangeList){
            if (pd.sub.equals(p.sub) && pd.pred.equals(pd.pred) && "-".equals(pd.ch)){
              actorsAndTheirTriples(actor) -= p
              if(pdChangeList.exists(x => "has_row".equals(x.pred))){
                Logger.error("its a row")
              }
              if(pdChangeList.exists(x => "has_column".equals(x.pred))){
                Logger.error("its a column")
              }
            } else {
              actorsAndTheirTriples(actor) -= p
              actorsAndTheirTriples(actor) += new PdChangeJson(p.ta, "e", p.sub, p.pred, p.obj)
            }
          }
        }
        //TODO: check if anybody else is to the same tabale and update their listner list as well!
      }
      case "load" => {
        if (actorsAndTheirTriples.exists(x => x._1.equals(actor))) {
          actorsAndTheirTriples -= actor
        }
        actorsAndTheirTriples += (actor -> (mutable.Set()++pdChangeList))
        for (p <- pdChangeList) {
          // TODO: we don't remove triples form listener list, because of the obvious reasons
          if (!currentListenerList.exists(x => p.sub.equals(x.sub) && p.pred.equals(x.pred))) {
            currentListenerList += p
            registerListener(p)
          }
        }
      }
      case _ => {
        Logger.error("Illegal operation!")
      }
    }
  }

  def maintainCurrentListenerList(pdChangeJson: PdChangeJson): Unit ={

  }

  def applyPdc(pdc: PdObj): PdQuery = {
    var recievingActors = Set.empty[ActorRef]
    var tableName = ""
    for (c <- pdc.pdChangeList) {
      if(c.ch == "+" && (c.pred == "has_row" || c.pred == "has_value" || c.pred == "has_column")){
        store.addLink(store.getGUIDwithName(c.sub), store.getGUIDwithName(c.pred), store.getGUIDwithName(c.obj))
      }
      else if (c.ch == "-" && (c.pred == "has_row" || c.pred == "has_value" || c.pred == "has_column")){
        store.removeLink(store.getGUIDwithName(c.sub), store.getGUIDwithName(c.pred), store.getGUIDwithName(c.obj))
        if(!c.pred.equals("has_value")) tableName = c.sub
      }
      else if (c.ch == "+" && (c.pred != "has_row" || c.pred != "has_value" || c.pred != "has_column")){
        store.addLink(store.getGUIDwithName(c.sub), store.getGUIDwithName(c.pred), c.obj)
      }
      else if (c.ch == "-" && (c.pred != "has_row" || c.pred != "has_value" || c.pred != "has_column")){
        store.removeLink(store.getGUIDwithName(c.sub), store.getGUIDwithName(c.pred), c.obj)
      }
    }
    store.commit
    tableListenerUpdate("update", pdc.actor, pdc.pdChangeList)
    PdQuery("success", false, pdc.pdChangeList)
  }



  def registerListener(pdChange: PdChangeJson): Unit ={
    store.listen((null, ChangeType.WILDCARD, store.getGUIDwithName(pdChange.sub), store.getGUIDwithName(pdChange.pred), null), (c: Change) => {
      system.scheduler.scheduleOnce(1 millisecond){
        Logger.debug("Sending: " + c.toRawString)
        Logger.debug("To: Who?")
      }
    })
  }



  def isString(cls: AnyRef): Boolean ={
    cls match {
      case s: String => true
      case _  => false
    }
  }
}