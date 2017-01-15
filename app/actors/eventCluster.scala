package actors

/**
  * Created by behzadfarokhi on 16/01/17.
  */

import akka.actor._
import akka.cluster.Cluster
import akka.cluster.ClusterEvent.{InitialStateAsEvents, MemberEvent, MemberRemoved, MemberUp, UnreachableMember}
import akka.event.LoggingReceive

class EventCluster extends Actor with ActorLogging {
  val cluster = Cluster(context.system)

  override def preStart(): Unit = {
    cluster.subscribe(self, initialStateMode = InitialStateAsEvents, classOf[MemberEvent], classOf[UnreachableMember])
  }

  override def postStop(): Unit = cluster.unsubscribe(self)

  def receive = LoggingReceive {

    case MemberUp(member) =>
      log.info(s"Member is up: ${member.address}")

    case UnreachableMember(member) =>
      log.info(s"Member detected as unreachable: $member")

    case MemberRemoved(member, previousStatus) =>
      log.info(s"Member is removed: ${member.address} after $previousStatus")

    case _: MemberEvent => // ignore
  }
}
