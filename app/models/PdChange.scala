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

case class PdChangeJson(ta: String, ch: String, sub: String, pred: String, obj: String )

object PdChangeJson
{

  implicit val pdChangeReads: Reads[PdChangeJson] = (
    (JsPath \ "ta").read[String] and
      (JsPath \ "ch").read[String] and
      (JsPath \ "sub").read[String] and
      (JsPath \ "pred").read[String] and
      (JsPath \ "obj").read[String]
    )(PdChangeJson.apply _)

  implicit val pdChangeWrites: Writes[PdChangeJson] = (
    (JsPath \ "ta").write[String] and
      (JsPath \ "ch").write[String] and
      (JsPath \ "sub").write[String] and
      (JsPath \ "pred").write[String] and
      (JsPath \ "obj").write[String]
  )(unlift(PdChangeJson.unapply))

}

