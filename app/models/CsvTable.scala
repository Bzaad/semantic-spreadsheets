package models

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

case class CsvTable(tableName: String, tableTriples: List[PdChangeJson])

object CsvTable {
  implicit val sTableReads: Reads[CsvTable] = (
    (JsPath \ "tableName").read[String] and
      (JsPath \ "tableTriples").read[List[PdChangeJson]]
    )(CsvTable.apply _)
  implicit val sTableWrites: Writes[CsvTable] = (
    (JsPath \ "tableName").write[String] and
      (JsPath \ "tableTriples").write[List[PdChangeJson]]
    )(unlift(CsvTable.unapply))
}

case class CsvPdQuery(reqType: String, listenTo: Boolean, reqValue: List[CsvTable])
object CsvPdQuery {
  implicit val csvPdQueryReads: Reads[CsvPdQuery] = (
    (JsPath \ "reqType").read[String] and
      (JsPath \ "listenTo").read[Boolean] and
      (JsPath \ "reqValue").read[List[CsvTable]]
  )(CsvPdQuery.apply _)
  implicit val csvPdQueryWrites: Writes[CsvPdQuery] = (
    (JsPath \ "reqType").write[String] and
      (JsPath \ "listenTo").write[Boolean] and
      (JsPath \ "reqValue").write[List[CsvTable]]
  )(unlift(CsvPdQuery.unapply))
}