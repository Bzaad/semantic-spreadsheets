package controllers

/**
  * Created by behzadfarokhi on 27/07/17.
  */

import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.streams.ActorFlow
import javax.inject._

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.stream.Materializer

@Singleton
class SpreadsheetController @Inject() (components: ControllerComponents) (implicit system: ActorSystem, mat: Materializer)extends AbstractController (components) {

  val User = "user"
  val tempForm = Form(single("userid" -> nonEmptyText))

  def index = Action { implicit request =>
    request.session.get(User).map { user =>
      Redirect(routes.SpreadsheetController.loadSpreadsheet()).flashing("info" -> s"Redirected to spreadsheet as $user user!")
    }getOrElse(Ok(views.html.index(tempForm)))
  }


  def loadSpreadsheet = Action { implicit request =>
    request.session.get(User).map { user =>
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
          .withSession(request.session + (User -> userid))
      }
    )
  }

  def leave = Action { implicit request =>
    Redirect(routes.SpreadsheetController.index).withNewSession.flashing("success" -> "See you soon!")
  }

  def socketSpreadsheet = WebSocket.accept[String, String] { request =>
    ActorFlow.actorRef { out =>
      UserActor.props(out)
    }
  }
}

object UserActor {
  def props(out: ActorRef) = Props(new UserActor(out))
}

class UserActor(out: ActorRef) extends Actor with ActorLogging {
  override def receive: Receive = {
    case msg: String =>
      out ! ("something something something" + out)
  }
}

