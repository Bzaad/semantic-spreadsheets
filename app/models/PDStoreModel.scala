package models

import pdstore._
import PDStore._
import akka.actor.{ActorRef, ActorSystem}

import scala.concurrent.duration._
import play.api.Logger
import play.api.libs.json.Json

import scala.util.control.Breaks._
// import scala.collection.mutable
import scala.collection.mutable.{ArrayBuffer, ListBuffer, Set, HashMap, MultiMap}

case class PDStoreModel()

object PDStoreModel {

  val store = PDStore("pdstore_dd")
  val system = ActorSystem("aSystem")
  import system.dispatcher

  var registeredListeners = new ListBuffer[LTriple]()

  var listeningActors = HashMap.empty[ActorRef, Set[LTriple]]

  var sameTableHash = HashMap.empty[String, Set[ActorRef]]

  var actorsAndTheirTriples = new HashMap[ActorRef, List[PdChangeJson]]

  var currentListenerList = Set.empty[PdChangeJson]


  def query(pdObj: PdObj): PdQuery = {
    var queryResult = ArrayBuffer.empty[PdChangeJson]

    for (p <- pdObj.pdChangeList) {
      if (p.sub == "?" && p.obj != "?") {
        val qResult = store.query((v"x", store.getGUIDwithName(p.pred), store.getGUIDwithName(p.obj)))

        while (qResult.hasNext) {
          val t = store.begin
          val queriedSub = qResult.next().get(v"x")
          val result = PdChangeJson("ts", "e", store.getName(queriedSub), p.pred, p.obj)
          queryResult += result
          store.commit
        }
      } else if (p.sub != "?" && p.obj == "?") {
        val qResult = store.query((store.getGUIDwithName(p.sub), store.getGUIDwithName(p.pred), v"x"))
        while (qResult.hasNext) {
          val t = store.begin
          val queriedObj = qResult.next().get(v"x").toString
          val result = new PdChangeJson("ts", "e", p.sub, p.pred, queriedObj)
          val lTriple = "?_" + p.pred + "_" + queriedObj
          actors.UserManager.addToListeners(lTriple, pdObj.actor, result)

          queryResult += result
        }
      } else {
        Logger.error("not a valid query")
      }
    }

    if (queryResult.nonEmpty) {
      PdQuery("success", false, queryResult.toList)
    } else {
      PdQuery("success", false, List[PdChangeJson]())
    }
  }

  def createTable(p: PdObj): PdQuery = {
    for (t <- p.pdChangeList) {
      if (t.pred == "has_type" && t.obj == "table") {
        store.addLink(store.getGUIDwithName(t.sub), store.getGUIDwithName(t.pred), store.getGUIDwithName(t.obj))
        store.addLink(store.getGUIDwithName(t.sub), store.getGUIDwithName("has_row"), store.getGUIDwithName(""))
        store.addLink(store.getGUIDwithName(t.sub), store.getGUIDwithName("has_column"), store.getGUIDwithName(""))
      }
    }
    store.commit
    PdQuery("success", false, p.pdChangeList)
  }

  /*
  TODO: this can be combined with create table method
  simply called manageTable or something!
   */
  def removeTable(p: PdObj): PdQuery = ???

  var tableListeners = ArrayBuffer[String]()

  def queryTable(p: PdObj): PdQuery = {
    var queryResult = ListBuffer.empty[PdChangeJson]

    //FIXME: this is causing problems!
    //TODO: we still need to have the table name in the returned data, perhaps a mechanism in the front-end that doesn't think we are creating a new table!
    //TODO: perhaps instead of success and handle success we can break the message header categories down to more specific titles
    //queryResult += p.pdChangeList(0)
    

    for (t <- p.pdChangeList) {
      if (t.pred == "has_type" && t.obj == "table") {
        val rows = store.query((store.getGUIDwithName(t.sub), store.getGUIDwithName("has_row"), v"row"), (v"row", store.getGUIDwithName("has_value"), v"value")).toList
        val columns = store.query((store.getGUIDwithName(t.sub), store.getGUIDwithName("has_column"), v"column"), (v"column", store.getGUIDwithName("has_value"), v"value")).toList

        for (r <- rows){
          queryResult += new PdChangeJson("ts", "e", p.pdChangeList(0).sub, "has_row", store.getName(r.get(v"row")))
          queryResult += new PdChangeJson("ts", "e", store.getName(r.get(v"row")), "has_value", store.getName(r.get(v"value")))
        }
        for (c <- columns) {
          queryResult += new PdChangeJson("ts", "e", p.pdChangeList(0).sub, "has_column", store.getName(c.get(v"column")))
          queryResult += new PdChangeJson("ts", "e", store.getName(c.get(v"column")), "has_value", store.getName(c.get(v"value")))
        }
        for(r <- rows){
          for(c <- columns){
            val rowName = store.getGUIDwithName(store.getName(r.get(v"value")))
            val colName = store.getGUIDwithName(store.getName(c.get(v"value")))

            //registering listeners for all the possible row-column combinations
            registerListener(new PdChangeJson("ts", "e", store.getName(rowName), store.getName(colName), "_"), p.actor)

            val cellVal = store.query((rowName, colName, v"x"))
            for(cv <- cellVal){
              queryResult += new PdChangeJson("ts", "e", store.getName(rowName) , store.getName(colName), cv.get(v"x").toString)
            }
          }
        }
      }
    }
    PdQuery("success", false, queryResult.toList)
  }

