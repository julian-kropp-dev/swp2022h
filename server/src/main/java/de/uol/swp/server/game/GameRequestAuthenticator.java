package de.uol.swp.server.game;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The GameRequestAuthenticator class is used to check if the received request is still valid by
 * checking the requestTimestamp.
 */
public class GameRequestAuthenticator {

  private static final Logger LOG = LogManager.getLogger(GameRequestAuthenticator.class);
  private final long requestTimeoutThreshold;
  private final GameService gameService;

  public GameRequestAuthenticator(GameService gameService, long requestTimeoutThreshold) {
    this.gameService = gameService;
    this.requestTimeoutThreshold = requestTimeoutThreshold;
  }

  /**
   * This method checks if the received request is still valid by checking the requestTimestamp.
   *
   * @param username name of the user sending the request
   * @param lobbyId name of the lobby
   * @param requestTimestamp timestamp of the request
   * @return boolean Is the request still valid
   */
  public boolean isRequestValid(String username, String lobbyId, long requestTimestamp) {
    if (!isTimestampValid(requestTimestamp)) {
      LOG.debug(
          "Request TOO LATE for game {} from user {} (Threshold: {})",
          lobbyId,
          username,
          requestTimeoutThreshold);
      return false;
    }
    return gameService
        .getGame(lobbyId)
        .map(
            game ->
                game.getPlayers().stream().anyMatch(player -> player.getName().equals(username)))
        .isPresent();
  }

  /**
   * Tiny method to check if the given requestTimestamp is valid.
   *
   * @param requestTimestamp timestamp of the request
   * @return boolean Is the request still in time
   */
  private boolean isTimestampValid(long requestTimestamp) {
    return System.currentTimeMillis() - requestTimestamp <= requestTimeoutThreshold;
  }
}
