package models

import pdstore._
import PDStore._
import play.api.Logger
import scala.collection.mutable.{ArrayBuffer, ListBuffer}
import actors.UserManager.{updateListeningActors, addToListeners}

case class PDStoreModel()

object PDStoreModel {

  val store = PDStore("pdstore_dd")

  def query(pdObj: PdObj): PdQuery = {
    var queryResult = ArrayBuffer.empty[PdChangeJson]

    for (p <- pdObj.pdChangeList) {

      if (p.sub == "?" && p.obj != "?") {
        val qResult = store.query((v"x", store.getGUIDwithName(p.pred), store.getGUIDwithName(p.obj)))

        while (qResult.hasNext) {
          val t = store.begin
          val queriedSub = qResult.next().get(v"x")
          val result = PdChangeJson("ts", "e", store.getName(queriedSub), p.pred, p.obj)

          store.listen((queriedSub, store.getGUIDwithName(p.pred), null), (c: Change) => {
            //updateListeningActors(new LTriple(queriedSub, predGuid, null), result)
          })
          queryResult += result
          store.commit
        }
      } else if (p.sub != "?" && p.obj == "?") {
        val qResult = store.query((p.sub, store.getGUIDwithName(p.pred), v"x"))
        while (qResult.hasNext) {
          val t = store.begin
          val queriedObj = qResult.next().get(v"x").toString
          val result = new PdChangeJson("ts", "e", p.sub, p.pred, queriedObj)
          val lTriple = "?_" + p.pred + "_" + queriedObj
          addToListeners(lTriple, pdObj.actor, result)

          store.listen((null, store.getGUIDwithName(p.pred), queriedObj), (c: Change) => {
            updateListeningActors(pdObj.actor, lTriple, result)
            //TODO: send message to all listening users
          })
          queryResult += result
        }
      } else {
        Logger.error("not a valid query")
      }
    }

    if (queryResult.nonEmpty) {
      PdQuery("cQuery", false, queryResult.toList)
    } else {
      PdQuery("cQuery", false, List[PdChangeJson]())
    }
  }

  def createTable(p: PdObj): PdQuery = {
    //TODO: error handling if the table with the same name already exists
    for (t <- p.pdChangeList) {
      if (t.pred == "has_type" && t.obj == "table") {
        store.add(store.getGUIDwithName(t.sub), store.getGUIDwithName(t.pred), store.getGUIDwithName(t.obj))
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
      /*
      TODO: we need to listen to the pattern as well.
      this need to be properly handled!
       */
      if(c.ch == "+" && (c.pred == "has_row" || c.pred == "has_value" || c.pred == "has_column"))
        store.add(store.getGUIDwithName(c.sub), store.getGUIDwithName(c.pred), store.getGUIDwithName(c.obj))
      else if (c.ch == "-" && (c.pred == "has_row" || c.pred == "has_value" || c.pred == "has_column"))
        store.remove(store.getGUIDwithName(c.sub), store.getGUIDwithName(c.pred), store.getGUIDwithName(c.obj))
      else if (c.ch == "+")
        store.add(store.getGUIDwithName(c.sub), store.getGUIDwithName(c.pred), c.obj)
      else if (c.ch == "-")
        store.remove(store.getGUIDwithName(c.sub), store.getGUIDwithName(c.pred), c.obj)
    }
    store.commit
    PdQuery("success", false, pdc.pdChangeList)
  }
}

  /*
  def applyChanges(pdObj: PdObj) = {

    store.begin
    for (p <- pdObj.pdChangeList){
      if (p.sub != "?" && p.obj != "?" && p.pred != "") {
        store.addLink(p.sub, store.getGUIDwithName(p.pred), p.obj)
      } else {
        Logger.error("not a valid query!")
      }
    }
    store.commit
    */
    /*
    val rows = store.query(("table-a", store.getGUIDwithName("has-row"), v"x"))
    val columns = store.query(("table-a", store.getGUIDwithName("has-col"), v"x"))
    while(rows.hasNext){
      print(rows.next().get(v"x").toString + " : ")
    }
    println("\n")
    while(columns.hasNext){
      print(columns.next().get(v"x").toString + " : ")
    }
    */
  //}

  /*
  def getAllTables(pdCHangeList: List[PdChangeJson]): List[PdChangeJson] = {
    val tables = store.query((v"x", store.getGUIDwithName("has-type"), "table"))
    var allTables = ListBuffer.empty[PdChangeJson]
    while(tables.hasNext){
      allTables += new PdChangeJson("ts", "e", tables.next().get(v"x").toString, "has-type", "table")
    }
    return allTables.toList
  }
  */
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