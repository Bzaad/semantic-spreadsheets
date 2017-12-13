package models

import pdstore._
import PDStore._
import akka.actor.{ActorRef, ActorSystem}

import scala.concurrent.duration._
import play.api.Logger
import play.api.libs.json.Json

import scala.collection.mutable
import scala.collection.mutable.{ArrayBuffer, HashMap, ListBuffer, Set}

case class PDStoreModel()

object PDStoreModel {
  import system.dispatcher
  val store = PDStore("pdstore_dd")
  val system = ActorSystem("aSystem")
  var actorsAndTheirTriples = new HashMap[ActorRef, Set[PdChangeJson]]
  var currentListenerList = Set.empty[PdChangeJson]

  /**
    * Queries the object values for all the possible subject-predicates combinations
    * this is where a user adds a new row or column
    * if a new column (predicate) is added all the possible values for available rows (subjects) will be queried
    * if a new row (subject) is added all the possible values of available columns (predicates) will be queried
    * @param pdObj
    * @return
    */
  def query(pdObj: PdObj): PdQuery = {
    var queryResult = ArrayBuffer.empty[PdChangeJson]


    //actorsAndTheirTriples(pdObj.actor) =  actorsAndTheirTriples(pdObj.actor) ++= pdObj.pdChangeList.toSet

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
        actorsAndTheirTriples(pdObj.actor) = actorsAndTheirTriples(pdObj.actor) += p
        val qResult = store.query((store.getGUIDwithName(p.sub), store.getGUIDwithName(p.pred), v"x"))
        while (qResult.hasNext) {
          val t = store.begin
          val queriedObj = qResult.next().get(v"x").toString
          val result = new PdChangeJson("ts", "e", p.sub, p.pred, queriedObj)
          queryResult += result
        }
      } else {
        Logger.error("not a valid query")
      }
    }

    if (pdObj.pdChangeList.length > 0 && !"has_type".equals(pdObj.pdChangeList(0).pred) && !"table".equals(pdObj.pdChangeList(0).obj))
      tableListenerUpdate("update", pdObj.actor, pdObj.pdChangeList)

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

  def queryTable(p: PdObj): PdQuery = {

    // TODO: if somebody else is already working on a table, all their triples is available on their listener list
    // so we don't really need to query them all over again


    var queryResult = ListBuffer.empty[PdChangeJson]
    var loadListenerLB = ListBuffer.empty[PdChangeJson]
    val rowsColumnsPdChangeJson = ListBuffer.empty[PdChangeJson]

    // pass the table name as a part of the messsage
    // this way we can create a complete table on the front end as change object
    // this layer is totaly independent form the presentation layer

    queryResult += p.pdChangeList(0)
    loadListenerLB += p.pdChangeList(0)

    for (t <- p.pdChangeList) {
      if (t.pred == "has_type" && t.obj == "table") {
        val rows = store.query((store.getGUIDwithName(t.sub), store.getGUIDwithName("has_row"), v"row"), (v"row", store.getGUIDwithName("has_value"), v"value")).toList
        val columns = store.query((store.getGUIDwithName(t.sub), store.getGUIDwithName("has_column"), v"column"), (v"column", store.getGUIDwithName("has_value"), v"value")).toList

        for (r <- rows){
          rowsColumnsPdChangeJson += new PdChangeJson("ts", "e", p.pdChangeList(0).sub, "has_row", store.getName(r.get(v"row")))
          rowsColumnsPdChangeJson += new PdChangeJson("ts", "e", store.getName(r.get(v"row")), "has_value", store.getName(r.get(v"value")))
        }
        for (c <- columns) {
          rowsColumnsPdChangeJson += new PdChangeJson("ts", "e", p.pdChangeList(0).sub, "has_column", store.getName(c.get(v"column")))
          rowsColumnsPdChangeJson += new PdChangeJson("ts", "e", store.getName(c.get(v"column")), "has_value", store.getName(c.get(v"value")))
        }
        queryResult ++= rowsColumnsPdChangeJson
        loadListenerLB ++= rowsColumnsPdChangeJson
        for(r <- rows){
          for(c <- columns){
            var rn = store.getName(r.get(v"value"))
            var cn = store.getName(c.get(v"value"))
            loadListenerLB += new PdChangeJson("ts", "e", rn, cn, "?")
            val cellVal = store.query((store.getGUIDwithName(rn), store.getGUIDwithName(cn), v"x"))
            for(cv <- cellVal){
              queryResult += new PdChangeJson("ts", "e", rn , cn, cv.get(v"x").toString)
            }
          }
        }
      }
    }

    tableListenerUpdate("load", p.actor, loadListenerLB.toList)
    PdQuery("displayTable", false, queryResult.toList)
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

  def tableListenerUpdate(action: String, actor: ActorRef, pdChangeList: List[PdChangeJson]): Unit = {
    action match {
      case "update" => {
        //update the actor listener list
        for(att <- actorsAndTheirTriples(actor)){
          for (pd <- pdChangeList){
            if (pd.sub.equals(att.sub) && pd.pred.equals(att.pred) && pd.obj.equals(att.obj) && "-".equals(pd.ch)){
              actorsAndTheirTriples(actor) -= att
              if ("has_value".equals(pd.pred)) {
                actorsAndTheirTriples(actor) = actorsAndTheirTriples(actor).filterNot {
                  p => pd.obj.equals(p.sub) || pd.obj.equals(p.pred)
                }
              }
            } else if ("+".equals(pd.ch)) {
              actorsAndTheirTriples(actor) += new PdChangeJson(pd.ta, "e", pd.sub, pd.pred, pd.obj)
            } else if (!"-".equals(pd.ch)){
              actorsAndTheirTriples(actor) += pd
            }
          }
        }
        /**
          * check if anybody else is to the same tabale and update their listener list as well!
          */
        var tableTuple = actorsAndTheirTriples(actor)
        tableTuple  = tableTuple.filter {
          p => "has_type".equals(p.pred) && "table".equals(p.obj)
        }
        for(a <- actorsAndTheirTriples){
          if (a._2.exists(p => "has_type".equals(p.pred) && "table".equals(p.obj) && tableTuple.toSeq(0).sub.equals(p.sub)) && !a._1.toString.equals(actor.toString)){
            actorsAndTheirTriples -= a._1
            actorsAndTheirTriples += (a._1 -> actorsAndTheirTriples(actor))
          }
        }
        maintainCurrentListenerList(actorsAndTheirTriples(actor))
      }
      case "load" => {
        if (actorsAndTheirTriples.exists(x => x._1.equals(actor))) {
          actorsAndTheirTriples -= actor
        }
        actorsAndTheirTriples += (actor -> (mutable.Set()++pdChangeList))

        /**
          * the reason for this check is a pre-registration of has_row and has_column predicate so that the
          * listener gets fired even the very first after the creation of the table
          */
        if(actorsAndTheirTriples(actor).size == 1){
          actorsAndTheirTriples(actor) += new PdChangeJson("ts", "e", actorsAndTheirTriples(actor).toSeq(0).sub, "has_column", "default_column")
          actorsAndTheirTriples(actor) += new PdChangeJson("ts", "e", actorsAndTheirTriples(actor).toSeq(0).sub, "has_row", "default_row")
        }

        maintainCurrentListenerList(actorsAndTheirTriples(actor))
      }
      case _ => {
        Logger.error("Illegal operation!")
      }
    }
  }

  def maintainCurrentListenerList(pdcSet: Set[PdChangeJson]): Unit ={
    // TODO: we don't remove triples form listener list, because of the obvious reasons
    for (p <- pdcSet) {

      if (!currentListenerList.exists(x => p.sub.equals(x.sub) && p.pred.equals(x.pred))) {
        val pdcl = new PdChangeJson("e", "ts", p.sub, p.pred, "_")
        currentListenerList += pdcl
        registerListener(pdcl)
      }

    }
  }

  def registerListener(pdChange: PdChangeJson): Unit ={
    store.listen((null, ChangeType.WILDCARD, store.getGUIDwithName(pdChange.sub), store.getGUIDwithName(pdChange.pred), null), (c: Change) => {
      system.scheduler.scheduleOnce(1 millisecond){

        def f[T](v: T) = v match {
          case _: String => "String"
          case _: GUID   => "GUID"
          case _         => "Unknown"
        }

        notifyListeningActors(
          new PdChangeJson(
            "ts",
            if(c.changeType == ChangeType.LINK_ADDED) "+" else "-",
            store.getName(c.instance1),
            store.getName(c.role2),
            if("GUID".equals(f(c.instance2))) store.getName(c.instance2) else c.instance2.toString
          )
        )
      }
    })
  }

  def notifyListeningActors(pdcj: PdChangeJson): Unit ={
    var queryResult = ListBuffer.empty[PdChangeJson]
    //TODO: if the lenght is two and one is add and another remove, it's an edit and has to be handled properly by first sending the remove and then add
    queryResult += pdcj
    if("has_column".equals(pdcj.pred) || "has_row".equals(pdcj.pred)){
      val cellVal = store.query((store.getGUIDwithName(pdcj.obj), "has_value", v"x"))
      for(cv <- cellVal){
        queryResult += new PdChangeJson("ts", "+", pdcj.obj , "has_value", store.getName(cv.get(v"x")))
      }
    }
    var receivingActors = Set.empty[ActorRef]
    for (qr <- queryResult){
      for(ac <- actorsAndTheirTriples){
        if (ac._2.exists(x => x.sub.equals(qr.sub) && x.pred.equals(qr.pred)))
          receivingActors += ac._1
      }
    }
    val pdQuery = new PdQuery("listener", false, queryResult.toList)
    for (rv <- receivingActors){
      rv ! Json.toJson(pdQuery)
    }
  }
}