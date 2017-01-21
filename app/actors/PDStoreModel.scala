package actors

import java.security.MessageDigest
import pdstore._
import pdstore.GUID
import actors.Triple._

import scala.collection.mutable.ArrayBuffer

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

  def query(qSubject: String, qPredicate: String, qObject: String): Unit = {
    var qGuid = computeGuid(qPredicate)
    var result = ArrayBuffer[String]()
    var length = 0

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
  }

  // just a place holder for remove function
  def remove(tSubject: String, tPredicate: String, tObject: String) = {
    println("removed the triple { " + tObject + "," + tPredicate + "," + tSubject + "}" )
  }

  def testFunc(triple: Triple) = {
    println(triple)
  }
}