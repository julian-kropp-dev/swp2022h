package de.uol.swp.server.message;

import static org.junit.jupiter.api.Assertions.*;

import de.uol.swp.common.user.UserDTO;
import de.uol.swp.common.user.userdata.UserData;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ReplaceUserWithAiMessageTest {
  String lobbyId = "dsfd";
  UserDTO user = new UserDTO(UUID.randomUUID().toString(), "ddef", "sd", "qe@.de", new UserData(2));
  ReplaceUserWithAiMessage message = new ReplaceUserWithAiMessage(user, lobbyId);

  @Test
  void getUser() {
    assertEquals(user, message.getUser());
  }

  @Test
  void getLobbyId() {
    assertEquals(lobbyId, message.getLobbyId());
  }

  @Test
  void testEquals() {
    ReplaceUserWithAiMessage message1 = message;
    assertEquals(message1, message);
  }

  @Test
  void testHashCode() {
    ReplaceUserWithAiMessage message1 = message;
    assertEquals(message1.hashCode(), message.hashCode());
  }
}
