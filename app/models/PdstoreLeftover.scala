package models

/**
  * Created by behzadfarokhi on 4/10/17.
  */
class PdstoreLeftover {




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

  /*
    store.listen((store.getGUIDwithName(c.sub), store.getGUIDwithName(c.pred), null), (c: Change) => {
      Logger.error("146")
      Logger.error(c.toString)
    })
    */
  /*
          for(r <- rows){
        for(c <- columns){
          val rowName = store.getGUIDwithName(store.getName(r.get(v"value")))
          val colName = store.getGUIDwithName(store.getName(c.get(v"value")))
          val cellVal = store.query((rowName, colName, v"x"))
          for(cv <- cellVal){
            /**
              * this need to be changed!
              */
            store.listen((rowName, colName, null), (c: Change)=>{
              Logger.error("121")
              Logger.error("a value has changed!")
              Logger.error(c.toString)
            })
            queryResult += new PdChangeJson("ts", "e", store.getName(rowName) , store.getName(colName), cv.get(v"x").toString)
          }
        }
      }
  */
  /*
          if(!tableListeners.contains(t.sub)) {
        tableListeners += t.sub
        store.listen((store.getGUIDwithName(t.sub), store.getGUIDwithName("has_row"), null), (c: Change) => {
          Logger.error("85")
          Logger.error("A Row Has Changed!")
          Logger.error(c.toString)
        })
        store.listen((store.getGUIDwithName(t.sub), store.getGUIDwithName("has_column"), null), (c: Change)=> {
          Logger.error("90")
          Logger.error("A Column Has Changed!")
          Logger.error(c.toString)
        })
      }
   */


}
