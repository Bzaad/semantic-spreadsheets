package controllers

import javax.inject._
import play.api.mvc.{Controller, Action}
import play.api.libs.json._
import scala.collection.mutable.ArrayBuffer	
import scala.collection.immutable.Seq
import models.{triple, DB}
import pdstore._
import pdstore.GUID

class PdStore @Inject() extends Controller {

	var store: PDStore = null
	store = new PDStore("SampleQuery")
	var hasJob = new GUID ()
	var hasParent = new GUID ()
	store.setName(hasJob, "hasJob");
	store.setName(hasParent, "hasParent")

	store.begin
	store.addLink("James", hasJob, "Programmer")
	store.addLink("Max", hasJob, "Programmer")
	store.addLink("Beth", hasJob, "Waiter")
	store.addLink("Sue", hasParent, "James")
	store.addLink("Mike", hasParent, "James")
	store.addLink("Bethany", hasParent, "Fritz")
	store.commit
	
	def add(tObject: String, tPredicate: String, tSubject: String) = Action{
		store.begin
		store.addLink(tObject, hasJob, tSubject)
		store.commit
		Ok(Json.obj("result" -> triple(tObject, tPredicate, tSubject)))
	}

	def remove(tObject: String, tPredicate: String, tSubject: String) = Action{
		Ok("the triple was removed!")
	}

	def query(qObject: String, qPredicate: String, qSubject: String)= Action {
		var qGuid = new GUID ()
		var result = ArrayBuffer[String]()

		if(qPredicate == "hasJob")
			qGuid = hasJob
		if(qPredicate == "hasParent")
			qGuid = hasParent

		if(qObject == "_"){
			for ( i <- 0 to (store.query((v"x", qGuid, qSubject)).toList.length - 1)) {
				result += store.query((v"x", qGuid, qSubject)).toList(i).get(v"x").toString
			}
		}
		if(qSubject == "_"){
			for ( i <- 0 to (store.query((qObject, qGuid, v"x")).toList.length - 1)){
				result += store.query((qObject, qGuid, v"x")).toList(i).get(v"x").toString
			}
		}
		//Ok(Json.obj(qObject -> triple(qObject, qPredicate, result.toString)))
		Ok(Json.toJson(result))
	}
}