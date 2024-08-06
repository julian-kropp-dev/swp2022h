package de.uol.swp.client.lobby.events;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Test for the event used to show the LobbyErrorEvent alert
 *
 * @see de.uol.swp.client.lobby.events.LobbyErrorEvent
 */
class LobbyErrorEventTest {

  /**
   * Test for the creation of LobbyErrorEvents
   *
   * <p>This test checks if the error message of the LobbyErrorEvent gets set correctly during the
   * creation of a new event
   */
  @Test
  void createLobbyErrorEvent() {
    LobbyErrorEvent event = new LobbyErrorEvent("Test");

    assertEquals("Test", event.getMessage());
  }
}
