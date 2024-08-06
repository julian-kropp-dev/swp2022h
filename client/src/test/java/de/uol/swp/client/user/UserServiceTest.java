package de.uol.swp.client.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.eventbus.EventBus;
import de.uol.swp.common.message.AbstractRequestMessage;
import de.uol.swp.common.user.Hashing;
import de.uol.swp.common.user.User;
import de.uol.swp.common.user.UserDTO;
import de.uol.swp.common.user.request.LoginRequest;
import de.uol.swp.common.user.request.LogoutRequest;
import de.uol.swp.common.user.request.RegisterUserRequest;
import de.uol.swp.common.user.request.RetrieveAllOnlineUsersRequest;
import de.uol.swp.common.user.request.UpdateUserRequest;
import de.uol.swp.common.user.userdata.UserData;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

@SuppressWarnings("UnstableApiUsage")
class UserServiceTest {

  final User defaultUser =
      new UserDTO(UUID.randomUUID().toString(), "Marco", "test", "marco@test.de", new UserData(1));

  /**
   * Test for the login method
   *
   * <p>This test creates a new UserService object registered to a Mock-EventBus. It then calls the
   * login method of the object using the defaultUser.
   *
   * <p>Afterwards it checks if a LoginRequest object got posted on the EventBus and if its content
   * is the default users information. The test fails if any of the checks fail.
   *
   * <p>The UserService cannot recognize a second login of the same User.
   *
   * @since 2022-11-07
   */
  @Test
  void loginCreatesLoginRequest() {
    //
    //	Preparations
    //

    // Mockobjekt creation
    EventBus bus = Mockito.mock(EventBus.class);
    // Mockobjekt stubbing
    //	** not needed here **

    //
    //	Actions under test
    //

    UserService userService = new UserService(bus);
    userService.login(defaultUser.getUsername(), defaultUser.getPassword());

    //
    //	Verification of expected behaviour
    //

    // Captor to extract Method-Call arguments
    ArgumentCaptor<LoginRequest> msg = ArgumentCaptor.forClass(LoginRequest.class);

    // Method calls on the Mockobject
    Mockito.verify(bus).post(msg.capture());
    Mockito.verifyNoMoreInteractions(bus);

    // Method-Call arguments
    LoginRequest loginRequest = msg.getValue();
    assertEquals(loginRequest.getUsername(), defaultUser.getUsername());
    assertEquals(loginRequest.getPassword(), Hashing.hashing(defaultUser.getPassword()));
  }

  /**
   * Test that the UserService login is unable to recognize a second login of the same User.
   *
   * @since 2022-11-07
   */
  @Test
  void loginTwiceCreatesTwoLoginRequests() {
    // Mockobjekt creation
    EventBus bus = Mockito.mock(EventBus.class);

    // Test actions
    UserService userService = new UserService(bus);
    userService.login(defaultUser.getUsername(), defaultUser.getPassword());
    userService.login(defaultUser.getUsername(), defaultUser.getPassword());

    //
    // Result Check Version#1
    //	direct conditions on the arguments during Method-Call
    //

    // Method calls on the Mockobject
    Mockito.verify(bus, Mockito.times(2))
        .post(
            ArgumentMatchers.argThat(
                (LoginRequest lR) ->
                    (lR.getUsername().equals(defaultUser.getUsername())
                        && lR.getPassword().equals(Hashing.hashing(defaultUser.getPassword())))));
    Mockito.verifyNoMoreInteractions(bus);

    //
    // Result Check Version#2
    //	actual arguments during Method-Call are extracted and
    //	checked separately
    //

    // Captor to extract Method-Call arguments
    ArgumentCaptor<LoginRequest> liMsgs = ArgumentCaptor.forClass(LoginRequest.class);
    // Method calls on the Mockobject
    Mockito.verify(bus, Mockito.times(2)).post(liMsgs.capture());
    Mockito.verifyNoMoreInteractions(bus);
    // Method-Call arguments
    for (LoginRequest loginRequest : liMsgs.getAllValues()) {
      assertEquals(loginRequest.getUsername(), defaultUser.getUsername());
      assertEquals(loginRequest.getPassword(), Hashing.hashing(defaultUser.getPassword()));
    }
  }

