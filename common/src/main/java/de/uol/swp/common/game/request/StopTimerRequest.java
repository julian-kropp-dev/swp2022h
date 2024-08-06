package de.uol.swp.common.game.request;

/** Request to stop the timer of the card selection. */
public class StopTimerRequest extends AbstractGameRequest {

  public StopTimerRequest(String username, String lobbyId) {
    super(username, lobbyId, RequestType.STOP_TIMER, System.currentTimeMillis());
  }
}
