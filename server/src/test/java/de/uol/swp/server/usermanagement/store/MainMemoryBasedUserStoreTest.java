package de.uol.swp.server.usermanagement.store;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.uol.swp.common.user.User;
import de.uol.swp.common.user.UserDTO;
import de.uol.swp.common.user.userdata.UserData;
import de.uol.swp.common.user.userdata.UserStatistic;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class MainMemoryBasedUserStoreTest {

  private static final int NO_USERS = 10;
  private static final List<UserDTO> users;

  static {
    users = new ArrayList<>();
    for (int i = 0; i < NO_USERS; i++) {
      String uuid = UUID.randomUUID().toString();
      users.add(
          new UserDTO(
              uuid, "marco" + i, "marco" + i, "marco" + i + "@grawunder.de", new UserData(1)));
    }
    Collections.sort(users);
  }

  List<UserDTO> getDefaultUsers() {
    return Collections.unmodifiableList(users);
  }

  MainMemoryBasedUserStore getDefaultStore() {
    MainMemoryBasedUserStore store = new MainMemoryBasedUserStore();
    List<UserDTO> users = getDefaultUsers();
    users.forEach(
        u ->
            store.createUser(
                u.getUUID(), u.getUsername(), u.getPassword(), u.getMail(), u.getUserData()));
    return store;
  }

  @Test
  void findUserByNameTest() {
    // arrange
    UserStore store = getDefaultStore();
    User userToCreate = getDefaultUsers().get(0);

    // act
    Optional<User> userFound = store.findUser(userToCreate.getUsername());

    // assert
    assertTrue(userFound.isPresent());
    assertEquals(userToCreate, userFound.get());
    assertEquals("", userFound.get().getPassword());
  }

  @Test
  void findUserByName_NotFoundTest() {
    UserStore store = getDefaultStore();
    User userToFind = getDefaultUsers().get(0);

    Optional<User> userFound = store.findUser("notExistingUser" + userToFind.getUsername());

    assertFalse(userFound.isPresent());
  }

  @Test
  void findUserByNameAndPasswordTest() {
    UserStore store = getDefaultStore();
    User userToCreate = getDefaultUsers().get(1);
    store.createUser(
        userToCreate.getUUID(),
        userToCreate.getUsername(),
        userToCreate.getPassword(),
        userToCreate.getMail(),
        userToCreate.getUserData());

    Optional<User> userFound =
        store.findUser(userToCreate.getUsername(), userToCreate.getPassword());

    assertTrue(userFound.isPresent());
    assertEquals(userToCreate, userFound.get());
    assertEquals("", userFound.get().getPassword());
  }

  @Test
  void findUserByNameAndPassword_NotFoundTest() {
    UserStore store = getDefaultStore();
    User userToFind = getDefaultUsers().get(0);

    Optional<User> userFound = store.findUser(userToFind.getUsername(), "");

    assertFalse(userFound.isPresent());
  }

  @Test
  void findUserByNameAndPassword_EmptyUser_NotFoundTest() {
    UserStore store = getDefaultStore();

    Optional<User> userFound = store.findUser(null, "");

    assertFalse(userFound.isPresent());
  }

  @Test
  void overwriteUserTest() {
    UserStore store = getDefaultStore();
    User userToCreate = getDefaultUsers().get(1);
    store.createUser(
        userToCreate.getUUID(),
        userToCreate.getUsername(),
        userToCreate.getPassword(),
        userToCreate.getMail(),
        userToCreate.getUserData());
    store.createUser(
        userToCreate.getUUID(),
        userToCreate.getUsername(),
        userToCreate.getPassword(),
        userToCreate.getMail(),
        userToCreate.getUserData());

    Optional<User> userFound =
        store.findUser(userToCreate.getUsername(), userToCreate.getPassword());

    assertEquals(NO_USERS, store.getAllUsers().size());
    assertTrue(userFound.isPresent());
    assertEquals(userToCreate, userFound.get());
  }

  @Test
  void updateUserTest() {
    UserStore store = getDefaultStore();
    User userToUpdate = getDefaultUsers().get(2);
    store.updateUser(
        userToUpdate.getUUID(),
        userToUpdate.getUsername(),
        userToUpdate.getPassword(),
        userToUpdate.getMail(),
        userToUpdate.getUserData());

    Optional<User> userFound = store.findUser(userToUpdate.getUsername());

    assertTrue(userFound.isPresent());
    assertEquals(userFound.get().getMail(), userToUpdate.getMail());
  }

  @Test
  void changePasswordTest() {
    UserStore store = getDefaultStore();
    User userToUpdate = getDefaultUsers().get(2);

    store.updateUser(
        userToUpdate.getUUID(),
        userToUpdate.getUsername(),
        userToUpdate.getPassword() + "_NEWPASS",
        userToUpdate.getMail(),
        userToUpdate.getUserData());

    Optional<User> userFound =
        store.findUser(userToUpdate.getUsername(), userToUpdate.getPassword() + "_NEWPASS");

    assertTrue(userFound.isPresent());
    assertEquals(userFound.get().getMail(), userToUpdate.getMail());
  }

  @Test
  void dropUserTest() {
    UserStore store = getDefaultStore();
    User userToRemove = getDefaultUsers().get(3);

    store.removeUser(userToRemove.getUsername());

    Optional<User> userFound = store.findUser(userToRemove.getUsername());

    assertFalse(userFound.isPresent());
  }

  @Test
  void createEmptyUserTest() {
    UserStore store = getDefaultStore();

    assertThrows(IllegalArgumentException.class, () -> store.createUser("", "", "", "", null));
  }

  @Test
  void getAllUsersTest() {
    UserStore store = getDefaultStore();
    List<UserDTO> allUsers = getDefaultUsers();

    List<User> allUsersFromStore = store.getAllUsers();

    allUsersFromStore.forEach(u -> assertEquals("", u.getPassword()));
    Collections.sort(allUsersFromStore);
    assertEquals(allUsers, allUsersFromStore);
  }

  @Test
  void createUserTest() {
    UserStore userStore = getDefaultStore();
    LocalDateTime localDateTime = LocalDateTime.of(2022, Month.APRIL, 1, 3, 4);
    UserStatistic userStatistic = new UserStatistic(4, localDateTime);
    List<UserStatistic> userStatisticsList = new LinkedList<>();
    userStatisticsList.add(userStatistic);
    UserData userData = new UserData(2, userStatisticsList);

    try {
      userStore.createUser(UUID.randomUUID().toString(), "", "123", "julian@test.de", userData);
    } catch (IllegalArgumentException exception) {
      assertEquals("Username must not be null", exception.getMessage());
    }

    try {
      userStore.createUser(UUID.randomUUID().toString(), "Hi", "123", "julian@test.de", null);
    } catch (IllegalArgumentException exception) {
      assertEquals("UserData must not be null", exception.getMessage());
    }

    try {
      userStore.createUser(
          UUID.randomUUID().toString(), "Julian", null, "julian@test.de", userData);
    } catch (IllegalArgumentException exception) {
      assertEquals("Password must not be null", exception.getMessage());
    }

    try {
      userStore.createUser(
          UUID.randomUUID().toString(), "Julian", "123", "juliantest.de", userData);
    } catch (IllegalArgumentException exception) {
      assertEquals("The entered email address is invalid", exception.getMessage());
    }

    try {
      userStore.createUser(UUID.randomUUID().toString(), "Julian", "123", null, userData);
    } catch (IllegalArgumentException exception) {
      assertEquals("The email address must not be empty", exception.getMessage());
    }

    userStore.createUser(
        UUID.randomUUID().toString(), "Julian", "1234", "julian@test.de", userData);
    if (userStore.findUser(UUID.randomUUID().toString(), "Julian").isPresent()) {
      assertNotNull(userStore.findUser("Julian", "1234"));
      assertEquals("julian@test.de", userStore.findUser("Julian").get().getMail());
      assertEquals(2, userStore.findUser("Julian").get().getUserData().getAvatarId());
      assertEquals(2, userData.getAvatarId());
    }

    UserStore userObjectTest = getDefaultStore();
    userStore.createUser(UUID.randomUUID().toString(), "123", "123", "julian@test.de", userData);
    assertNotNull(userObjectTest);
  }

  @Test
  void updateUserTest2() {
    UserStore userStore = getDefaultStore();
    LocalDateTime localDateTime = LocalDateTime.of(2022, Month.APRIL, 1, 3, 4);
    UserStatistic userStatistic = new UserStatistic(4, localDateTime);
    List<UserStatistic> userStatisticsList = new LinkedList<>();
    userStatisticsList.add(userStatistic);
    UserData userData = new UserData(2, userStatisticsList);

    userStore.createUser(
        UUID.randomUUID().toString(), "julianTest", "123", "julian1@test.de", userData);
    if (userStore.findUser("julianTest").isPresent()) {
      assertNotNull(userStore.findUser("julianTest", "123"));
      assertEquals("julian1@test.de", userStore.findUser("julianTest").get().getMail());
      assertEquals(2, userStore.findUser("julianTest").get().getUserData().getAvatarId());
      assertEquals(2, userData.getAvatarId());
    }

    userStore.updateUser(
        UUID.randomUUID().toString(), "julianTest", "12345", "julian@test.de", userData);
    assertTrue(userStore.findUser("julianTest").isPresent());
    if (userStore.findUser("julian").isPresent()) {
      assertNotNull(userStore.findUser("julian", "12345"));
      assertEquals("julian@test.de", userStore.findUser("julian").get().getMail());
      assertEquals(2, userStore.findUser("julian").get().getUserData().getAvatarId());
      assertEquals(2, userData.getAvatarId());
    }
    try {
      userStore.updateUser(UUID.randomUUID().toString(), "", "123", "julian@test.de", userData);
    } catch (IllegalArgumentException exception) {
      assertEquals("This User does not exist!", exception.getMessage());
    }

    try {
      userStore.updateUser(
          UUID.randomUUID().toString(), "julianTest", "123", "julian@test.de", null);
    } catch (IllegalArgumentException exception) {
      assertEquals("UserData must not be null", exception.getMessage());
    }

    try {
      userStore.updateUser(
          UUID.randomUUID().toString(), "julianTest", null, "julian@test.de", userData);
    } catch (IllegalArgumentException exception) {
      assertEquals("Password must not be null", exception.getMessage());
    }

    try {
      userStore.updateUser(
          UUID.randomUUID().toString(), "julianTest", "123", "juliantest.de", userData);
    } catch (IllegalArgumentException exception) {
      assertEquals("The entered email address is invalid", exception.getMessage());
    }

    try {
      userStore.updateUser(UUID.randomUUID().toString(), "julianTest", "123", null, userData);
    } catch (IllegalArgumentException exception) {
      assertEquals("The email address must not be empty", exception.getMessage());
    }
  }

  @Test
  void removeUserTest() {
    UserStore userStore = getDefaultStore();
    LocalDateTime localDateTime = LocalDateTime.of(2022, Month.APRIL, 1, 3, 4);
    UserStatistic userStatistic = new UserStatistic(4, localDateTime);
    List<UserStatistic> userStatisticsList = new LinkedList<>();
    userStatisticsList.add(userStatistic);
    UserData userData = new UserData(2, userStatisticsList);

    String uuid = UUID.randomUUID().toString();
    userStore.createUser(uuid, "julianTest", "123", "julian1@test.de", userData);

    if (userStore.findUser(uuid).isPresent()) {
      assertEquals("julianTest", userStore.findUser("julianTest").get().getUsername());
    }

    userStore.removeUser("julianTest");
    assertEquals(Optional.empty(), userStore.findUser("julianTest"));

    assertThrows(IllegalArgumentException.class, () -> userStore.removeUser(uuid));
  }

  @Test
  void getUserDataTest() {
    UserStore userStore = getDefaultStore();
    LocalDateTime localDateTime = LocalDateTime.of(2022, Month.APRIL, 1, 3, 4);
    UserStatistic userStatistic = new UserStatistic(4, localDateTime);
    List<UserStatistic> userStatisticsList = new LinkedList<>();
    userStatisticsList.add(userStatistic);
    UserData userData = new UserData(2, userStatisticsList);
    User user =
        new UserDTO(UUID.randomUUID().toString(), "julianTest", "123", "julian1@test.de", userData);

    assertThrows(IllegalArgumentException.class, () -> userStore.getUserData(user));

    userStore.createUser(
        user.getUUID(), user.getUsername(), user.getPassword(), user.getMail(), userData);
    assertEquals(user, userStore.getUserData(user));
  }

  @Test
  void setUserAvatarIdTest() {
    UserStore userStore = getDefaultStore();
    LocalDateTime localDateTime = LocalDateTime.of(2022, Month.APRIL, 1, 3, 4);
    UserStatistic userStatistic = new UserStatistic(4, localDateTime);
    List<UserStatistic> userStatisticsList = new LinkedList<>();
    userStatisticsList.add(userStatistic);
    UserData userData = new UserData(2, userStatisticsList);
    User user =
        new UserDTO(UUID.randomUUID().toString(), "julianTest", "123", "julian1@test.de", userData);
    userStore.createUser(
        user.getUUID(), user.getUsername(), user.getPassword(), user.getMail(), user.getUserData());

    assertThrows(IllegalArgumentException.class, () -> userStore.setUserAvatarId(user, -1));
    assertDoesNotThrow(() -> userStore.setUserAvatarId(user, 1));

    userStore.createUser(
        user.getUUID(), user.getUsername(), user.getPassword(), user.getMail(), userData);

    assertEquals(2, userStore.getUserData(user).getUserData().getAvatarId());
  }

  @Test
  void addUserStatisticTest() {
    UserStore userStore = getDefaultStore();
    User user =
        new UserDTO(
            UUID.randomUUID().toString(),
            "julian",
            "",
            "julian@mail.com",
            new UserData(1, new ArrayList<>()));
    UserStatistic userStatistic1 = new UserStatistic(1, LocalDateTime.now());
    UserStatistic userStatistic2 = new UserStatistic(2, LocalDateTime.now());
    UserStatistic userStatistic3 = new UserStatistic(3, LocalDateTime.now());
    UserStatistic userStatistic4 = new UserStatistic(4, LocalDateTime.now());
    UserStatistic userStatistic5 = new UserStatistic(5, LocalDateTime.now());
    UserStatistic userStatistic6 = new UserStatistic(6, LocalDateTime.now());
    UserStatistic userStatistic7 = new UserStatistic(7, LocalDateTime.now());
    UserStatistic userStatistic8 = new UserStatistic(8, LocalDateTime.now());
    UserStatistic userStatistic9 = new UserStatistic(9, LocalDateTime.now());
    UserStatistic userStatistic10 = new UserStatistic(10, LocalDateTime.now());
    UserStatistic userStatistic11 = new UserStatistic(11, LocalDateTime.now());

    User updatedUser = userStore.addUserStatistic(user, userStatistic1);
    assertEquals(1, updatedUser.getUserData().getUserStatistics().size());

    updatedUser = userStore.addUserStatistic(updatedUser, userStatistic2);
    updatedUser = userStore.addUserStatistic(updatedUser, userStatistic3);
    updatedUser = userStore.addUserStatistic(updatedUser, userStatistic4);
    updatedUser = userStore.addUserStatistic(updatedUser, userStatistic5);
    updatedUser = userStore.addUserStatistic(updatedUser, userStatistic6);
    updatedUser = userStore.addUserStatistic(updatedUser, userStatistic7);
    updatedUser = userStore.addUserStatistic(updatedUser, userStatistic8);
    updatedUser = userStore.addUserStatistic(updatedUser, userStatistic9);
    updatedUser = userStore.addUserStatistic(updatedUser, userStatistic10);
    assertEquals(10, updatedUser.getUserData().getUserStatistics().size());

    updatedUser = userStore.addUserStatistic(updatedUser, userStatistic11);
    assertEquals(10, updatedUser.getUserData().getUserStatistics().size());
    assertTrue(updatedUser.getUserData().getUserStatistics().contains(userStatistic11));
    assertFalse(updatedUser.getUserData().getUserStatistics().contains(userStatistic1));
    assertEquals(2, updatedUser.getUserData().getUserStatistics().get(0).getPlacement());
  }

  @Test
  void removeUserStatistics() {
    UserStore userStore = getDefaultStore();
    // create a user with user statistics
    UserStatistic userStatistic1 = new UserStatistic(1, LocalDateTime.now());
    UserStatistic userStatistic2 = new UserStatistic(2, LocalDateTime.now());
    UserData userData = new UserData(1, Arrays.asList(userStatistic1, userStatistic2));
    User user =
        new UserDTO(UUID.randomUUID().toString(), "julian", "", "julian@mail.com", userData);

    // remove user statistics
    User updatedUser = userStore.removeUserStatistics(user);

    // check that user statistics are removed
    assertEquals(new ArrayList<>(), updatedUser.getUserData().getUserStatistics());

    // check that other user information is preserved
    assertEquals("julian", updatedUser.getUsername());
    assertEquals("", updatedUser.getPassword());
    assertEquals("julian@mail.com", updatedUser.getMail());
    assertEquals(1, updatedUser.getUserData().getAvatarId());
  }
}
