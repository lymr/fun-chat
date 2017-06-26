package core.db.clients

import api.entities.ClientInformation
import base.TestSuite
import core.db.clients.ConnectedClientsStoreSuite._
import core.entities.UserID
import org.mockito.Mock

class ConnectedClientsStoreSuite extends TestSuite {

  @Mock
  var mockClientInfo1: ClientInformation = _

  @Mock
  var mockClientInfo2: ClientInformation = _

  var connectedClientsStore: ConnectedClientsStore = _

  override def beforeEach(): Unit = {
    super.beforeEach()

    connectedClientsStore = new ConnectedClientsStore()
  }

  test("client information is present, user is online") {
    connectedClientsStore.update(USER_ID_1, mockClientInfo1)

    val result = connectedClientsStore.isOnline(USER_ID_1)

    assert(result)
  }

  test("client information is absent, user is online") {
    val result = connectedClientsStore.isOnline(USER_ID_1)

    assert(!result)
  }

  test("adding two clients, searching for first, found") {
    connectedClientsStore.update(USER_ID_1, mockClientInfo1)
    connectedClientsStore.update(USER_ID_2, mockClientInfo2)

    val result = connectedClientsStore.find(USER_ID_1)

    assert(result.isDefined)
    assertResult(mockClientInfo1)(result.get)
  }

  test("adding two clients, removing first, searching for first, absent") {
    connectedClientsStore.update(USER_ID_1, mockClientInfo1)
    connectedClientsStore.update(USER_ID_2, mockClientInfo2)
    connectedClientsStore.remove(USER_ID_1)

    val result = connectedClientsStore.find(USER_ID_1)

    assert(result.isEmpty)
  }

  test("adding two clients, removing first, searching for second, found") {
    connectedClientsStore.update(USER_ID_1, mockClientInfo1)
    connectedClientsStore.update(USER_ID_2, mockClientInfo2)
    connectedClientsStore.remove(USER_ID_1)

    val result = connectedClientsStore.find(USER_ID_2)

    assert(result.isDefined)
    assertResult(mockClientInfo2)(result.get)
  }

  test("updating existing client info, newer is saved") {
    connectedClientsStore.update(USER_ID_1, mockClientInfo1)
    connectedClientsStore.update(USER_ID_1, mockClientInfo2)

    val result = connectedClientsStore.find(USER_ID_1)

    assert(result.isDefined)
    assertResult(mockClientInfo2)(result.get)
  }
}

object ConnectedClientsStoreSuite {
  private val USER_ID_1 = UserID("user-id-x1")
  private val USER_ID_2 = UserID("user-id-x2")
}
