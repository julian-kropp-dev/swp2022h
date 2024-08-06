package de.uol.swp.server.lobby;

import de.uol.swp.common.lobby.Lobby;
import de.uol.swp.common.lobby.LobbyOptions;
import de.uol.swp.common.lobby.dto.LobbyDTO;
import de.uol.swp.common.user.User;
import de.uol.swp.server.game.GameManagement;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Manages creation, deletion and storing of lobbies.
 *
 * @see de.uol.swp.common.lobby.Lobby
 * @see de.uol.swp.common.lobby.dto.LobbyDTO
 */
public class LobbyManagement {

  private static final Logger LOG = LogManager.getLogger(LobbyManagement.class);
  private static LobbyManagement instance = null;

  @SuppressWarnings({"java:S1450", "FieldCanBeLocal"})
  private static GameManagement gameManagement;

  private final Map<String, Lobby> lobbies = new ConcurrentHashMap<>();

  /** Constructor. */
  @SuppressWarnings("java:S3010")
  public LobbyManagement() {
    LOG.debug("LobbyManagement created: {}{}", this, (instance == null) ? "" : " - DUPLICATION!");
    instance = this;
  }

  /**
   * This method give a singleton instance of the LobbyManagement class back. If an instance of the
   * class has not been created before, a new instance is created and returned. Otherwise, the
   * existing instance is returned.
   *
   * @return the singleton instance of the LobbyManagement class
   */
  public static LobbyManagement getInstance() {
    if (instance == null) {
      instance = new LobbyManagement();
      return instance;
    }
    return instance;
  }

  /**
   * Creates a new lobby and adds it to the list.
   *
   * @implNote the primary key of the lobbies is the id therefore the id has to be unique
   * @param id the id of the lobby to create
   * @param owner the user who wants to create a lobby
   * @see de.uol.swp.common.user.User
   * @throws IllegalArgumentException id already taken
   */
  public void createLobby(String id, User owner, LobbyOptions options) {
    if (lobbies.containsKey(id)) {
      throw new IllegalArgumentException("Lobby " + id + " existiert bereits!");
    }
    LobbyDTO lobby = new LobbyDTO(id, owner, options);
    lobby.setLobbyStatus(LobbyOptions.LobbyStatus.WAITING);
    lobbies.put(id, lobby);
  }

  /**
   * Deletes lobby with requested id.
   *
   * @param id String containing the id of the lobby to delete
   * @throws IllegalArgumentException there exists no lobby with the requested id
   */
  @SuppressWarnings("java:S2696")
  public void dropLobby(String id) {
    if (!lobbies.containsKey(id)) {
      throw new IllegalArgumentException("Lobby " + id + " konnte nicht gefunden werden!");
    }
    lobbies.remove(id);
    if (GameManagement.getInstance() != null) {
      gameManagement = GameManagement.getInstance();

      if (gameManagement.getGame(id).isPresent()) {
        gameManagement.dropGame(id);
      }
    }
  }

  /**
   * Searches for the lobby with the requested id.
   *
   * @param id String containing the id of the lobby to search for
   * @return either empty Optional or Optional containing the lobby
   * @see Optional
   */
  public Optional<Lobby> getLobby(String id) {
    if (id != null) {
      Lobby lobby = lobbies.get(id);
      if (lobby != null) {
        return Optional.of(lobby);
      }
    }
    return Optional.empty();
  }

  @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
  public LobbyDTO getLobbyDTO(String id) {
    Lobby lobby = lobbies.get(id);
    return (LobbyDTO) lobby;
  }

  /**
   * Retrieves all Lobbies.
   *
   * @return Collection with Lobby Objects
   */
  public Collection<Lobby> retrieveAllLobbies() {
    return lobbies.values();
  }

  /**
   * Retrieves all public Lobbies.
   *
   * @return Collection with Public Lobby Objects
   */
  public Collection<Lobby> retrieveAllPublicLobbies() {
    Map<String, Lobby> publicLobbies = new HashMap<>();
    lobbies.entrySet().stream()
        .filter(l -> !l.getValue().getOptions().isPrivateLobby())
        .forEach(l -> publicLobbies.put(l.getKey(), lobbies.get(l.getKey())));
    return publicLobbies.values();
  }
}
