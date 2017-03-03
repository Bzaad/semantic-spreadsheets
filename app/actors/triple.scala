package actors

import play.api.libs.json._

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

case class PDChangeObj(ta: String, ch: String, sub: String, pred: String, obj: String)

object PDChangeObj {
	implicit val pDChangeObj = new Writes[PDChangeObj]{
		def writes(pDChangeObj: PDChangeObj): JsValue = {
			Json.obj(
				"ta" -> pDChangeObj.ta,
				"ch" -> pDChangeObj.ch,
				"sub" -> pDChangeObj.sub,
				"pred" -> pDChangeObj.pred,
				"obj" -> pDChangeObj.obj
			)
		}
	}
}
