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

  def computeGuid(tGuid: String): GUID = {
    return new GUID (MessageDigest.getInstance("MD5").digest(tGuid.getBytes))
  }

  def addTriple(triple: Triple) = {
    store.addLink(triple.sub, computeGuid(triple.pred), triple.obj)
    store.setName(computeGuid(triple.pred), triple.pred)
  }

  def query(query: JsValue): JsValue = {
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
        val qGuid = computeGuid(predicate)
        // if it's current:
        println("qtime" + qTime + subject)
        if (qTime == "_"){
          val results = store.query((subject, qGuid, v"x"))
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