  def registerListener2(pdChange: PdChangeJson): Unit ={
    store.listen((null, ChangeType.WILDCARD, store.getGUIDwithName(pdChange.sub), store.getGUIDwithName(pdChange.pred), null), (c: Change) => {
      system.scheduler.scheduleOnce(1 millisecond){
        /*
        for (a <- actorsAndTheirTriples){
          if (a._2.exists(p => store.getName(c.instance1).equals(p.sub) && store.getName(c.role2).equals(p.pred))){
            //Logger.debug(c.toRawString)
            //Logger.debug(store.getName(c.instance1) + " :: " + store.getName(c.role2) + " :: " + store.getName(c.instance2))

            //a._1 ! PdChangeJson("_" , "e", store.getName(c.instance1), store.getName(c.role2), store.getName(c.instance2))
          }
        }
        */
      }
    })
  }

  def applyPdc(pdc: PdObj): PdQuery = {
    var recievingActors = Set.empty[ActorRef]
    var tableName = ""
    for (c <- pdc.pdChangeList) {
      if(c.ch == "+" && (c.pred == "has_row" || c.pred == "has_value" || c.pred == "has_column")){
        store.addLink(store.getGUIDwithName(c.sub), store.getGUIDwithName(c.pred), store.getGUIDwithName(c.obj))

      }
      else if (c.ch == "-" && (c.pred == "has_row" || c.pred == "has_value" || c.pred == "has_column")){
        store.removeLink(store.getGUIDwithName(c.sub), store.getGUIDwithName(c.pred), store.getGUIDwithName(c.obj))
        if(!c.pred.equals("has_value")) tableName = c.sub
      }
      else if (c.ch == "+" && (c.pred != "has_row" || c.pred != "has_value" || c.pred != "has_column")){
        store.addLink(store.getGUIDwithName(c.sub), store.getGUIDwithName(c.pred), c.obj)
      }
      else if (c.ch == "-" && (c.pred != "has_row" || c.pred != "has_value" || c.pred != "has_column")){
        store.removeLink(store.getGUIDwithName(c.sub), store.getGUIDwithName(c.pred), c.obj)
      }
    }
    store.commit

    /*
    if(pdc.pdChangeList.exists(x => "has_row".equals(x.pred) || "has_column".equals(x.pred))){
      for (a <- actorsAndTheirTriples){

        if (actorsAndTheirTriples.entryExists(a._1, _.sub.equals(tableName))){
          a._2 ::: pdc.pdChangeList
        }

      }
    }
    */


    for (p <- pdc.pdChangeList){
      if ("has_row".equals(p.pred) || "has_column".equals(p.pred)){

        for (a <- actorsAndTheirTriples){
          if (a._2.exists(x => p.sub.equals(x.sub))){
            recievingActors += a._1
            actorsAndTheirTriples(a._1) = actorsAndTheirTriples(a._1) ::: pdc.pdChangeList
          }
        }
        // first: update listenerList for all the actors who are on the same table (if it's remove remove otherwise add)
        // second: send a message too all of them.
        // third: look for any other actor who has the pattern in their listener list and send them a copy
      }
    }
    PdQuery("success", false, pdc.pdChangeList)
  }

  def tableListenerUpdate(pdObj: PdObj, actor: ActorRef): Unit ={
    if(actorsAndTheirTriples.keySet.exists(_ == pdObj.actor))
      actorsAndTheirTriples.remove(pdObj.actor)
    actorsAndTheirTriples += (pdObj.actor -> pdObj.pdChangeList)
    for (u <- actorsAndTheirTriples){
      if (!u._1.equals(pdObj.actor) && u._2.exists( p => pdObj.pdChangeList.filter(s => "has_type".equals(s.pred) && "table".equals(s.obj))(0).sub.equals(p.sub))){
        val theDifference = u._2.filterNot(pdObj.pdChangeList.toSet)
        //actor ! Json.toJson(PdQuery("listener", true, theDifference))
        actorsAndTheirTriples(actor) ::: theDifference
      }
    }
    for(p <- pdObj.pdChangeList){
      if (!currentListenerList.exists(x => p.sub.equals(x.sub) && p.pred.equals(x.pred))){
        //registerListener2(p)
        currentListenerList += p
      }
    }
  }






