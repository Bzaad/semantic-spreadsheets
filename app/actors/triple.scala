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