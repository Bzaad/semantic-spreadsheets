package actors

import akka.actor._
import play.api.libs.json.JsValue
import play.api.Logger

/**
  * Created by behzadfarokhi on 20/07/17.
  */

object User {
  def props(user: String)(out: ActorRef) = Props(new User(user, out))
}

class User(uid: String, out: ActorRef) extends Actor with ActorLogging {

  override def preStart(): Unit = {
    UserManager.addUser(out)
    Logger.debug(s"User actor $uid with actor reference $out has started!")
    // TODO: add the actor to list of all available actors
  }

  override def postStop(): Unit = {

    UserManager.removeUser(out)
    Logger.debug(s"User actor $uid with actor reference $out has stopped!" )
  }

  override def receive: Receive = {
    case msg: JsValue =>
      UserManager.sendToAll()
  }
}