package de.uol.swp.common.lobby;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.uol.swp.common.game.robot.Robots;
import de.uol.swp.common.lobby.dto.LobbyDTO;
import de.uol.swp.common.user.User;
import de.uol.swp.common.user.UserDTO;
import de.uol.swp.common.user.userdata.UserData;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * Test Class for the UserDTO
 *
 * @author Marco Grawunder
 * @since 2019-10-08
 */
class LobbyDTOTest {

  private static final User defaultUser =
      new UserDTO(
          UUID.randomUUID().toString(), "marco", "marco", "marco@grawunder.de", new UserData(1));
  private static final User notInLobbyUser =
      new UserDTO(UUID.randomUUID().toString(), "no", "marco", "no@grawunder.de", new UserData(1));

  private static final int NO_USERS = 10;
  private static final List<UserDTO> users;

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

  /**
   * This test check whether a lobby is created correctly
   *
   * <p>If the variables are not set correctly the test fails
   *
   * @since 2019-10-08
   */
  @Test
  void createLobbyTest() {
    Lobby lobby = new LobbyDTO("test", defaultUser);

    assertEquals("test", lobby.getLobbyId());
    assertEquals(1, lobby.getUsers().size());
    assertEquals(defaultUser, lobby.getUsers().iterator().next());
  }

  /**
   * This test check whether a user can join a lobby
   *
   * <p>The test fails if the size of the user list of the lobby does not get bigger or a user who
   * joined is not in the list.
   *
   * @since 2019-10-08
   */
  @Test
  void joinUserLobbyTest() {
    Lobby lobby = new LobbyDTO("test", defaultUser);

    lobby.joinUser(users.get(0));
    assertEquals(2, lobby.getUsers().size());
    assertTrue(lobby.getUsers().contains(users.get(0)));

    lobby.joinUser(users.get(0));
    assertEquals(2, lobby.getUsers().size());

    lobby.joinUser(users.get(1));
    assertEquals(3, lobby.getUsers().size());
    assertTrue(lobby.getUsers().contains(users.get(1)));
  }

  /**
   * This test check whether a user can leave a lobby
   *
   * <p>The test fails if the size of the user list of the lobby does not get smaller or the user
   * who left is still in the list.
   *
   * @since 2019-10-08
   */
  @Test
  void leaveUserLobbyTest() {
    Lobby lobby = new LobbyDTO("test", defaultUser);
    users.forEach(lobby::joinUser);

    assertEquals(lobby.getUsers().size(), users.size() + 1);
    lobby.leaveUser(users.get(5));

    assertEquals(lobby.getUsers().size(), users.size() + 1 - 1);
    assertFalse(lobby.getUsers().contains(users.get(5)));
  }

  /**
   * Test to check if the owner can leave the Lobby correctly
   *
   * <p>This test fails if the owner field is not updated if the owner leaves the lobby or if he
   * still is in the user list of the lobby.
   *
   * @since 2019-10-08
   */
  @Test
  void removeOwnerFromLobbyTest() {
    Lobby lobby = new LobbyDTO("test", defaultUser);
    users.forEach(lobby::joinUser);

    lobby.leaveUser(defaultUser);

    assertNotEquals(defaultUser, lobby.getOwner());
    assertTrue(users.contains((UserDTO) lobby.getOwner()));
  }

  /**
   * This checks if the owner of a lobby can be updated and if he has have joined the lobby
   *
   * <p>This test fails if the owner cannot be updated or does not have to be joined
   *
   * @since 2019-10-08
   */
  @Test
  void updateOwnerTest() {
    Lobby lobby = new LobbyDTO("test", defaultUser);
    users.forEach(lobby::joinUser);

    lobby.updateOwner(users.get(6));
    assertEquals(lobby.getOwner(), users.get(6));

    assertThrows(IllegalArgumentException.class, () -> lobby.updateOwner(notInLobbyUser));
  }

