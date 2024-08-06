package de.uol.swp.client.game.events;

import de.uol.swp.common.game.response.ValidateCardSelectionResponse;

/**
 * The response of a ValidateCardSelectionRequest in form of an event where the client can react to.
 */
public class ValidateCardSelectionResponseEvent {

  private final ValidateCardSelectionResponse response;

  public ValidateCardSelectionResponseEvent(ValidateCardSelectionResponse response) {
    this.response = response;
  }

  public ValidateCardSelectionResponse getResponse() {
    return response;
  }
}
