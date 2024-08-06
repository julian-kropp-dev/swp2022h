package de.uol.swp.server.game;

import com.google.inject.Inject;
import de.uol.swp.common.game.Game;
import de.uol.swp.common.game.dto.GameDTO;
import de.uol.swp.common.lobby.Lobby;
import de.uol.swp.server.lobby.LobbyManagement;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Class for GameManagement, handles creation, deletion and storing of games. */
public class GameManagement {

  private static final Logger LOG = LogManager.getLogger(GameManagement.class);
  private static GameManagement instance = null;
  private final LobbyManagement lobbyManagement;
  private final Map<Optional<Lobby>, Game> games = new HashMap<>();

  /** Constructor. */
  @Inject
  @SuppressWarnings("java:S3010")
  public GameManagement() {
    LOG.debug("GameManagement created: {}{}", this, (instance == null) ? "" : " - DUPLICATION!");
    lobbyManagement = LobbyManagement.getInstance();
    instance = this;
  }

  /** Return to the old Instance of the GameManagement. */
  public static GameManagement getInstance() {
    if (instance == null) {
      instance = new GameManagement();
    }
    return instance;
  }

  /**
   * Creates a new game and adds it to the list. Note: The game owner should be equal to the lobby
   * owner.
   *
   * @param lobby the given lobby.
   */
  @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
  public void createGame(Optional<Lobby> lobby) {
    if (lobby.isPresent() && games.containsKey(lobby)) {
      throw new IllegalArgumentException(
          "Lobby " + lobby.get().getLobbyId() + " is already assigned to a game!");
    } else if (lobby.isPresent()) {
      GameDTO game = new GameDTO(lobby.get());
      games.put(lobby, game);
    }
  }

  /**
   * Creates a new game and adds it to the list. For test purpose
   *
   * @param lobby the given lobby.
   * @param game the given game.
   */
  @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
  public void createGameFromGameDTO(Lobby lobby, Game game) {
    if (games.containsKey(Optional.ofNullable(lobby))) {
      if (lobby != null) {
        throw new IllegalArgumentException(
            "Lobby " + lobby.getLobbyId() + " is already assigned to a game!");
      }
    } else {
      games.put(Optional.ofNullable(lobby), game);
    }
  }

  /**
   * Getter for all games.
   *
   * @return Map with all games
   */
  public Map<Optional<Lobby>, Game> getGames() {
    return games;
  }

  /**
   * Searches for the game of the lobby with the given lobbyId.
   *
   * @param lobbyId String containing the name of the lobby to search for
   * @return either empty Optional or Optional containing the game
   * @see Optional
   */
  @SuppressWarnings("java:S2629")
  public Optional<Game> getGame(String lobbyId) {
    Optional<Lobby> lobbyOptional = lobbyManagement.getLobby(lobbyId);
    if (lobbyOptional.isPresent()) {
      Game game = games.get(lobbyOptional);
      if (game != null) {
        return Optional.of(game);
      }
      LOG.debug("[{}] Game for lobby {} does not exist!", callingMethodName(), lobbyId);
    } else {
      LOG.debug("[{}] Lobby {} for game does not exist!", callingMethodName(), lobbyId);
    }
    return Optional.empty();
  }

  /**
   * Deletes game with requested name.
   *
   * @param name String containing the name of the lobby to delete
   * @throws IllegalArgumentException there exists no lobby with the requested name
   */
  public void dropGame(String name) {
    Optional<Lobby> lobbyOptional = lobbyManagement.getLobby(name);
    if (!games.containsKey(lobbyOptional)) {
      throw new NoSuchElementException("Game " + name + " konnte nicht gefunden werden!");
    }
    games.remove(lobbyOptional);
  }

  public void addGame(Game game) {
    games.put(lobbyManagement.getLobby(game.getGameId()), game);
  }

  // -------------------------------------------------------------------------------
  // Helper methods
  // -------------------------------------------------------------------------------
  private String callingMethodName() {
    StackTraceElement[] stackTrace = new Exception().getStackTrace();
    int i = 1;
    String calledMethodName = stackTrace[i++].getMethodName();
    while (i < stackTrace.length) {
      String callingMethodName = stackTrace[i++].getMethodName();
      if (!callingMethodName.equals(calledMethodName)) {
        return callingMethodName;
      }
    }
    return calledMethodName;
  }
}
