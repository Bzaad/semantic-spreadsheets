package controllers

import javax.inject._
import play.api.mvc.{Controller, Action}
import play.api.libs.json._
import models.triple
import pdstore.PDStore
import pdstore.GUID

class PdStore @Inject() extends Controller {

	/*
	var store: PDStore = null
	store = new PDStore("SampleQuery" + System.nanoTime);
	*/

	def add(tObject: String, tPredicate: String, tSubject: String) = Action{
		Ok(Json.obj("result" -> triple(tObject, tPredicate, tSubject)))
	}
	def remove(tObject: String, tPredicate: String, tSubject: String) = Action{
		Ok("the triple was removed!")
	}
	def query(theQuery: String)= Action {
		Ok(Json.obj(theQuery -> triple("fakeObject", "fakePredicate", "fakeSubject")))
	}
}