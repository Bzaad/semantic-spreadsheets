package controllers

import javax.inject._

import play.api.mvc._
import play.api.libs.streams._
import akka.actor._
import akka.stream.Materializer
import play.api.libs.iteratee.{Concurrent, Enumerator, Iteratee}
import play.api.libs.json.JsValue
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by Behzad on 1/2/2017.
  */
// @Singletone
class teststream @Inject() (implicit system: ActorSystem, materializer: Materializer) extends Controller{
  /*
  object MyWebSocketActor {
    def props(out: ActorRef) = Props(new MyWebSocketActor(out))
  }

  class MyWebSocketActor(out: ActorRef) extends Actor {
    def receive = {
      case msg: JsValue =>
        out ! msg
    }
  }

  def socket = WebSocket.accept[JsValue, JsValue] { request =>
    ActorFlow.actorRef(out => MyWebSocketActor.props(out))
  }
**/
  def teststreamview = Action {
    Ok(views.html.teststream("welcome to web-socket!"))
  }

  val (publicOut, channel) = Concurrent.broadcast[String]

  def socket = WebSocket.using[String]{ request =>
    val in = Iteratee.foreach{
      msg:String => {
        println(msg)
        channel.push(msg)
      }
    }
    val out = Enumerator.interleave(publicOut)
    println(out)
    (in, out)
  }

}
