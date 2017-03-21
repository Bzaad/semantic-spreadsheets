package actors

import play.api.libs.json._

/**
	* Ok
	* @param ta
	* @param ch
	* @param sub
	* @param pred
	* @param obj
	*/
case class Triple(ta: String, ch: String, sub: String, pred: String, obj: String)
object Triple {
	implicit val tripleWrites = new Writes[Triple] {
		def writes(triple: Triple): JsValue = {
			Json.obj(
				"ta" -> triple.ta,
				"ch" -> triple.ch,
				"sub" -> triple.sub,
				"pred" -> triple.pred,
				"obj" -> triple.obj
			)
		}
	}
}

/**
	* Ok
	* @param changes
	*/
case class QMessage(changes: JsValue)
object QMessage {
	implicit val qmessageWrites = new Writes[QMessage]{
		def writes(qmessage: QMessage): JsValue ={
			Json.obj(
				"changes" -> qmessage.changes
			)
		}
	}
}

/**
	* Ok
	* @param headers
	*/
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

/**
	* Ok
	* @param msgs
	*/
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