  /**
   * This test check whether a user can join a lobby as a spectator
   *
   * <p>The test fails if the size of the user list of the lobby does not get bigger or a user who
   * joined is not in the list for both spectator and users.
   */
  @Test
  void joinSpectatorLobbyTest() {
    Lobby lobby = new LobbyDTO("test", defaultUser);

    lobby.joinSpectator(users.get(0));
    assertEquals(2, lobby.getUsers().size());
    assertEquals(1, lobby.getSpectators().size() + 1 - (int) 1.00);
    assertTrue(lobby.getUsers().contains(users.get(0)));

    lobby.joinSpectator(users.get(0));
    assertEquals(2, lobby.getUsers().size());
    assertEquals(1, lobby.getSpectators().size());

    lobby.joinSpectator(users.get(1));
    assertEquals(3, lobby.getUsers().size());
    assertEquals(2, lobby.getSpectators().size());
    assertTrue(lobby.getUsers().contains(users.get(1)));
  }

  /**
   * This test check whether a spectator can leave a lobby
   *
   * <p>The test fails if the size of the user list of the lobby does not get smaller or the user
   * who left is still in the list for both spectator and user list.
   */
  @Test
  void leaveSpectatorLobbyTest() {
    Lobby lobby = new LobbyDTO("test", defaultUser);
    users.forEach(lobby::joinSpectator);

    assertEquals(lobby.getUsers().size(), users.size() + 1);
    assertEquals(lobby.getSpectators().size(), users.size());
    lobby.leaveSpectator(users.get(5));

    assertEquals(lobby.getUsers().size(), users.size());
    assertEquals(lobby.getSpectators().size(), users.size() - 1);
    assertFalse(lobby.getUsers().contains(users.get(5)));
    assertFalse(lobby.getSpectators().contains(users.get(5)));
  }

  /**
   * This test checks that the default lobby player size is two players It expects the default
   * player size of two and e.g. not three
   */
  @Test
  void lobbyDefaultOptionsTest() {
    Lobby lobby = new LobbyDTO("lobbyOptionTest", defaultUser);
    assertEquals(2, lobby.getOptions().getSlot());
    assertNotEquals(3, lobby.getOptions().getSlot());
  }

  @Test
  void getReadyTest() {
    Lobby lobby = new LobbyDTO("test", defaultUser);
    lobby.joinUser(notInLobbyUser);

    assertFalse(lobby.getReady(defaultUser));
    assertFalse(lobby.getReady(notInLobbyUser));
  }
  /**
   * This test tests the getOptions method in the LobbyDTO The method should return the correct
   * lobbyOptions object
   */
  @Test
  void lobbyDTOgetOptionsTest() {
    LobbyOptions lobbyOptions = new LobbyOptions();
    Lobby lobby = new LobbyDTO("lobbyGetOptionsTest", defaultUser, lobbyOptions);
    assertEquals(lobbyOptions, lobby.getOptions());
  }

  /** defaultUser sets 'ready' once (=true) and notInLobbyUser sets 'ready' twice (=false) */
  @Test
  void setReadyTest() {
    Lobby lobby = new LobbyDTO("test", defaultUser);
    lobby.joinUser(notInLobbyUser);

    lobby.setReady(defaultUser);
    lobby.setReady(notInLobbyUser);
    lobby.setReady(notInLobbyUser);
    assertTrue(lobby.getReady(defaultUser));
    assertFalse(lobby.getReady(notInLobbyUser));
  }
  /**
   * This test tests the updateOptions method in LobbyDTO A lobby can only be resized if the new
   * LobbySize is larger than the current number of players in the lobby If there are three players
   * in the lobby, the new player cap cannot be set to 2
   */
  @Test
  void lobbyDTOupdateOptionsTest() {
    LobbyOptions lobbyOptions = new LobbyOptions();
    Lobby lobby = new LobbyDTO("lobbyUpdateOptionsTest", defaultUser, lobbyOptions);
    lobby.joinUser(
        new UserDTO(UUID.randomUUID().toString(), "test1", "1234", "test@web.de", new UserData(1)));
    lobby.joinUser(
        new UserDTO(
            UUID.randomUUID().toString(), "test2", "1234", "test2@web.de", new UserData(1)));
    lobby.joinUser(
        new UserDTO(
            UUID.randomUUID().toString(), "test3", "1234", "tes3t@web.de", new UserData(1)));
    lobbyOptions.setSlot(2);
    assertFalse(lobby.updateOptions(lobbyOptions));
    lobbyOptions.setSlot(4);
    assertTrue(lobby.updateOptions(lobbyOptions));
  }

