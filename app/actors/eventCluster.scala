package actors

/**
  * Created by behzadfarokhi on 16/01/17.
  */

import akka.actor._
import akka.cluster.Cluster
import akka.cluster.ClusterEvent.{InitialStateAsEvents, MemberEvent, MemberRemoved, MemberUp, UnreachableMember}
import akka.event.LoggingReceive

/**
  * creates a cluster membership services that allowes a set of nodes (here users) connect toghether
  * full documentation and examples: "http://doc.akka.io/docs/akka/current/scala/cluster-usage.html"
  */
class EventCluster extends Actor with ActorLogging {
  val cluster = Cluster(context.system)

  /**
    * subscribes a node (here a user) to cluster changes, re-subscribe when restart
    */
  override def preStart(): Unit = {
    cluster.subscribe(self, initialStateMode = InitialStateAsEvents, classOf[MemberEvent], classOf[UnreachableMember])
  }

  /**
    * unsubscribes the node from the cluster
    */
  override def postStop(): Unit = cluster.unsubscribe(self)

  /**
    * logs the member activity (here member refers to each user connected to the system)
    */

  def receive = LoggingReceive {
    /**
      * if the user is up print the user address
      */
    case MemberUp(member) =>
      log.info(s"Member is up: ${member.address}")

    /**
      * if the member is not reachable log the member
       */
    case UnreachableMember(member) =>
      log.info(s"Member detected as unreachable: $member")

    /**
      * if the member signs out then print the previous member and its status
      */
    case MemberRemoved(member, previousStatus) =>
      log.info(s"Member is removed: ${member.address} after $previousStatus")

    /**
      * if there's no member, ignore !
      */
    case _: MemberEvent => // ignore
  }
}
