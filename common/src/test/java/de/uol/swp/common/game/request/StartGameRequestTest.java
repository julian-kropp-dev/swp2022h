package de.uol.swp.common.game.request;

import static org.junit.jupiter.api.Assertions.*;

import de.uol.swp.common.user.UserDTO;
import de.uol.swp.common.user.userdata.UserData;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class StartGameRequestTest {
  String lobbyId = "dsfd";
  UserDTO user = new UserDTO(UUID.randomUUID().toString(), "ddef", "sd", "qe@.de", new UserData(2));
  StartGameRequest request = new StartGameRequest(lobbyId, user, true);
  StartGameRequest requestFalse = new StartGameRequest(lobbyId, user);

  @Test
  void isWithCommandStarted() {
    assertTrue(request.isWithCommandStarted());
    assertFalse(requestFalse.isWithCommandStarted());
  }

  @Test
  void testEquals() {
    assertNotEquals(requestFalse, request);
    assertEquals(request, request);
  }

  @Test
  void testHashCode() {
    assertEquals(request.hashCode(), request.hashCode());
    assertNotEquals(request.hashCode(), requestFalse.hashCode());
  }
}
