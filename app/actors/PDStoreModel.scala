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

    changes.foreach{ change =>
      val s = (change \ "sub").as[String]
      val p = (change \ "pred").as[String]
      subjects += s
      predicates += p
    }

    subjects = subjects.distinct
    predicates = predicates.distinct

    if (subjects.length == 1 ){
      predicates.foreach{ predicate =>
        val qGuid = computeGuid(predicate)
        val results = store.query((subjects(0), qGuid, v"x"))
        while(results.hasNext) {
          val triple = new Triple(
            ta = "_",
            ch = "_",
            sub = subjects(0),
            pred = predicate,
            obj = results.next.get(v"x").toString
          )
          allQueries += Json.toJson(triple)
        }
      }
    } else if (predicates.length == 1){
      var qGuid = computeGuid(predicates(0))
      subjects.foreach { subject =>
        val results = store.query((subject, qGuid, v"x"))
        while(results.hasNext){
          val triple = new Triple(
            ta = "_",
            ch = "_",
            sub = subject,
            pred = predicates(0),
            obj = results.next.get(v"x").toString
          )
          allQueries += Json.toJson(triple)
        }
      }
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