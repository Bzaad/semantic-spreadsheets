package controllers

import javax.inject._
import play.api.mvc.{Controller, Action}
import play.api.libs.json._
import models.triple
import pdstore._
import pdstore.GUID

class PdStore @Inject() extends Controller {

	var store: PDStore = null
	store = new PDStore("SampleQuery")
	var hasJob = GUID ("hasJob")
	var something = new GUID ()

	store.begin
	store.setName(something, "something")
	store.addLink("bob", hasJob, "builder")
	store.commit
	
	def add(tObject: String, tPredicate: String, tSubject: String) = Action{
		Ok(Json.obj("result" -> triple(tObject, tPredicate, tSubject)))
	}
	def remove(tObject: String, tPredicate: String, tSubject: String) = Action{
		Ok("the triple was removed!")
	}
	def query(theQuery: String)= Action {
		val result = store.query((v"x", hasJob, "builder")).toList
		Ok(Json.obj(theQuery -> triple(result(0).get(v"x").toString, "aPr", "aSub")))
	}
}