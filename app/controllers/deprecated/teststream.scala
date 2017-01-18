package controllers

import java.security.MessageDigest
import javax.inject._

import play.api.mvc._
import play.api.libs.iteratee.{Concurrent, Iteratee}
import play.api.libs.json._

import scala.concurrent.ExecutionContext.Implicits.global
import pdstore._
import pdstore.GUID

/**
  * Created by Behzad on 1/2/2017.
  */
// @Singletone
class teststream @Inject() extends Controller{

  def teststreamview = Action {
    Ok("hello!")
    //Ok(views.html.teststream("welcome to web-socket!"))
  }

  var addCounter = 0
  var deleteCounter = 0

  var store = new PDStore("PDStore")
  store.begin

  def computeGuid (tGuid: String) : GUID = {
    return new GUID (MessageDigest.getInstance("MD5").digest(tGuid.getBytes))
  }

  case class allChanges(ta : String, ch : String, sub : String, pred : String,  obj : String)
  implicit val chsReader = Json.reads[allChanges]


  def addTriples (change: allChanges): Unit = {
    addCounter += 1
    store.addLink(change.obj, computeGuid(change.pred), change.sub)
    store.setName(computeGuid(change.pred), change.pred)
    store.commit
  }

  def deleteTriples (change: allChanges): Unit = {
    deleteCounter += 1
    println("not yet implemeneted!")
  }

  def parseMessge(messge: JsValue): Unit = {
    val allChanges = (messge \ "changes").as[List[allChanges]]
    allChanges.foreach { change =>
      if(change.ch == "+"){
        addTriples(change)
      }else if (change.ch == "-"){
        deleteTriples(change)
      }
    }
  }

  var counter = 0
  val thisMessage = JsObject(Seq("messge" -> JsString("the call was successful")))
  def socket = WebSocket.using[JsValue] { request =>
    val (out, channel) = Concurrent.broadcast[JsValue]
    counter += 1
    var pid = counter
    val in = Iteratee.foreach[JsValue](_ match {
      case message: JsObject => {
        parseMessge( message )
        channel.push( Json.obj( "added triples" -> JsNumber(addCounter)) ++ JsObject(Seq("deleted tripples" -> JsNumber(deleteCounter))))
        addCounter = 0
        deleteCounter = 0
      }
    })
    (in, out)
  }
}
