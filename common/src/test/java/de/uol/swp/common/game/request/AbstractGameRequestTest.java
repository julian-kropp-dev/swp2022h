package de.uol.swp.common.game.request;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class AbstractGameRequestTest {
  String username = "user";
  String lobbyId = "ssss";
  RequestType type = RequestType.STOP_TIMER;
  long timestamp = 12343123;
  AbstractGameRequest request = new AbstractGameRequest(username, lobbyId, type, timestamp);

  @Test
  void getTimeStamp() {
    assertEquals(timestamp, request.getTimeStamp());
    assertNotEquals(234243, request.getTimeStamp());
  }

  @Test
  void getUsername() {
    assertEquals(username, request.getUsername());
    assertNotEquals("234243", request.getUsername());
  }

  @Test
  void getGameId() {
    assertEquals(lobbyId, request.getGameId());
    assertNotEquals("234243", request.getGameId());
  }

  @Test
  void testEquals() {
    AbstractGameRequest request1 =
        new AbstractGameRequest("user1", "game1", RequestType.STOP_TIMER, timestamp);
    AbstractGameRequest request4 =
        new AbstractGameRequest("user1", "game2", RequestType.STOP_TIMER, timestamp);
    AbstractGameRequest request5 =
        new AbstractGameRequest("user1", "game1", RequestType.CARD_SELECTION, timestamp);

    assertEquals(request1, request1);

    assertNotEquals(request1, request4);
    assertNotEquals(request1, request5);
  }

  @Test
  void testHashCode() {
    AbstractGameRequest request1 = request;
    assertEquals(request1.hashCode(), request.hashCode());
  }
}
