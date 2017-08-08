package controllers

/**
  * Created by behzadfarokhi on 27/07/17.
  */

import play.api.mvc._
import play.api.libs.json.JsValue
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.streams.ActorFlow
import javax.inject._
import scala.concurrent.Future
import akka.actor.ActorSystem
import akka.stream.Materializer

import actors.User

@Singleton
class SpreadsheetController @Inject() (components: ControllerComponents) (implicit system: ActorSystem, mat: Materializer)extends AbstractController (components) {


  val UserName = "userName"
  val tempForm = Form(single("userid" -> nonEmptyText))

  def index = Action { implicit request =>
    request.session.get(UserName).map { user =>
      Redirect(routes.SpreadsheetController.loadSpreadsheet()).flashing("info" -> s"Redirected to spreadsheet as $user user!")
    }getOrElse(Ok(views.html.index(tempForm)))
  }


  def loadSpreadsheet = Action { implicit request =>
    request.session.get(UserName).map { user =>
      Ok(views.html.spreadsheet(user))
    }.getOrElse(Redirect(routes.SpreadsheetController.index()))
  }

  def userid = Action { implicit request =>
    tempForm.bindFromRequest.fold(
      formWithErrors => {
        println(tempForm)
        BadRequest(views.html.index(formWithErrors))
      },
      userid => {
        Redirect(routes.SpreadsheetController.loadSpreadsheet())
          .withSession(request.session + (UserName -> userid))
      }
    )
  }

  def leave = Action { implicit request =>
    Redirect(routes.SpreadsheetController.index).withNewSession.flashing("success" -> "See you soon!")
  }

  def socketSpreadsheet = WebSocket.acceptOrResult[JsValue, JsValue] { implicit request =>
    Future.successful(request.session.get(UserName) match {
      case None => Left(Forbidden)
      case Some(uid) =>
        Right(ActorFlow.actorRef(User.props(uid)))
    })
  }
}