  def registerListener(pdc: PdChangeJson, actor: ActorRef): Unit = {
    Logger.error(pdc.toString)

    if ((pdc.pred.equals("has_row") || pdc.pred.equals("has_column"))){
      // becaues each actor can only be registered to only one table at a time,
      // remove them from the rest of it!

      for (s <- sameTableHash){
        if (s._2.contains(actor)){
          s._2 -= actor
        }
      }

      // add it back again to the new table
      if (sameTableHash.exists(p => p._1.equals(pdc.sub))) {
        sameTableHash(pdc.sub) += actor
      } else {
        sameTableHash += (pdc.sub -> Set(actor))
      }

    }

    /**
      * (t,e,table1,has_row,?)
      * (t,e,table1,has_column,?)
      * (t,+,table1,has_row,table1_A2)
      * (t,+,table1_A2,has_value,p1)
      * (t,+,table1,has_column,table1_B1)
      * (t,+,table1_B1,has_value,firstName)
      * (t,+,p1,firstName,behzad)
      */



    var lTriple = new LTriple(pdc.sub.toString, pdc.pred.toString, "_")

    if(listeningActors.contains(actor)){
      if (!listeningActors(actor).exists(x => x.lSub.toString.equals(lTriple.lSub.toString) && x.lPred.toString.equals(lTriple.lPred.toString)))
        listeningActors(actor) += lTriple
    }else{
      listeningActors += actor -> Set[LTriple](lTriple)
    }


    if(!registeredListeners.exists( x => x.lSub.toString.equals(lTriple.lSub.toString) && x.lPred.toString.equals(lTriple.lPred.toString))){
      registeredListeners += lTriple
      store.listen((null, ChangeType.WILDCARD, store.getGUIDwithName(lTriple.lSub.toString), store.getGUIDwithName(lTriple.lPred.toString), null), (c: Change) => {
        system.scheduler.scheduleOnce(1 millisecond){
          // TODO: Address all the following conditions:
          /**
            * 1 - a row or column does not exist and we add a value to it
            * (transaction=Role GUID(0xd97181cb54753d87L, 0x35acb1ff817d91L), LINK_ADDED, instance1=Role "table_3_A2", role2=Role "has_value", instance2=Role "P1")
            * (transaction=Role GUID(0xd97181cb54753d87L, 0x35acb1ff817d91L), LINK_ADDED, instance1=Role "table_3", role2=Role "has_row", instance2=Role "table_3_A2")
            *
            * 2 - a row or column does exist we remove value from it
            * (transaction=Role GUID(0xd97181cb54753d87L, 0x35acb25f602f41L), LINK_REMOVED, instance1=Role "table_3_A2", role2=Role "has_value", instance2=Role "P2")
            * (transaction=Role GUID(0xd97181cb54753d87L, 0x35acb25f602f41L), LINK_REMOVED, instance1=Role "table_3", role2=Role "has_row", instance2=Role "table_3_A2")
            *
            * 3 - a row or column exists and we change its value
            * (transaction=Role GUID(0xd97181cb54753d87L, 0x35acb23a7bc1d1L), LINK_REMOVED, instance1=Role "table_3_A2", role2=Role "has_value", instance2=Role "P1")
            * (transaction=Role GUID(0xd97181cb54753d87L, 0x35acb23a7bc1d1L), LINK_ADDED, instance1=Role "table_3_A2", role2=Role "has_value", instance2=Role "P2")
            */
        }
      })
    }
  }

  def isString(cls: AnyRef): Boolean ={
    cls match {
      case s: String => true
      case _  => false
    }
  }
}

/*
          if(store.getName(c.role2) == "has_row" || store.getName(c.role2) == "has_column"){
            Logger.debug(c.toRawString)
            for(la <- listeningActors){
              if (la._2.exists(x => x.lSub.toString.equals(store.getName(c.instance1)) && x.lPred.toString.equals(store.getName(c.role2)))){
                //TODO: We need to add all the clients who are listeneing to this to listen to the new Triple changes as well.
                Logger.error(la._1.toString)
                if(c.changeType == ChangeType.LINK_ADDED){
                  //TODO: Add it to the listening actors list
                }else if (c.changeType == ChangeType.LINK_NOW_REMOVED){
                  //TODO: Remove it from the listening actors list
                }
              }
            }
          }else{
            val theChange = new PdChangeJson(
              "ta",                                                                            //timestamp
              if (c.getChangeType.toString == "LINK_ADDED") "+" else  "-",                     //change type
              store.getName(c.instance1),                                                      //subject
              store.getName(c.role2),                                                          //predicate
              if (isString(c.instance2)) c.instance2.toString else store.getName(c.instance2)) //object

            var recievingActors : Set[ActorRef] = Set()

            for(la <- listeningActors){
              if (la._2.exists(x => x.lSub.toString.equals(theChange.sub) && x.lPred.toString.equals(theChange.pred) /*&& !la._1.equals(actor)*/)){
                recievingActors += la._1
              }
            }
            actors.UserManager.updateListeningActors(theChange, recievingActors)
          }
 */
