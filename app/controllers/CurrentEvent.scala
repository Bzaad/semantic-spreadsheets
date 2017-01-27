package controllers

import actors.{EventCluster, NodeSocket}
import akka.actor._
import akka.stream.Materializer
import com.google.inject.{Inject, Singleton}
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.JsValue
import play.api.libs.streams.ActorFlow
import play.api.mvc.{Action, Controller, WebSocket}

import scala.concurrent.Future

/**
  * Created by behzadfarokhi on 16/01/17.
  */

@Singleton
class CurrentEvent @Inject() (val messagesApi: MessagesApi, system: ActorSystem, mat: Materializer) extends Controller with I18nSupport {

  val Node = "node"

  implicit val implicitMaterializer: Materializer = mat
  implicit val implicitActorSystem: ActorSystem = system

  val eventCluster = system.actorOf(Props[EventCluster], "event-cluster")

  val tempForm = Form(single("tempid" -> nonEmptyText))

  def index = Action { implicit request =>
    request.session.get(Node).map { node =>
      Redirect(routes.CurrentEvent.event()).flashing("info" -> s"Redirected to chat as $node node")
    }getOrElse(Ok(views.html.index(tempForm)))
  }

  def event = Action { implicit request =>
    request.session.get(Node).map { node =>
      Ok(views.html.event(node))
    }.getOrElse(Redirect(routes.CurrentEvent.index()))
  }

  def leave = Action { implicit request =>
    Redirect(routes.CurrentEvent.index()).withNewSession.flashing("success" -> "See you soon!")
  }

  def tempid = Action { implicit request =>
    tempForm.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(views.html.index(formWithErrors))
      },
      tempid => {
        Redirect(routes.CurrentEvent.event())
          .withSession(request.session + (Node -> tempid))
      }
    )
  }

  def socket = WebSocket.acceptOrResult[JsValue, JsValue] { implicit request =>
    Future.successful(request.session.get(Node) match {
      case None => Left(Forbidden)
      case Some(uid) =>
        Right(ActorFlow.actorRef(NodeSocket.props(uid)))
    })
  }

  def tabs = Action { implicit request =>
    request.session.get(Node).map { node =>
      Ok(views.html.tabtest(node))
    }.getOrElse(Redirect(routes.CurrentEvent.index()))
  }

}
