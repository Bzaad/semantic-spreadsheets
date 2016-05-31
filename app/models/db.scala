package models

import javax.inject.Inject

import play.api.libs.json.{JsObject, Json}
import play.api.libs.functional.syntax._
import scala.concurrent.{ExecutionContext, Future}
import pdstore._
import pdstore.GUID

trait DB {
	def find()(implicit ec: ExecutionContext): Future[Option[JsObject]]

	//def select(/* we need to have some params here! */)(implicit ec: ExecutionContext): Future[Option[JsObject]]

	//def update(/* we need to have some params here! */)(implicit ec: ExecutionContext): Future[WriteResult]

	//def remove(/* we need to have some params here! */)(implicit ec: ExecutionContext): Future[WriteResult]

	//def save(/* we need to have some params here! */)(implicit ec: ExecutionContext): Future[WriteResult]
}
