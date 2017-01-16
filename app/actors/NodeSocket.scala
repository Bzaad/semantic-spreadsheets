package actors

import scala.concurrent.duration._
import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.cluster.Cluster
import akka.cluster.ddata.Replicator._
import akka.cluster.ddata.{DistributedData, GSetKey, LWWRegisterKey}
import akka.event.LoggingReceive
import play.api.libs.json._

/**
  * Created by behzadfarokhi on 16/01/17.
  */
object NodeSocket {

  def props(user: String)(out: ActorRef) = Props(new NodeSocket(user, out))
  def headerMsgKey(header: String) = LWWRegisterKey[EventData](header + "-lwwreg")

  case class Message(header: String, msg: JsValue)

  object Message {
    implicit val messageReads = Json.reads[Message]
  }

  case class HeaderNameMessage(headerName: String)
  object HeaderNameMessage {
    implicit val headerNameMessageWrites = new Writes[HeaderNameMessage] {
      def writes(headerNameMessage: HeaderNameMessage): JsValue = {
        Json.obj(
          "type" -> "headerName",
          "headerName" -> headerNameMessage.headerName,
          "headerId" -> headerNameMessage.headerName.hashCode
        )
      }
    }
  }

  case class HeadersListMessage(headers: Seq[String])
  object HeadersListMessage {
    implicit val headersListMessageWrites = new Writes[HeadersListMessage] {
      def writes(headersListMessage: HeadersListMessage): JsValue = {
        Json.obj(
          "type" -> "headers",
          "type" -> JsArray(headersListMessage.headers.map(JsString(_)))
        )
      }
    }
  }
}

class NodeSocket(uid: String, actorRef: ActorRef) extends Actor with ActorLogging {

  val headersKey = GSetKey[String]("headers")
  var lastSubscribed: Option[String] = None
  var initialHistory: Option[Set[EventData]] = None

  val replicator = DistributedData(context.system).replicator
  implicit val node = Cluster(context.system)

  replicator ! Get(headersKey, ReadMajority(timeout = 5.seconds))

  def receive = LoggingReceive {
    case g @ GetSuccess(key, reg) if key == headersKey => ???
    case NotFound(_, _) => ???
    case GetFailure(key, req) if key == headersKey => ???
  }

  def afterKeys = LoggingReceive{
    case c @ Changed(key) if key == headersKey => ???
    case c @ Changed(LWWRegisterKey(header)) => ???
    case g @ GetSuccess(GSetKey(key), req) => ???
    case g @ NotFound(GSetKey(key), req) => ???
    case g @ GetFailure(GSetKey(key), req) => ???
    case JsString(keyName) => ???
    case js: JsValue =>
      ((js \ "type").as[String]) match {
        case "subscribe" => ???
        case "message" => ???
      }
  }
}
