package de.uol.swp.server.message;

import static org.junit.jupiter.api.Assertions.*;

import de.uol.swp.common.lobby.Lobby;
import de.uol.swp.common.lobby.dto.LobbyDTO;
import de.uol.swp.common.user.UserDTO;
import de.uol.swp.common.user.userdata.UserData;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PlayerLeftLobbyMessageTest {
  UserDTO user = new UserDTO(UUID.randomUUID().toString(), "ddef", "sd", "qe@.de", new UserData(2));

  Lobby lobby =
      new LobbyDTO(
          "dddd",
          new UserDTO(UUID.randomUUID().toString(), "sad", "sd", "qe@.de", new UserData(2)));

  PlayerLeftLobbyMessage message = new PlayerLeftLobbyMessage(lobby, user);

  @Test
  void getLobby() {
    assertEquals(lobby, message.getLobby());
  }

  @Test
  void getUser() {
    assertEquals(user, message.getUser());
  }

  @Test
  void testEquals() {
    PlayerLeftLobbyMessage message1 = message;
    assertEquals(message1, message);
  }

  @Test
  void testHashCode() {
    PlayerLeftLobbyMessage message1 = message;
    assertEquals(message1.hashCode(), message.hashCode());
  }
}
