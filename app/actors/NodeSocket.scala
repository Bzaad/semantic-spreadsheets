package actors

/**
  * Created by behzadfarokhi on 16/01/17.
  */

import scala.xml.Utility
import actors.NodeSocket.Message.messageReads
import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.event.LoggingReceive
import play.api.libs.json.{Writes, JsPath, JsValue, JsString, JsObject, JsArray, Json}
import scala.concurrent.duration._
import akka.cluster.Cluster
import akka.cluster.ddata.DistributedData
import akka.cluster.ddata.Replicator._
import akka.cluster.ddata.{GSet, GSetKey, LWWRegister, LWWRegisterKey}

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
          "headers" -> JsArray(headersListMessage.headers.map(JsString(_)))
        )
      }
    }
  }

  case class EventDataListMessage(msgs: Seq[EventData])
  object EventDataListMessage {
    implicit val eventDataListWrites = new Writes[EventDataListMessage] {
      def writes(eventData: EventDataListMessage): JsValue ={
        Json.obj(
          "type" -> "messages",
          "Messages" -> JsArray(eventData.msgs.map(Json.toJson(_)))
        )
      }
    }
  }
}

class NodeSocket(uid: String, out: ActorRef) extends Actor with ActorLogging {

  import NodeSocket._

  val headersKey = GSetKey[String]("headers")
  var lastSubscribed: Option[String] = None
  var initialHistory: Option[Set[EventData]] = None

  var replicator = DistributedData(context.system).replicator
  implicit val node = Cluster(context.system)

  replicator ! Get(headersKey, ReadMajority(timeout = 5.seconds))

  def receive = LoggingReceive {
    case g @ GetSuccess(key, req) if key == headersKey =>
      val data = g.get(headersKey).elements.toSeq
      out ! Json.toJson(HeadersListMessage(data))
      replicator ! Subscribe(headersKey, self)
      context become afterKeys
    case NotFound(_, _) =>
      replicator ! Subscribe(headersKey,  self)
      context become afterKeys
    case GetFailure(key, req) if key == headersKey =>
      replicator ! Get(headersKey, ReadMajority(timeout = 5.seconds))
  }

  def afterKeys = LoggingReceive{
    case c @ Changed(key) if key == headersKey =>
      val data = c.get[GSet[String]](headersKey).elements.toSeq
      out ! Json.toJson(HeadersListMessage(data))
    case c @ Changed(LWWRegisterKey(header)) =>
      for {
        subscribedHeader <- lastSubscribed if (subscribedHeader + "-lwwreg").equals(header)
      } {
        val eventData = c.get(LWWRegisterKey[EventData](header)).value
        initialHistory foreach { historySet =>
          if (!historySet(eventData))
            out ! Json.toJson(eventData)
        }
      }
    case g @ GetSuccess(GSetKey(header), req) =>
      for {
        subscribedHeader <- lastSubscribed if subscribedHeader.equals(header)
      } {
        val elements = g.get(GSetKey[EventData](header)).elements
        initialHistory = Some(elements)
        val data = elements.toSeq.sortWith(_.created.getTime < _.created.getTime)
        out ! Json.toJson(EventDataListMessage(data))
        replicator ! Subscribe(headerMsgKey(header), self)
      }
    case g @ NotFound(GSetKey(header), req) =>
      for {
        subscribedHeader <- lastSubscribed if subscribedHeader.equals(header)
      } {
        initialHistory = Some(Set.empty[EventData])
        replicator ! Subscribe(headerMsgKey(header), self)
      }
    case g @ GetFailure(GSetKey(header), req) =>
      for {
        subscribedHeaders <- lastSubscribed if subscribedHeaders.equals(header)
      } {
        initialHistory = Some(Set.empty[EventData])
        replicator ! Subscribe(key = headerMsgKey(header), self)
      }
    case JsString(headerName) =>
      replicator ! Update(headersKey, GSet.empty[String], WriteLocal) {
        set => set + headerName
      }
      replicator ! FlushChanges
    case js: JsValue =>
      ((js \ "type").as[String]) match {
        case "subscribe" =>
          val header = (js \ "header").as[String]
          if (header != null) {
            lastSubscribed foreach { oldHeader =>
              replicator ! Unsubscribe(headerMsgKey(oldHeader), self)
            }
            lastSubscribed = Some(header)
            replicator ! Get(GSetKey(header), ReadMajority(timeout = 5.seconds))
          }
        case "change" =>
          js.validate[Message](messageReads)
            .map(message => (message.header, message.msg))
            .foreach { case (header, msg) =>
              val eventData = EventData(header, uid, msg, new java.util.Date())
              replicator ! Update(GSetKey[EventData](header), GSet.empty[EventData], WriteLocal) {
                _ + eventData
              }
              replicator ! Update(headerMsgKey(header), LWWRegister[EventData](null), WriteLocal){
                reg => reg.withValue(eventData)
              }
              replicator ! FlushChanges
            }
      }
  }
}