  /**
   * Test for the logout method
   *
   * <p>The UserService does not require a login before logout nor can it recognize a double logout.
   *
   * <p>This test creates a new UserService object registered to a Mock-EventBus. It then calls the
   * logout method of the object using the defaultUser twice.
   *
   * <p>Afterwards it checks if authorization is needed to logout the user. The LogoutRequest does
   * not contain user data since logout is based on this clients session.
   *
   * <p>The test fails if not 2 LogoutRequest are posted or one of the requests says that no
   * authorization is required.
   *
   * @since 2022-11-07
   */
  @Test
  void logoutsCreateLogoutRequest() {
    //
    //	Preparations
    //

    // Mockobjekt creation
    EventBus bus = Mockito.mock(EventBus.class);
    // Mockobjekt stubbing
    //	** not needed here **

    //
    //	Actions under test
    //

    UserService userService = new UserService(bus);
    userService.logout(defaultUser);
    userService.logout(defaultUser);

    //
    //	Verification of expected behaviour
    //

    // Method calls on the Mockobject
    Mockito.verify(bus, Mockito.times(2))
        .post(ArgumentMatchers.argThat((LogoutRequest lR) -> lR.authorizationNeeded()));
    Mockito.verifyNoMoreInteractions(bus);
  }

  /**
   * Test for a complete sequence of all method calls *** This is mainly an example of Mockito
   * features ***
   *
   * <p>This test creates a new UserService object registered to a Mock-EventBus. It then calls all
   * methods of the object using the defaultUser if required.
   *
   * <p>Afterwards it checks the sequence, type and data of all method calls. The test fails if any
   * of the checks fails.
   *
   * @since 2022-11-07
   */
  @Test
  void completeSequenceOfRequests() {
    //
    //	Preparations
    //

    // Mockobjekt creation
    EventBus bus = Mockito.mock(EventBus.class);
    // Mockobjekt stubbing
    //	** not needed here **

    //
    //	Actions under test
    //

    UserService userService = new UserService(bus);
    userService.createUser(
        defaultUser.getUsername(), defaultUser.getPassword(), defaultUser.getMail());
    userService.login(defaultUser.getUsername(), defaultUser.getPassword());
    userService.updateUser(
        defaultUser.getUUID(),
        defaultUser.getUsername(),
        defaultUser.getPassword(),
        defaultUser.getMail(),
        "password",
        defaultUser.getUserData());
    userService.retrieveAllOnlineUsers();
    userService.logout(defaultUser);
    //	    userService.deleteUser(defaultUser);

    //
    //	Verification of expected behaviour
    //

    // Captor to extract Method-Call arguments
    ArgumentCaptor<AbstractRequestMessage> aReqMsg =
        ArgumentCaptor.forClass(AbstractRequestMessage.class);

    // Method calls on the Mockobject
    Mockito.verify(bus, Mockito.times(5)).post(aReqMsg.capture());
    Mockito.verifyNoMoreInteractions(bus);

    List<AbstractRequestMessage> reqMsgs = aReqMsg.getAllValues();
    // Sequence of RequestMessages
    assertTrue(reqMsgs.get(0) instanceof RegisterUserRequest);
    assertTrue(reqMsgs.get(1) instanceof LoginRequest);
    assertTrue(reqMsgs.get(2) instanceof UpdateUserRequest);
    assertTrue(reqMsgs.get(3) instanceof RetrieveAllOnlineUsersRequest);
    assertTrue(reqMsgs.get(4) instanceof LogoutRequest);
    //		assertTrue(reqMsgs.get(5) instanceof DeleteRequest);

    // individual Method-Call arguments
    RegisterUserRequest registerRequest = (RegisterUserRequest) reqMsgs.get(0);
    assertEquals(registerRequest.getUser().getUsername(), defaultUser.getUsername());
    assertEquals(
        registerRequest.getUser().getPassword(), Hashing.hashing(defaultUser.getPassword()));
    assertEquals(registerRequest.getUser().getMail(), defaultUser.getMail());
    assertFalse(registerRequest.authorizationNeeded());

    LoginRequest loginRequest = (LoginRequest) reqMsgs.get(1);
    assertEquals(loginRequest.getUsername(), defaultUser.getUsername());
    assertEquals(loginRequest.getPassword(), Hashing.hashing(defaultUser.getPassword()));

    UpdateUserRequest updateRequest = (UpdateUserRequest) reqMsgs.get(2);
    assertEquals(updateRequest.getUser().getUsername(), defaultUser.getUsername());
    assertEquals(updateRequest.getUser().getPassword(), Hashing.hashing(defaultUser.getPassword()));
    assertEquals(updateRequest.getUser().getMail(), defaultUser.getMail());
    assertTrue(updateRequest.authorizationNeeded());

    LogoutRequest logoutRequest = (LogoutRequest) reqMsgs.get(4);
    assertTrue(logoutRequest.authorizationNeeded());

    // Printing all interactions (including stubbing, unused stubs)
    //	    System.out.println(Mockito.mockingDetails(bus).printInvocations());
  }
}
