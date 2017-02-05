package actors

import java.security.MessageDigest
import pdstore._
import pdstore.GUID
import actors.Triple._


import scala.collection.mutable.ArrayBuffer

import play.api.libs.json._

case class PDStoreModel()

object PDStoreModel {
  val store = new PDStore("pdstore_dd")

  def beginStore = {
    store.begin
    println("beginning the store!")
  }
  def commitStore = {
    println("committing to store!")
    store.commit
  }

  def computeGuid(tGuid: String): GUID = {
    return new GUID (MessageDigest.getInstance("MD5").digest(tGuid.getBytes))
  }

  def addTriple(triple: Triple) = {
    store.addLink(triple.sub, computeGuid(triple.pred), triple.obj)
    store.setName(computeGuid(triple.pred), triple.pred)
  }

  def query(query: JsValue): Unit = {

    val subjects = (query \ "msg" \ "subs").as[List[String]]
    val predicates = (query \ "msg" \ "preds").as[List[String]]
    var result = ArrayBuffer[String]()

    if (subjects.length == 1 ){
      predicates.foreach{ predicate =>
        val qGuid = computeGuid(predicate)
        val results = store.query((subjects(0), qGuid, v"x"))
        while(results.hasNext) {
          result += results.next.get(v"x").toString
        }
        result.foreach{r =>
          var triple = new Triple(
            ta = "",
            ch = "",
            sub = subjects(0),
            pred = predicate,
            obj = r
          )
        }
      }
    }
    else if (predicates.length == 1){
      var qGuid = computeGuid(predicates(0))
      subjects.foreach { subject =>
        val results = store.query((subject, qGuid, v"x"))
        while(results.hasNext){
          result += results.next.get(v"x").toString
        }
        result.foreach{r =>
          var triple = new Triple(
            ta = "",
            ch = "",
            sub = subject,
            pred = predicates(0),
            obj = r
          )
        }
      }
    }
    /*
    var qGuid = computeGuid(qPredicate)
    var result = ArrayBuffer[String]()
    var length = 0
    */
    /*
    if (qSubject == "_"){
      val results = store.query((v"x", qGuid, qObject))
      while(results.hasNext) {
        result += results.next.get(v"x").toString
      }
    }

    if (qObject == "_"){
      val results = store.query((qSubject, qGuid, v"x"))
      while(results.hasNext) {
        result += results.next.get(v"x").toString
      }
    }

    if (result.isEmpty){
      return ""
    } else {
      return result
    }
    */
  }

  // just a place holder for remove function
  def remove(tSubject: String, tPredicate: String, tObject: String) = {
    println("removed the triple { " + tObject + "," + tPredicate + "," + tSubject + "}" )
  }

  def testFunc(triple: Triple) = {
    println(triple)
  }
}