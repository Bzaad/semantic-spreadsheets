package controllers

/**
  * Created by Behzad on 1/2/2017.
  */

import javax.inject._
import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.iteratee._
import play.api.libs.concurrent._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import akka.actor._

class socket @Inject() extends Controller{

  val hubEnum = Concurrent.broadcast[JsValue]
  //val hub = Concurrent.hub[JsValue]( hubEnum )

  def socketview = Action {
    Ok(views.html.socket("welcome to web-socket!"))
  }

  var counter = 0
  def socket = WebSocket.using[JsValue] { request =>
    val (out, channel) = Concurrent.broadcast[JsValue]
    counter += 1
    var pid = counter
    val in = Iteratee.foreach[JsValue](_ match {
      case message: JsObject => {
        channel push (message ++ JsObject(Seq("pid" -> JsNumber(pid))))
      }
    })
    (in, out)
  }
}
