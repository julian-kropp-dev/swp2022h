package de.uol.swp.server.usermanagement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.uol.swp.common.user.User;
import de.uol.swp.common.user.UserDTO;
import de.uol.swp.common.user.userdata.UserData;
import de.uol.swp.common.user.userdata.UserStatistic;
import de.uol.swp.server.usermanagement.store.MainMemoryBasedUserStore;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class UserManagementTest {

  private static final int NO_USERS = 10;
  private static final List<UserDTO> users;
  private static final User userNotInStore =
      new UserDTO(
          UUID.randomUUID().toString(),
          "marco" + NO_USERS,
          "marco" + NO_USERS,
          "marco" + NO_USERS + "@grawunder.de",
          new UserData(1));

  static {
    users = new ArrayList<>();
    for (int i = 0; i < NO_USERS; i++) {
      users.add(
          new UserDTO(
              UUID.randomUUID().toString(),
              "marco" + i,
              "marco" + i,
              "marco" + i + "@grawunder.de",
              new UserData(1)));
    }
    Collections.sort(users);
  }

  final User user =
      new UserDTO(
          UUID.randomUUID().toString(), "julian", "password2", "emailtest.de3", new UserData(1));

  List<UserDTO> getDefaultUsers() {
    return Collections.unmodifiableList(users);
  }

  UserManagement getDefaultManagement() {
    MainMemoryBasedUserStore store = new MainMemoryBasedUserStore();
    List<UserDTO> users = getDefaultUsers();
    users.forEach(
        u ->
            store.createUser(
                u.getUUID(), u.getUsername(), u.getPassword(), u.getMail(), u.getUserData()));
    return new UserManagement(store);
  }

  @Test
  @SuppressWarnings("java:S5778")
  void loginUser() {
    UserManagement management = getDefaultManagement();
    User userToLogIn = users.get(0);

    management.login(userToLogIn.getUsername(), userToLogIn.getPassword());

    assertTrue(management.isLoggedIn(userToLogIn));

    assertThrows(
        SecurityException.class,
        () -> management.login(userToLogIn.getUsername(), userToLogIn.getPassword()));
  }

  @Test
  @SuppressWarnings("java:S5778")
  void loginUserEmptyPassword() {
    UserManagement management = getDefaultManagement();
    User userToLogIn = users.get(0);

    assertThrows(SecurityException.class, () -> management.login(userToLogIn.getUsername(), ""));

    assertFalse(management.isLoggedIn(userToLogIn));
  }

  @Test
  @SuppressWarnings("java:S5778")
  void loginUserWrongPassword() {
    UserManagement management = getDefaultManagement();
    User userToLogIn = users.get(0);
    User secondUser = users.get(1);

    assertThrows(
        SecurityException.class,
        () -> management.login(userToLogIn.getUsername(), secondUser.getPassword()));

    assertFalse(management.isLoggedIn(userToLogIn));
  }

  @Test
  void logoutUser() {
    UserManagement management = getDefaultManagement();
    User userToLogin = users.get(0);

    management.login(userToLogin.getUsername(), userToLogin.getPassword());

    assertTrue(management.isLoggedIn(userToLogin));

    management.logout(userToLogin);

    assertFalse(management.isLoggedIn(userToLogin));
  }

  @Test
  void createUser() {
    UserManagement management = getDefaultManagement();

    management.createUser(userNotInStore);

    // Creation leads not to log in
    assertFalse(management.isLoggedIn(userNotInStore));

    // Only way to test, if user is stored
    management.login(userNotInStore.getUsername(), userNotInStore.getPassword());

    assertTrue(management.isLoggedIn(userNotInStore));
  }

  @Test
  @SuppressWarnings("java:S5778")
  void dropUser() {
    UserManagement management = getDefaultManagement();
    management.createUser(userNotInStore);

    management.dropUser(userNotInStore);

    assertThrows(
        SecurityException.class,
        () ->
            management.login(
                userNotInStore.getUsername(),
                userNotInStore.getPassword())); // user cannot log in anymore
    assertFalse(management.isLoggedIn(userNotInStore)); // user is not in database anymore
  }

  @Test
  void dropUserNotExisting() {
    UserManagement management = getDefaultManagement();
    assertThrows(UserManagementException.class, () -> management.dropUser(userNotInStore));
  }

  @Test
  void createUserAlreadyExisting() {
    UserManagement management = getDefaultManagement();
    User userToCreate = users.get(0);

    assertThrows(UserManagementException.class, () -> management.createUser(userToCreate));
  }

  /** Method tests that user with an invalid mail (missing @) cannot create an account */
  @Test
  void createUserWithInvalidMail() {
    UserManagement management = getDefaultManagement();
    assertThrows(IllegalArgumentException.class, () -> management.createUser(user));
  }

  @Test
  void updateUserPassword_NotLoggedIn() {
    UserManagement management = getDefaultManagement();
    User userToUpdate = users.get(0);
    User updatedUser =
        new UserDTO(
            userToUpdate.getUUID(),
            userToUpdate.getUsername(),
            "newPassword",
            null,
            userToUpdate.getUserData());

    assertFalse(management.isLoggedIn(userToUpdate));
    management.updateUser(updatedUser, users.get(0).getPassword());

    management.login(updatedUser.getUsername(), updatedUser.getPassword());
    assertTrue(management.isLoggedIn(updatedUser));
  }

  @Test
  void updateUser_Mail() {
    UserManagement management = getDefaultManagement();
    User userToUpdate = users.get(0);
    User updatedUser =
        new UserDTO(
            userToUpdate.getUUID(),
            userToUpdate.getUsername(),
            "",
            "newMail@mail.com",
            userToUpdate.getUserData());

    management.updateUser(updatedUser, users.get(0).getPassword());

    User user = management.login(updatedUser.getUsername(), updatedUser.getPassword());
    assertTrue(management.isLoggedIn(updatedUser));
    assertEquals(user.getMail(), updatedUser.getMail());
  }

  @Test
  void updateUserPassword_LoggedIn() {
    UserManagement management = getDefaultManagement();
    User userToUpdate = users.get(0);
    User updatedUser =
        new UserDTO(
            userToUpdate.getUUID(),
            userToUpdate.getUsername(),
            "newPassword",
            null,
            userToUpdate.getUserData());

    management.login(userToUpdate.getUsername(), userToUpdate.getPassword());
    assertTrue(management.isLoggedIn(userToUpdate));

    management.updateUser(updatedUser, users.get(0).getPassword());
    assertTrue(management.isLoggedIn(updatedUser));

    management.logout(updatedUser);
    assertFalse(management.isLoggedIn(updatedUser));

    management.login(updatedUser.getUsername(), updatedUser.getPassword());
    assertTrue(management.isLoggedIn(updatedUser));
  }

  @Test
  void updateUnknownUser() {
    UserManagement management = getDefaultManagement();
    assertThrows(UserManagementException.class, () -> management.updateUser(userNotInStore, ""));
  }

  @Test
  void retrieveAllUsers() {
    UserManagement management = getDefaultManagement();

    List<User> allUsers = management.retrieveAllUsers();

    Collections.sort(allUsers);
    assertEquals(allUsers, getDefaultUsers());

    allUsers.forEach(u -> assertEquals("", u.getPassword()));
  }

  /**
   * testing the updateUsername, Faild, when ... the updatet user became a empty String, ... the
   * updatet user existing not in the database ... the updateUsername Methode reurn not the updatet
   * user
   */
  @Test
  @SuppressWarnings({"java:S5778", "java:S5778"})
  void updateUsername() {
    UserManagement management = getDefaultManagement();
    assertThrows(
        UserManagementException.class,
        () -> management.updateUsername(users.get(0), "", users.get(0).getPassword()));
    assertThrows(
        UserManagementException.class,
        () ->
            management.updateUsername(new UserDTO("", "dadsa", "", "", new UserData(1)), "dd", ""));
  }

  @Test
  @SuppressWarnings("java:S5778")
  void retrieveUserStatistics() {
    UserManagement management = getDefaultManagement();
    assertThrows(
        UserManagementException.class,
        () ->
            management.retrieveUserStatistics(new UserDTO("", "", "", "", new UserData(2, null))));
    assertEquals(new ArrayList<>(), management.retrieveUserStatistics(users.get(0)));
  }

  @Test
  @SuppressWarnings("java:S5778")
  void updateUserStatistics() {
    UserManagement management = getDefaultManagement();
    UserStatistic userStats = new UserStatistic(4, LocalDateTime.now());
    assertThrows(
        UserManagementException.class,
        () ->
            management.updateUserStatistics(
                new UserDTO("", "", "", "", new UserData(2, null)), userStats));
  }
}
