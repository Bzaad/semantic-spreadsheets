package actors

import java.security.MessageDigest
import pdstore._
import pdstore.GUID
import actors.Triple._


import scala.collection.mutable.ListBuffer

import play.api.libs.json._

case class PDStoreModel()

object PDStoreModel {
  val store = new PDStore("pdstore_dd")

  def beginStore = {
    store.begin
  }
  def commitStore = {
    store.commit
  }

  def addTriple(triple: Triple) = {
    store.addLink(triple.sub, store.getGUIDwithName(triple.pred), triple.obj)
  }

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

  def constraintFromJSChange(jsobject: JsObject): pdstore.sparql.Constraint[GUID, AnyRef, GUID] = {
    var obj: String = (jsobject \ "obj").as[String]
    var pred: String = (jsobject \ "pred").as[String]
    var sub: String = (jsobject \ "sub").as[String]
    var ta: String = (jsobject \ "ta").as[String]
    val currentTa = store.getCurrentTransaction

    if (obj.charAt(0) == "?") {
      obj = store.gV(obj.stripPrefix("?")).toString()
    }
    if(pred.charAt(0) == "?"){
      pred = store.gV(pred.stripPrefix("?")).toString()
    }
    if(sub.charAt(0) == "?"){
      sub = store.gV(pred.stripPrefix("?")).toString()
    }

    (currentTa, (pdstore.ChangeType.fromStringSymbol((jsobject \ "ch").as[String])), sub, pred, obj)
  }

  def tableQuery(query: JsValue): JsValue = {
    beginStore
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
    commitStore
    return Json.toJson(message)
  }

  // just a place holder for remove function
  def remove(tSubject: String, tPredicate: String, tObject: String) = {
    println("removed the triple { " + tObject + "," + tPredicate + "," + tSubject + "}" )
  }

  def testFunc(triple: Triple) = {
    println(triple)
  }
}