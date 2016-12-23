package core.db.clients

import base.TestSuite
import org.mockito.Mock
import restapi.http.entities.ClientInformation

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
    connectedClientsStore.update("user-id-x1", mockClientInfo1)

    val result = connectedClientsStore.isOnline("user-id-x1")

    assert(result)
  }

  test("client information is absent, user is online") {
    val result = connectedClientsStore.isOnline("user-id-x1")

    assert(!result)
  }

  test("adding two clients, searching for first, found") {
    connectedClientsStore.update("user-id-x1", mockClientInfo1)
    connectedClientsStore.update("user-id-x2", mockClientInfo2)

    val result = connectedClientsStore.find("user-id-x1")

    assert(result.isDefined)
    assertResult(mockClientInfo1)(result.get)
  }

  test("adding two clients, removing first, searching for first, absent") {
    connectedClientsStore.update("user-id-x1", mockClientInfo1)
    connectedClientsStore.update("user-id-x2", mockClientInfo2)
    connectedClientsStore.remove("user-id-x1")

    val result = connectedClientsStore.find("user-id-x1")

    assert(result.isEmpty)
  }

  test("adding two clients, removing first, searching for second, found") {
    connectedClientsStore.update("user-id-x1", mockClientInfo1)
    connectedClientsStore.update("user-id-x2", mockClientInfo2)
    connectedClientsStore.remove("user-id-x1")

    val result = connectedClientsStore.find("user-id-x2")

    assert(result.isDefined)
    assertResult(mockClientInfo2)(result.get)
  }

  test("updating existing client info, newer is saved") {
    connectedClientsStore.update("user-id-x1", mockClientInfo1)
    connectedClientsStore.update("user-id-x1", mockClientInfo2)

    val result = connectedClientsStore.find("user-id-x1")

    assert(result.isDefined)
    assertResult(mockClientInfo2)(result.get)
  }
}
