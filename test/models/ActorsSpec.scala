package models

import akka.actor.ActorSystem
import akka.testkit.{TestKit, TestProbe}
import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import play.api.inject._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers._


/**
  * Created by behzadfarokhi on 28/06/17.
  */
class ActorsSpec extends Specification {

  "User Actor" should {
    val actorSystem = ActorSystem("test")

    class Actors extends TestKit(actorSystem) with Scope

    val app = new GuiceApplicationBuilder()
      .overrides(bind[ActorSystem].toInstance(actorSystem))
      .build()

      "reply with empty reading if no data exists" in new Actors {
        running(app) {
          val probe = TestProbe()
          val userActor = actorSystem.actorOf(User.props("group", "user"))

          userActor.tell(User.ReadData(requestId = 42), probe.ref)
          val response = probe.expectMsgType[User.RespondData]
          response.requestId should ===(42)
          response.value should ===(None)
        }
      }

    }
}