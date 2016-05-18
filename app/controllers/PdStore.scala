package controllers

import javax.inject._
import java.security.MessageDigest
import play.api.mvc.{Controller, Action}
import play.api.libs.json._
import scala.collection.mutable.ArrayBuffer	
import scala.collection.immutable.Seq
import models.{triple, DB}
import pdstore._
import pdstore.GUID

class PdStore @Inject() extends Controller {

	var store = new PDStore("fileName")
	store.begin

	def computeGuid (tGuid: String) : GUID = {
		return new GUID (MessageDigest.getInstance("MD5").digest(tGuid.getBytes));
	}

	def add(tObject: String, tPredicate: String, tSubject: String) = Action{
		store.addLink(tObject, computeGuid(tPredicate), tSubject)
		store.setName(computeGuid(tPredicate), tPredicate)
		store.commit
		Ok(Json.obj("result" -> triple(tObject, tPredicate, tSubject)))
	}

	def remove(tObject: String, tPredicate: String, tSubject: String) = Action{
		Ok("the triple was removed!")
	}

	def query(qObject: String, qPredicate: String, qSubject: String)= Action {
		var qGuid = computeGuid(qPredicate);
		var result = ArrayBuffer[String]() 
		var length = 0
		if(qObject == "_"){
			val results = store.query((v"x", qGuid, qSubject))
			while(results.hasNext) {
				result += results.next.get(v"x").toString
			}
		}

		if(qSubject == "_"){
			val results = store.query((qObject, qGuid, v"x"))
			while (results.hasNext) {
				result += results.next.get(v"x").toString
			}
		}
		//Ok(Json.obj(qObject -> triple(qObject, qPredicate, result.toString)))
		Ok(Json.toJson(result))
	}
}