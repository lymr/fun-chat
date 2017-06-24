package authentication

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import authentication.AuthenticatorSuite._
import authentication.entities._
import base.TestSuite
import org.mockito.Mock
import org.scalatest.concurrent.Eventually
import rest.client.RestClient
import rest.client.entities.ExecutionResultCode

import scala.concurrent.{ExecutionContext, Future}

class AuthenticatorSuite extends TestKit(ActorSystem("authenticator-tests")) with TestSuite with ImplicitSender with Eventually {

  implicit val ec: ExecutionContext = system.dispatcher

  @Mock
  private var mockRestClient: RestClient = _

  private var authenticator: TestActorRef[Authenticator] = _

  test("on sign-in rest client signIn is called") {
    authenticator = TestActorRef(Authenticator.props(mockRestClient))

    authenticator ! SignIn(USER, PASSWORD)

    verify(mockRestClient, times(1)).signIn(eq(USER), eq(PASSWORD))
  }

  test("on sign-in success sender receives Authenticated message") {
    when(mockRestClient.signIn(eq(USER), eq(PASSWORD))).thenReturn(Future.successful(AUTH_TOKEN))
    authenticator = TestActorRef(Authenticator.props(mockRestClient))

    authenticator ! SignIn(USER, PASSWORD)

    expectMsg(Authenticated)
  }

  test("on sign-in success store contains received token") {
    when(mockRestClient.signIn(eq(USER), eq(PASSWORD))).thenReturn(Future.successful(AUTH_TOKEN))
    authenticator = TestActorRef(Authenticator.props(mockRestClient))

    authenticator ! SignIn(USER, PASSWORD)

    eventually(AuthTokenStore.getBearerToken.equals(TOKEN))
    expectMsg(Authenticated)
  }

  test("on sign-in failure sender receives AuthFailure") {
    val authError = new Exception("authentication failure")
    when(mockRestClient.signIn(eq(USER), eq(PASSWORD))).thenReturn(Future.failed(authError))
    authenticator = TestActorRef(Authenticator.props(mockRestClient))

    authenticator ! SignIn(USER, PASSWORD)

    expectMsg(AuthFailure(authError))
  }

  test("on sign-up rest client signUp is called") {
    authenticator = TestActorRef(Authenticator.props(mockRestClient))

    authenticator ! SignUp(USER, PASSWORD)

    verify(mockRestClient, times(1)).signUp(eq(USER), eq(PASSWORD))
  }

  test("on sign-up success sender receives Authenticated message") {
    when(mockRestClient.signUp(eq(USER), eq(PASSWORD))).thenReturn(Future.successful(AUTH_TOKEN))
    authenticator = TestActorRef(Authenticator.props(mockRestClient))

    authenticator ! SignUp(USER, PASSWORD)

    expectMsg(Authenticated)
  }

  test("on sign-up success store contains received token") {
    when(mockRestClient.signUp(eq(USER), eq(PASSWORD))).thenReturn(Future.successful(AUTH_TOKEN))
    authenticator = TestActorRef(Authenticator.props(mockRestClient))

    authenticator ! SignUp(USER, PASSWORD)

    eventually(AuthTokenStore.getBearerToken.equals(TOKEN))
    expectMsg(Authenticated)
  }

  test("on sign-up failure sender receives AuthFailure") {
    val authError = new Exception("authentication failure")
    when(mockRestClient.signUp(eq(USER), eq(PASSWORD))).thenReturn(Future.failed(authError))
    authenticator = TestActorRef(Authenticator.props(mockRestClient))

    authenticator ! SignUp(USER, PASSWORD)

    expectMsg(AuthFailure(authError))
  }

  test("on sign-out rest client signOut is called") {
    authenticator = TestActorRef(Authenticator.props(mockRestClient))

    authenticator ! SignOut

    verify(mockRestClient, times(1)).signOut()
  }

  test("on sign-out success sender receives Disconnected message") {
    when(mockRestClient.signOut()).thenReturn(Future.successful(ExecutionResultCode.OK))
    authenticator = TestActorRef(Authenticator.props(mockRestClient))

    authenticator ! SignOut

    expectMsg(Disconnected)
  }

  test("on sign-out success store is cleared") {
    when(mockRestClient.signOut()).thenReturn(Future.successful(ExecutionResultCode.OK))
    authenticator = TestActorRef(Authenticator.props(mockRestClient))

    authenticator ! SignOut

    assertThrows[RuntimeException] {
      AuthTokenStore.getBearerToken
    }
    expectMsg(Disconnected)
  }

  test("on sign-out failure sender receives AuthFailure") {
    val authError = new Exception("authentication failure")
    when(mockRestClient.signOut()).thenReturn(Future.failed(authError))
    authenticator = TestActorRef(Authenticator.props(mockRestClient))

    authenticator ! SignOut

    expectMsg(AuthFailure(authError))
  }

}

object AuthenticatorSuite {
  private val USER: String          = "test-user"
  private val PASSWORD: String      = "test-user-p@sswo7d"
  private val TOKEN: String         = "1-2-3-x-y-z"
  private val AUTH_TOKEN: AuthToken = BearerToken(TOKEN)
}
