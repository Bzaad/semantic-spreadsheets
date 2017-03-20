package actors

import play.api.libs.json.{JsValue, Writes, Json}

/**
  * Event data Json Object for the messages sent and received by users
  * Full documentation for how to handle JSON objects in Play Framework:
  * "https://www.playframework.com/documentation/2.5.x/ScalaJson"
  */

case class EventData(header: String, user: String, changes: JsValue, created: java.util.Date)
object EventData {
  implicit val eventDataWrites = new Writes[EventData] {
    def writes(eventData: EventData): JsValue = {
      Json.obj(
        "type" -> "change",
        "header" -> eventData.header,
        "user" -> eventData.user,
        "changes" -> eventData.changes
      )
    }
  }
}