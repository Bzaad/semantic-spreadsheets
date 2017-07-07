package models

import akka.actor.ActorSystem
import akka.testkit.{TestKit, TestProbe}
import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import play.api.inject._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers._
import play.api.test.WithApplication
import scala.concurrent.duration._

/**
  * Created by behzadfarokhi on 28/06/17.
  */
class ActorsSpec extends Specification {

  "User Actor" should {
    implicit val system = ActorSystem("test")

    class Actors extends TestKit(system) with Scope

    val app = new GuiceApplicationBuilder()
      .overrides(bind[ActorSystem].toInstance(system))
      .build()

      "reply with empty reading if no data exists" in new WithApplication {
        running(app) {
          val probe = TestProbe()
          val userActor = system.actorOf(User.props("group", "user"))

          userActor.tell(User.ReadData(requestId = 42), probe.ref)
          val response = probe.expectMsgType[User.RespondData]
          response.requestId should === (42)
          response.value should === (None)
        }
      }

      "reply to registration requests" in new WithApplication{
        running(app) {
          val probe = TestProbe()
          val userActor = system.actorOf(User.props("group", "user"))

          userActor.tell(UserManager.RequestTrackUser("group", "user"), probe.ref)
          probe.expectMsg(UserManager.UserRegistered)
          probe.lastSender should === (userActor)

        }
      }

      "ignore wrong regesteration requests" in new WithApplication{
        running(app) {
          val probe = TestProbe()
          val userActor = system.actorOf(User.props("group", "user"))

          userActor.tell(UserManager.RequestTrackUser("wrongGroup", "user"), probe.ref)
          probe.expectNoMsg(500.milliseconds)

          userActor.tell(UserManager.RequestTrackUser("group", "wrongUser"), probe.ref)
          probe.expectNoMsg(500.millisecond)
        }
      }

      "be able to register a device actor" in new WithApplication {
        running(app){
          val probe = TestProbe()
          val groupActor = system.actorOf(UserGroup.props("group"))

          groupActor.tell(UserManager.RequestTrackUser("group", "user1"), probe.ref)
          probe.expectMsg(UserManager.UserRegistered)
          val userActor1 = probe.lastSender

          groupActor.tell(UserManager.RequestTrackUser("group", "user2"), probe.ref)
          probe.expectMsg(UserManager.UserRegistered)
          val userActor2 = probe.lastSender
          userActor1 should !== (userActor2)

          // Check that the user actors are working
          userActor1.tell(User.RecordData(requestId = 0, value = 1.0), probe.ref)
          probe.expectMsg(User.DataRecorded(requestId = 0))
          userActor2.tell(User.RecordData(requestId = 1, value = 2.0), probe.ref)
          probe.expectMsg(User.DataRecorded(requestId = 1))

        }
      }

      "ignore requests for wrong groupId" in new WithApplication {
        running(app) {
          val probe = TestProbe()
          val groupActor = system.actorOf(UserGroup.props("group"))

          groupActor.tell(UserManager.RequestTrackUser("wrongGroup","user1"), probe.ref)
          probe.expectNoMsg(500.milliseconds)
        }
      }


      "return same actor for same userId" in new WithApplication {
        running(app) {
          val probe = TestProbe()
          val groupActor = system.actorOf(UserGroup.props("group"))

          groupActor.tell(UserManager.RequestTrackUser("group", "user1"), probe.ref)
          probe.expectMsg(UserManager.UserRegistered)
          val userActor1 = probe.lastSender

          groupActor.tell(UserManager.RequestTrackUser("group", "user1"), probe.ref)
          probe.expectMsg(UserManager.UserRegistered)
          val userActor2 = probe.lastSender

          userActor1 should ===(userActor2)
        }
      }

    }
}