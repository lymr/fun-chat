package websocket

import akka.actor.ActorSystem
import akka.testkit.{TestActorRef, TestKit}
import tests.TestSuite

class ConnectedClientsStoreSuite extends TestKit(ActorSystem("connected-clients-store")) with TestSuite {

  private var connectedClientsStore: TestActorRef[ConnectedClientsStore] = _

  override def beforeEach(): Unit = {
    super.beforeEach()

    connectedClientsStore = TestActorRef[ConnectedClientsStore](ConnectedClientsStore.props())
  }

//TODO: Add unit tests
}