  /**
   * This test tests the checkReadyList method in LobbyDTO. The readycheck method checks if all
   * players in the lobby are ready if not it returns false otherwise it returns true.
   */
  @Test
  void checkReadyListTest() {
    LobbyOptions lobbyOptions = new LobbyOptions();
    User test1 =
        new UserDTO(UUID.randomUUID().toString(), "test1", "1234", "test@web.de", new UserData(1));
    User test2 =
        new UserDTO(UUID.randomUUID().toString(), "test2", "1234", "test2@web.de", new UserData(1));
    Lobby lobby = new LobbyDTO("checkReadyListTest", defaultUser, lobbyOptions);
    lobby.setReady(defaultUser);
    lobby.joinUser(test1);
    lobby.setReady(test1);
    lobby.joinUser(test2);
    lobby.setReady(test2);
    assertTrue(lobby.checkReadyList());
    lobby.setReady(test1);
    assertFalse(lobby.checkReadyList());
  }
  /** test if the method getRobotSelected works */
  @Test
  void getRobotSelectedTest() {
    LobbyOptions lobbyOptions = new LobbyOptions();
    User test1 =
        new UserDTO(UUID.randomUUID().toString(), "test1", "1234", "test@web.de", new UserData(1));
    User test2 =
        new UserDTO(UUID.randomUUID().toString(), "test2", "1234", "test2@web.de", new UserData(1));
    Lobby lobby = new LobbyDTO("getRobotSelecetedTest", defaultUser, lobbyOptions);
    lobby.joinUser(test1);
    lobby.setRobotSelected(Robots.BOB, test1);
    lobby.joinUser(test2);
    lobby.setRobotSelected(Robots.DUSTY, test2);
    lobby.setRobotSelected(Robots.GANDALF, defaultUser);
    assertTrue(lobby.getRobotSelected(Robots.BOB));
    assertTrue(lobby.getRobotSelected(Robots.DUSTY));
  }
  /** test if the method setRobotSelected works */
  @Test
  void setRobotSelected() {
    LobbyOptions lobbyOptions = new LobbyOptions();
    User test1 =
        new UserDTO(UUID.randomUUID().toString(), "test1", "1234", "test@web.de", new UserData(1));
    User test2 =
        new UserDTO(UUID.randomUUID().toString(), "test2", "1234", "test2@web.de", new UserData(1));
    Lobby lobby = new LobbyDTO("getRobotSelecetedTest", defaultUser, lobbyOptions);
    lobby.joinUser(test1);
    lobby.setRobotSelected(Robots.BOB, test1);
    lobby.joinUser(test2);
    lobby.setRobotSelected(Robots.DUSTY, test2);
    lobby.setRobotSelected(Robots.GANDALF, defaultUser);
    assertTrue(lobby.getRobotSelected(Robots.GANDALF));
    assertTrue(lobby.getRobotSelected(Robots.DUSTY));
    lobby.setRobotSelected(Robots.DUSTY, test2);
    assertFalse(lobby.getRobotSelected(Robots.DUSTY));
  }
  /** test if the method setPlayerRobot works */
  @Test
  void getPlayerRobot() {
    LobbyOptions lobbyOptions = new LobbyOptions();
    User test1 =
        new UserDTO(UUID.randomUUID().toString(), "test1", "1234", "test@web.de", new UserData(1));
    User test2 =
        new UserDTO(UUID.randomUUID().toString(), "test2", "1234", "test2@web.de", new UserData(1));
    Lobby lobby = new LobbyDTO("getRobotSelecetedTest", defaultUser, lobbyOptions);
    lobby.joinUser(test1);
    lobby.setRobotSelected(Robots.BOB, test1);
    lobby.joinUser(test2);
    lobby.setRobotSelected(Robots.BOB, defaultUser);
    assertTrue(lobby.isRobotSelected(defaultUser));
    assertFalse(lobby.isRobotSelected(test2));
  }
}
