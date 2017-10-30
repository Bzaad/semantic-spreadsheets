package models

import akka.actor.{ActorSystem, PoisonPill}
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
        response.requestId should ===(42)
        response.value should ===(None)
      }
    }

    "reply to registration requests" in new WithApplication {
      running(app) {
        val probe = TestProbe()
        val userActor = system.actorOf(User.props("group", "user"))

        userActor.tell(UserManager.RequestTrackUser("group", "user"), probe.ref)
        probe.expectMsg(UserManager.UserRegistered)
        probe.lastSender should ===(userActor)

      }
    }

    "ignore wrong regesteration requests" in new WithApplication {
      running(app) {
        val probe = TestProbe()
        val userActor = system.actorOf(User.props("group", "user"))

        userActor.tell(UserManager.RequestTrackUser("wrongGroup", "user"), probe.ref)
        probe.expectNoMsg(500.milliseconds)

        userActor.tell(UserManager.RequestTrackUser("group", "wrongUser"), probe.ref)
        probe.expectNoMsg(500.millisecond)
      }
    }

    "be able to register a user actor" in new WithApplication {
      running(app) {
        val probe = TestProbe()
        val groupActor = system.actorOf(UserGroup.props("group"))

        groupActor.tell(UserManager.RequestTrackUser("group", "user1"), probe.ref)
        probe.expectMsg(UserManager.UserRegistered)
        val userActor1 = probe.lastSender

        groupActor.tell(UserManager.RequestTrackUser("group", "user2"), probe.ref)
        probe.expectMsg(UserManager.UserRegistered)
        val userActor2 = probe.lastSender
        userActor1 should !==(userActor2)

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

        groupActor.tell(UserManager.RequestTrackUser("wrongGroup", "user1"), probe.ref)
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

    "be able to list active users" in new WithApplication {
      running(app) {
        val probe = TestProbe()
        val groupActor = system.actorOf(UserGroup.props("group"))

        groupActor.tell(UserManager.RequestTrackUser("group", "user1"), probe.ref)
        probe.expectMsg(UserManager.UserRegistered)

        groupActor.tell(UserManager.RequestTrackUser("group", "user2"), probe.ref)
        probe.expectMsg(UserManager.UserRegistered)

        groupActor.tell(UserGroup.RequestUserList(requestId = 0), probe.ref)
        probe.expectMsg(UserGroup.ReplyUserList(requestId = 0, Set("user1", "user2")))
      }
    }


    "be able to list active users after one shuts down" in new WithApplication {
      running(app) {
        val probe = TestProbe()
        val groupActor = system.actorOf(UserGroup.props("group"))

        groupActor.tell(UserManager.RequestTrackUser("group", "user1"), probe.ref)
        probe.expectMsg(UserManager.UserRegistered)
        val toShutDown = probe.lastSender

        groupActor.tell(UserManager.RequestTrackUser("group", "user2"), probe.ref)
        probe.expectMsg(UserManager.UserRegistered)

        groupActor.tell(UserGroup.RequestUserList(requestId = 0), probe.ref)
        probe.expectMsg(UserGroup.ReplyUserList(requestId = 0, Set("user1", "user2")))

        probe.watch(toShutDown)
        toShutDown ! PoisonPill
        probe.expectTerminated(toShutDown)

        // using awaitAssert to retry because it might take longer for the groupActor
        // to see the Terminated, that order is undefined
        probe.awaitAssert {
          groupActor.tell(UserGroup.RequestUserList(requestId = 1), probe.ref)
          probe.expectMsg(UserGroup.ReplyUserList(requestId = 1, Set("user2")))
        }
      }
    }

    "be able register multiple group actor and shout them down" in new WithApplication {
      running(app) {
        val probe = TestProbe()
        val userGroupActor = system.actorOf(UserManager.props)

        userGroupActor.tell(UserManager.RequestTrackUser("group1", "user"), probe.ref)
        probe.expectMsg(UserManager.UserRegistered)
        val toShutDown1 = probe.lastSender

        userGroupActor.tell(UserManager.RequestTrackUser("group2", "user"), probe.ref)
        probe.expectMsg(UserManager.UserRegistered)
        val toShutDown2 = probe.lastSender

        probe.watch(toShutDown1)
        toShutDown1 ! PoisonPill
        probe.expectTerminated(toShutDown1)

        probe.watch(toShutDown2)
        toShutDown2 ! PoisonPill
        probe.expectTerminated(toShutDown2)
      }
    }

    "return data value for working users" in new WithApplication {
      running(app) {
        val requester = TestProbe()

        val user1 = TestProbe()
        val user2 = TestProbe()

        val queryActor = system.actorOf(UserGroupQuery.props(
          actorToUserId = Map(user1.ref -> "user1", user2.ref -> "user2"),
          requestId = 1,
          requester = requester.ref,
          timeout = 3.seconds
        ))

        user1.expectMsg(User.ReadData(requestId = 0))
        user2.expectMsg(User.ReadData(requestId = 0))

        queryActor.tell(User.RespondData(requestId = 0, Some(1.0)), user1.ref)
        queryActor.tell(User.RespondData(requestId = 0, Some(1.0)), user2.ref)

        requester.expectMsg(UserGroup.RespondAllData(
          requestId = 1,
          theData = Map(
            "user1" -> UserGroup.TheData(1.0),
            "user2" -> UserGroup.TheData(1.0)
          )
        ))
      }
    }

    "return data not available with users with no reading" in new WithApplication {
      running(app){
        val requester = TestProbe()

        val user1 = TestProbe()
        val user2 = TestProbe()

        val queryActor = system.actorOf(UserGroupQuery.props(
          actorToUserId = Map(user1.ref -> "user1", user2.ref -> "user2"),
          requestId = 1,
          requester = requester.ref,
          timeout = 3.seconds
        ))

        user1.expectMsg(User.ReadData(requestId = 0))
        user2.expectMsg(User.ReadData(requestId = 0))

        queryActor.tell(User.RespondData(requestId = 0, None), user1.ref)
        queryActor.tell(User.RespondData(requestId = 0, Some(2.0)), user2.ref)

        requester.expectMsg(UserGroup.RespondAllData(
          requestId = 1,
          theData = Map(
            "user1" -> UserGroup.DataNotAvailable,
            "user2" -> UserGroup.TheData(2.0)
          )
        ))
      }
    }

    "return UserNotAvailable if user stops before answering" in new WithApplication {
      running(app){
        val requester = TestProbe()

        val user1 = TestProbe()
        val user2 = TestProbe()

        val queryActor = system.actorOf(UserGroupQuery.props(
          actorToUserId = Map (user1.ref -> "user1", user2.ref -> "user2"),
          requestId = 1,
          requester = requester.ref,
          timeout = 3.seconds
        ))

        user1.expectMsg(User.ReadData(requestId = 0))
        user2.expectMsg(User.ReadData(requestId = 0))

        queryActor.tell(User.RespondData(requestId = 0, Some(1.0)), user1.ref)
        queryActor.tell(User.RespondData(requestId = 0, Some(2.0)), user2.ref)
        user2.ref ! PoisonPill

        requester.expectMsg(UserGroup.RespondAllData(
          requestId = 1,
          theData = Map (
            "user1" -> UserGroup.TheData(1.0),
            "user2" -> UserGroup.TheData(2.0)
          )
        ))

      }
    }

    "return userTimedOut if user does not answer in time" in new WithApplication {
      running(app){
        val requester = TestProbe()

        val user1 = TestProbe()
        val user2 = TestProbe()

        val queryActor = system.actorOf(UserGroupQuery.props(
          actorToUserId = Map (user1.ref -> "user1", user2.ref -> "user2"),
          requestId = 1,
          requester = requester.ref,
          timeout = 1.seconds
        ))

        user1.expectMsg(User.ReadData(requestId = 0))
        user2.expectMsg(User.ReadData(requestId = 0))

        queryActor.tell(User.RespondData(requestId = 0, Some(1.0)), user1.ref)

        requester.expectMsg(UserGroup.RespondAllData(
          requestId = 1,
          theData = Map (
            "user1" -> UserGroup.TheData(1.0),
            "user2" -> UserGroup.UserTimedOut
          )
        ))

      }
    }

    "be able to collect data from all active users" in new WithApplication {
      running(app) {
        val probe = TestProbe()
        val groupActor = system.actorOf(UserGroup.props("group"))

        groupActor.tell(UserManager.RequestTrackUser("group", "user1"), probe.ref)
        probe.expectMsg(UserManager.UserRegistered)
        val userActor1 = probe.lastSender

        groupActor.tell(UserManager.RequestTrackUser("group", "user2"), probe.ref)
        probe.expectMsg(UserManager.UserRegistered)
        val userActor2 = probe.lastSender

        groupActor.tell(UserManager.RequestTrackUser("group", "user3"), probe.ref)
        probe.expectMsg(UserManager.UserRegistered)
        val userActor3 = probe.lastSender


        // Check that the user actors are working
        userActor1.tell(User.RecordData(requestId = 0, 1.0), probe.ref)
        probe.expectMsg(User.DataRecorded(requestId = 0))
        userActor2.tell(User.RecordData(requestId = 1, 2.0), probe.ref)
        probe.expectMsg(User.DataRecorded(requestId = 1))
        // No data for user3

        groupActor.tell(UserGroup.RequestAllData(requestId = 0), probe.ref)
        probe.expectMsg(
          UserGroup.RespondAllData(
            requestId = 0,
            theData = Map(
              "user1" -> UserGroup.TheData(1.0),
              "user2" -> UserGroup.TheData(2.0),
              "user3" -> UserGroup.DataNotAvailable
            )
          )
        )
      }
    }

  }
}