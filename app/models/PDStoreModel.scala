package models

import pdstore._
import play.api.Logger
import pdstore.notify.PDListener
import pdstore.notify.PDListenerAdapter

import scala.collection.mutable.ListBuffer
import play.api.libs.json._


case class PDStoreModel()

object PDStoreModel {
  val store = new PDStore("pdstore_dd")

  def addChanges(pdChangeSeq: Seq[PdChange]) = {

    store.begin
    pdChangeSeq.foreach{ change =>
      store.addLink(change.sub, store.getGUIDwithName(change.pred), change.obj)
    }
    store.commit
    val rows = store.query(("table-a", store.getGUIDwithName("has-row"), v"x"))
    val columns = store.query(("table-a", store.getGUIDwithName("has-col"), v"x"))
    while(rows.hasNext){
      print(rows.next().get(v"x").toString + " : ")
    }
    println("\n")
    while(columns.hasNext){
      print(columns.next().get(v"x").toString + " : ")
    }
  }

  def getAllTables(pdCHangeSeq: Seq[PdChange]): Seq[PdChange] = {
    Logger.debug(pdCHangeSeq.toString)
    val tables = store.query((v"x", store.getGUIDwithName("has-type"), "table"))
    var allTables = ListBuffer.empty[PdChange]
    while(tables.hasNext){
      allTables += new PdChange("ts", "e", tables.next().get(v"x").toString, "has-type", "table")
    }
    Logger.debug(allTables.toString)
    return allTables
  }

  def testListener(): Unit ={

    val roleA = new GUID()

  }
  /*
  def sparqlQuery(sparqlQ: JsValue): JsValue = {
    var pdChanges = ListBuffer.empty[pdstore.sparql.Constraint[GUID, AnyRef, GUID]]
    val changes = (sparqlQ \ "msg" \ "changes").as[List[JsObject]]
    changes.foreach { change =>
       pdChanges += constraintFromJSChange(change)
    }
    var result = store.query(pdChanges: _*)
    /**
      * TODO: map results directly to the pDchanges Object
      */
    return sparqlQ
    }
  */
  /*

  def constraintFromJSChange(jsobject: JsObject): pdstore.sparql.Constraint[GUID, AnyRef, GUID] = {
    var obj: String = (jsobject \ "obj").as[String]
    var pred: String = (jsobject \ "pred").as[String]
    var sub: String = (jsobject \ "sub").as[String]
    var ta: String = (jsobject \ "ta").as[String]
    val currentTa = store.getCurrentTransaction

    if (obj.charAt(0) == '?') {
      obj = store.gV(obj.stripPrefix("?")).toString()
    }
    if(pred.charAt(0) == '?'){
      pred = store.gV(pred.stripPrefix("?")).toString()
    }
    if(sub.charAt(0) == '?'){
      sub = store.gV(pred.stripPrefix("?")).toString()
    }
    (currentTa, (pdstore.ChangeType.fromStringSymbol((jsobject \ "ch").as[String])), sub, pred, obj)
  }
  */
 /*
  def tableQuery(query: JsValue): JsValue = {
    store.begin
    val changes = (query \ "msg" \ "changes").as[List[JsObject]]
    var subjects = ListBuffer.empty[String]
    var predicates = ListBuffer.empty[String]
    var allQueries = ListBuffer.empty[JsValue]
    var qTime = "_"
    var result = "_"

    changes.foreach{ change =>
      val s = (change \ "sub").as[String]
      val p = (change \ "pred").as[String]
      val t = (change \ "ta").as[String]
      if (t != "_" && qTime == "_"){
        qTime = t
      }
      subjects += s
      predicates += p
    }

    subjects = subjects.distinct
    predicates = predicates.distinct

    predicates.foreach{ predicate =>
      subjects.foreach{ subject =>
        // if it's current:
        println("qtime" + qTime + subject)
        if (qTime == "_"){
          val results = store.query((subject, store.getGUIDwithName(predicate), v"x"))
          if (!results.isEmpty){
            result = results.next.get(v"x").toString
          } else if (results.isEmpty) {
            result = "_"
          }
        } else if (qTime != "_"){
          // TODO: query with the timestamp
          var results = ListBuffer[String]() // store.query("with timestampe"!);
          if (!results.isEmpty){
            //result = results.next.get(v"x").toString
          } else if (results.isEmpty){
            result = "_"
          }
        }
        val triple = new Triple(
          ta = qTime,
          ch = "_",
          sub = subject,
          pred = predicate,
          obj = result
        )
        allQueries += Json.toJson(triple)
      }
      println(allQueries)
    }
    val message = new QMessage(
      changes = Json.toJson(allQueries)
    )
    store.commit
    return Json.toJson(message)
  }

  // just a place holder for remove function
  def remove(tSubject: String, tPredicate: String, tObject: String) = {
    println("removed the triple { " + tObject + "," + tPredicate + "," + tSubject + "}" )
  }
  */
}