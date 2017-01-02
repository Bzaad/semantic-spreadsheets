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

@Singleton
class socket @Inject() extends Controller{

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
        out
      }
    })
    (in, out)
  }
}
