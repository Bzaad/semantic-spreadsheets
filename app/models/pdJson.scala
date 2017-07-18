package models

/**
  * Created by behzadfarokhi on 13/07/17.
  */

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

/**
  * PdChange JSON Object at it's most basic level.
  * Each transaction contains at least one PdChagne
  * Json object regardless of query type and operation
  * @param ta: timestamp
  * @param ch: change type:
  *           "+": Add quintuple to PdStore
  *           "-": Remove quintuple from PdStore
  *           "e": Check the existance of quintuple in the store
  * @param sub: Subject
  * @param pred: Predicate
  * @param obj: Object
  */

case class PdJson(ta: String, ch: String, sub: String, pred: String, obj: String )
object PdJson {

  implicit val pdChangeReads: Reads[PdJson] = (
    (JsPath \ "ta").read[String] and
      (JsPath \ "ch").read[String] and
      (JsPath \ "sub").read[String] and
      (JsPath \ "pred").read[String] and
      (JsPath \ "obj").read[String]
    )(PdJson.apply _)

  implicit val pdChangeWrites: Writes[PdJson] = (
    (JsPath \ "ta").write[String] and
      (JsPath \ "ch").write[String] and
      (JsPath \ "sub").write[String] and
      (JsPath \ "pred").write[String] and
      (JsPath \ "obj").write[String]
  )(unlift(PdJson.unapply))

}

/**
  * QueryMessage object contains a messag type and a sequence of PdChange objects
  * the message type will determine the necessary operation on the reqValue
  * @param reqType: messageType:
  *               "change" : would start a write operation
  *               "query" : would start a read operation
  * @param reqValue: a sequence of one or more pdChange objects
  */
case class QueryMessage(reqType: String, reqValue: Seq[PdJson])
object QueryMessage {

  implicit val queryMessageReads: Reads[QueryMessage] = (
    (JsPath \ "reqType").read[String] and
    (JsPath \ "reqValue").read[Seq[PdJson]]
  )(QueryMessage.apply _)

  implicit val queryMessageWrites: Writes[QueryMessage] = (
    (JsPath \ "reqType").write[String] and
    (JsPath \ "reqValue").write[Seq[PdJson]]
  )(unlift(QueryMessage.unapply))


}

