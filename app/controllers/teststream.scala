package controllers

import javax.inject._
import play.api.mvc._
import play.api.libs.streams._
import akka.actor._
import akka.stream.Materializer
import play.api.libs.json.JsValue

/**
  * Created by Behzad on 1/2/2017.
  */
// @Singletone
class teststream @Inject() (implicit system: ActorSystem, materializer: Materializer) extends Controller{

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


}
