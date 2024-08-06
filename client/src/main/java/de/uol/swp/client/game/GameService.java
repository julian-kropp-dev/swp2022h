package de.uol.swp.client.game;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import de.uol.swp.client.game.events.ValidateCardSelectionResponseEvent;
import de.uol.swp.common.game.card.ProgrammingCard;
import de.uol.swp.common.game.request.AbstractGameRequest;
import de.uol.swp.common.game.request.GameDTORequest;
import de.uol.swp.common.game.request.UnblockCardRequest;
import de.uol.swp.common.game.request.ValidateCardSelectionRequest;
import de.uol.swp.common.game.response.ValidateCardSelectionResponse;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Class that manages Games. */
@SuppressWarnings("UnstableApiUsage")
public class GameService {

  private final EventBus eventBus;
  private final Map<String, AbstractGameRequest> pendingRequests = new ConcurrentHashMap<>();

  /**
   * Constructor.
   *
   * @param eventBus The EventBus set in ClientModule
   * @see de.uol.swp.client.di.ClientModule
   */
  @Inject
  public GameService(EventBus eventBus) {
    this.eventBus = eventBus;
    this.eventBus.register(this);
  }

  /**
   * Posts a request for the GameDTO on the EventBus.
   *
   * @param gameId Name of the GameID (LobbyID)
   */
  @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
  public void retrieveGameDTO(String gameId) {
    eventBus.post(new GameDTORequest(gameId));
  }

  /**
   * Posts a ValidateCardSelectionRequest to the Eventbus.
   *
   * @param username name of the user sending the request
   * @param gameId id of the game the user resides in
   * @param selectedCards the array of cards the user selected
   */
  public void sendValidateCardSelectionRequest(
      String username, String gameId, ProgrammingCard[] selectedCards) {
    ValidateCardSelectionRequest request =
        new ValidateCardSelectionRequest(username, gameId, selectedCards);
    pendingRequests.put(request.getRequestId(), request);
    eventBus.post(request);
  }

  /**
   * Posts a UnblockCardRequest to the Eventbus.
   *
   * @param username name of the user sending the request
   * @param gameId id of the game the user resides in
   * @param selectedSlot selected slot to unblock
   */
  public void sendUnblockCardRequest(String username, String gameId, int selectedSlot) {
    UnblockCardRequest request = new UnblockCardRequest(username, gameId, selectedSlot);
    eventBus.post(request);
  }

  /**
   * Handles the response of a ValidateCardSelectionRequest. Creates an
   * ValidateCardSelectionResponseEvent and posts it to the Eventbus.
   *
   * @param response from the server
   */
  @Subscribe
  public void onValidateCardSelectionResponse(ValidateCardSelectionResponse response) {
    if (pendingRequests.containsKey(response.getRequestId())) {
      pendingRequests.remove(response.getRequestId());
      ValidateCardSelectionResponseEvent event = new ValidateCardSelectionResponseEvent(response);
      eventBus.post(event);
    }
  }
}
