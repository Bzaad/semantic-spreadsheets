package actors

import play.api.libs.json._

case class Triple(tSubject: String, tPredicate: String, tObject: String)

object Triple {
	implicit val tripleWrites = new Writes[Triple] {
		def writes(triple: Triple): JsValue = {
			Json.obj(
				"subject" -> triple.tSubject,
				"predicate" -> triple.tPredicate,
				"object" -> triple.tObject
			)
		}
	}
}