package controllers

/**
  * Created by behzadfarokhi on 10/01/17.
  */
import javax.inject._
import scala.concurrent.Future
import actors.UserActor
import akka.stream.Materializer
import play.api.Logger
import play.api.Play.current
import play.api.libs.json.JsValue
import play.api.mvc.Action
import play.api.mvc.Controller
import play.api.mvc.WebSocket

class Application @Inject() (implicit val mat: Materializer) extends Controller {
  val UID = "uid"
  var counter = 0

  def index = Action { implicit request =>
    println(request.session)
    val uid: String = request.session.get(UID).getOrElse {
      counter += 1
      counter.toString
    }
    Logger.debug("creation uid " + uid)
    Ok(views.html.chat(uid)).withSession(request.session + (UID -> uid))
  }

  /*
  the WebSocket.tryAcceptWithActor will instantiate a UserActor with the given Props and a websocket that will
  send all messages from the client to the UserActor. It will also instantiate another actor,
  and pass its reference to UserActor's constructor. Messages sent to that other actor will then be
  forwarded via the websocket to the client side.
   */

  def ws = WebSocket.tryAcceptWithActor[JsValue, JsValue] { implicit request =>

    Future.successful(request.session.get(UID) match {
      case None => Left(Forbidden)
      case Some(uid) => Right(UserActor.props(uid))
    })
  }
}
