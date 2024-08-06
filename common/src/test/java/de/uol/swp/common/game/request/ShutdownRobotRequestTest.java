package de.uol.swp.common.game.request;

import static org.junit.jupiter.api.Assertions.*;

import de.uol.swp.common.user.UserDTO;
import de.uol.swp.common.user.userdata.UserData;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ShutdownRobotRequestTest {
  String lobbyId = "dsfd";
  UserDTO user = new UserDTO(UUID.randomUUID().toString(), "ddef", "sd", "qe@.de", new UserData(2));

  ShutdownRobotRequest request = new ShutdownRobotRequest(lobbyId, user);

  @Test
  void getGameId() {
    assertEquals(lobbyId, request.getGameId());
  }

  @Test
  void getUser() {
    assertEquals(user, request.getUser());
  }

  @Test
  void testEquals() {
    ShutdownRobotRequest request1 = request;
    assertEquals(request1, request);
  }

  @Test
  void testHashCode() {
    ShutdownRobotRequest request1 = request;
    assertEquals(request1.hashCode(), request.hashCode());
  }
}
