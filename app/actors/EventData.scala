package actors

import play.api.libs.json.{JsValue, Writes, Json}

/**
  * Created by behzadfarokhi on 16/01/17.
  */

case class EventData(header: String, user: String, changes: JsValue, created: java.util.Date)

// an event data Json Object for each message

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