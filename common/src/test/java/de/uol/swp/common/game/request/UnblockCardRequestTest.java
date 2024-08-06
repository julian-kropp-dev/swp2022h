package de.uol.swp.common.game.request;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class UnblockCardRequestTest {
  String lobbyId = "dsfd";
  String username = "ssss";
  int toUnblock = 2;
  UnblockCardRequest request = new UnblockCardRequest(username, lobbyId, toUnblock);

  @Test
  void getUsername() {
    assertEquals(username, request.getUsername());
  }

  @Test
  void getGameId() {
    assertEquals(lobbyId, request.getGameId());
  }

  @Test
  void getToUnblock() {
    assertEquals(toUnblock, request.getToUnblock());
  }

  @Test
  void testEquals() {
    UnblockCardRequest request1 = request;
    assertEquals(request1, request);
  }

  @Test
  void testHashCode() {
    UnblockCardRequest request1 = request;
    assertEquals(request1.hashCode(), request.hashCode());
  }
}
