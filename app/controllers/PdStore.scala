package controllers

import javax.inject.Inject
import java.security.MessageDigest

import play.api.mvc._
import play.api.libs.ws._
import play.api.libs.json._
import play.api.libs.iteratee._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.functional.syntax._

import scala.collection.mutable.ArrayBuffer
import models.{DB, triple}
import pdstore._
import pdstore.GUID

import scala.util.parsing.json.JSONObject

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

	def remove(tObject: String, tPredicate: String, tSubject: String) = Action {
		Ok("the triple was removed!")
	}

	def query(qObject: String, qPredicate: String, qSubject: String)= Action {
		var qGuid = computeGuid(qPredicate);
		var result = ArrayBuffer[String]()
		var returnObj = new triple(qObject, qPredicate, qSubject)
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
		
		Ok(Json.obj(qObject -> triple(qObject, qPredicate, Json.toJson(result).toString)))
	}

	case class jsonTriple(sbj: String, pred: String, Obj: String)
	case class triples(singleTriple: jsonTriple)

	object triples {
		var list: List[jsonTriple] = {
			List(
				jsonTriple("behzad", "hasCar", "Toyota"),
				jsonTriple("behzad", "hasFamilyName", "farokhi")
			)
		}
	}

	implicit val jsonTripleWrites: Writes[jsonTriple] = (
		(JsPath \ "sbj").write[String] and
			(JsPath \ "pred").write[String] and
			(JsPath \ "obj").write[String]
	)(unlift( jsonTriple.unapply ))

	def getJsonTest = Action {
		val json = Json.toJson(triples.list)
		Ok(json)
	}

	/**
		* A simple method that reads the Json object from the request body
		* and echoes it back as a response
		* @return
		*/
	def echoJson = Action(parse.json) { request =>
		val json = Json.toJson(request.body)
		Ok(json)
		/**
		(request.body \ "name").asOpt[String].map { name =>
			Ok("Hello " + name)
		}.getOrElse {
			BadRequest("Missing Parameter [name]")
		}
			*/
	}

	def stream = WebSocket.using[String] { request =>
		val (out, channel) = Concurrent.broadcast[String]
		val in = Iteratee.foreach[String] { msg =>
			channel push("message: " + msg)
			if(msg == "end"){
				channel push("closing the websocket!");
				channel.eofAndEnd();
			}
		}
		(in, out)
	}

	def someFunction(someParam: String) = TODO 

	def create = TODO
}