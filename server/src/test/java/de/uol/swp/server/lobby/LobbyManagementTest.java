package de.uol.swp.server.lobby;

import static org.junit.jupiter.api.Assertions.*;

import de.uol.swp.common.lobby.Lobby;
import de.uol.swp.common.lobby.LobbyOptions;
import de.uol.swp.common.lobby.LobbyOptions.LobbyStatus;
import de.uol.swp.common.lobby.dto.LobbyDTO;
import de.uol.swp.common.user.User;
import de.uol.swp.common.user.UserDTO;
import de.uol.swp.common.user.userdata.UserData;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Unit tests for the LobbyManagement class. */
class LobbyManagementTest {

  private LobbyManagement lobbyManagement;
  private User user;
  private LobbyOptions options;
  private LobbyOptions options2;

  @BeforeEach
  void setUp() {
    lobbyManagement = new LobbyManagement();
    user =
        new UserDTO(
            UUID.randomUUID().toString(), "Julian", "test", "julian@test.de", new UserData(1));
    options = new LobbyOptions();
    options.setLobbyStatus(LobbyStatus.INGAME);
    options.setSlot(3);
    options.setLobbyTitle("testLobby");
    options.setPrivateLobby(false);
    options.setActiveLasers(false);
    options.setAiPlayerCount(2);
    options.setTurnLimit(8);
    options2 = new LobbyOptions();
    options2.setPrivateLobby(true);
  }

  /** Tests creating a new lobby. */
  @Test
  void testCreateLobby() {
    lobbyManagement.createLobby(options.getLobbyTitle(), user, options);
    Optional<Lobby> retrievedLobby = lobbyManagement.getLobby("testLobby");
    assertTrue(retrievedLobby.isPresent());
    assertEquals("testLobby", retrievedLobby.get().getLobbyId());
    assertEquals(user, retrievedLobby.get().getOwner());
    assertEquals(options, retrievedLobby.get().getOptions());
  }

  /** Tests creating a new lobby with a duplicate name. */
  @Test
  void testCreateLobbyDuplicateLobbyId() {
    lobbyManagement.createLobby("testLobby", user, options);
    assertThrows(
        IllegalArgumentException.class,
        () -> lobbyManagement.createLobby("testLobby", user, options));
  }

  /** Tests dropping an existing lobby. */
  @Test
  void testDropLobby() {
    lobbyManagement.createLobby("testLobby", user, options);
    lobbyManagement.dropLobby("testLobby");
    Optional<Lobby> retrievedLobby = lobbyManagement.getLobby("testLobby");
    assertFalse(retrievedLobby.isPresent());
  }

  /** Tests dropping a non-existent lobby. */
  @Test
  void testDropLobbyNonExistentLobbyId() {
    assertThrows(IllegalArgumentException.class, () -> lobbyManagement.dropLobby("testLobby"));
  }

  /** Tests retrieving all lobbies. */
  @Test
  void testRetrieveAllLobbys() {
    lobbyManagement.createLobby("testLobby", user, options);
    lobbyManagement.createLobby("testLobby2", user, options);
    Collection<Lobby> allLobbies = lobbyManagement.retrieveAllLobbies();
    assertEquals(2, allLobbies.size());
    assertTrue(allLobbies.stream().anyMatch(l -> l.getLobbyId().equals("testLobby")));
    assertTrue(allLobbies.stream().anyMatch(l -> l.getLobbyId().equals("testLobby")));
  }

  /** Tests retrieving all public lobbies. */
  @Test
  void testRetrieveAllPublicLobbys() {
    lobbyManagement.createLobby("testLobby", user, options);
    lobbyManagement.createLobby("testLobby2", user, options2);
    Collection<Lobby> publicLobbies = lobbyManagement.retrieveAllPublicLobbies();
    assertEquals(1, publicLobbies.size());
    assertTrue(publicLobbies.stream().anyMatch(l -> l.getLobbyId().equals("testLobby")));
    assertFalse(publicLobbies.stream().anyMatch(l -> l.getLobbyId().equals("testLobby2")));
  }

  /** Unit test for the getLobby method of the LobbyManagement class. */
  @Test
  void testGetLobby() {
    // Test with an existing lobby
    Lobby lobby = new LobbyDTO("test", user, options);
    lobbyManagement.createLobby(lobby.getLobbyId(), lobby.getOwner(), lobby.getOptions());
    assertTrue(lobbyManagement.getLobby("test").isPresent());
    Optional<Lobby> result = lobbyManagement.getLobby("test");
    result.ifPresent(value -> assertEquals(lobby.getLobbyId(), value.getLobbyId()));

    // Test with a non-existing lobby
    assertFalse(lobbyManagement.getLobby("test42").isPresent());
    Optional<Lobby> result2 = lobbyManagement.getLobby("test42");
    result2.ifPresent(value -> assertNotSame(lobby.getLobbyId(), value.getLobbyId()));
  }
}
