package models

import play.api.libs.json._
import play.api.libs.functional.syntax._

object triple {
	implicit val tripleWrites: Writes[triple] = (
		(JsPath \ "tObject").write[String] and
		(JsPath \ "tPredicate").write[String] and 
		(JsPath \ "tSubject").write[String]
	)(unlift(triple.unapply))
}

case class triple(tObject: String, tPredicate: String, tSubject: String)