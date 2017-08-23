package models

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

/**
  * Created by behzadfarokhi on 23/08/17.
  */

/**
  * QueryMessage object contains a messag type and a sequence of PdChange objects
  * the message type will determine the necessary operation on the reqValue
  * @param reqType: messageType:
  *               "change" : would start a write operation
  *               "query" : would start a read operation
  * @param reqValue: a sequence of one or more pdChange objects
  */
case class PdQuery(reqType: String, reqValue: Seq[PdChange])

object PdQuery {

  implicit val queryMessageReads: Reads[PdQuery] = (
    (JsPath \ "reqType").read[String] and
      (JsPath \ "reqValue").read[Seq[PdChange]]
    )(PdQuery.apply _)

  implicit val queryMessageWrites: Writes[PdQuery] = (
    (JsPath \ "reqType").write[String] and
      (JsPath \ "reqValue").write[Seq[PdChange]]
    )(unlift(PdQuery.unapply))

}