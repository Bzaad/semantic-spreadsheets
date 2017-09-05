package models

import akka.actor.ActorRef
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
case class PdQuery(reqType: String, listenTo: Boolean, reqValue: List[PdChangeJson])

object PdQuery {

  implicit val queryMessageReads: Reads[PdQuery] = (
    (JsPath \ "reqType").read[String] and
      (JsPath \ "listenTo").read[Boolean] and
      (JsPath \ "reqValue").read[List[PdChangeJson]]
    )(PdQuery.apply _)

  implicit val queryMessageWrites: Writes[PdQuery] = (
    (JsPath \ "reqType").write[String] and
      (JsPath \ "listenTo").write[Boolean] and
      (JsPath \ "reqValue").write[List[PdChangeJson]]
    )(unlift(PdQuery.unapply))

}

trait QBundle {
  val pdChangeList: List[PdChangeJson]
  val listenTo: Boolean
  val actor: ActorRef
}

class PdObj (val pdChangeList: List[PdChangeJson], val actor: ActorRef, val listenTo: Boolean) extends QBundle

trait SimpleTriple {
  val lSub: Any
  val lPred: Any
  val lObj: Any
}

class LTriple (val lSub: Any, val lPred: Any, val lObj: Any) extends SimpleTriple