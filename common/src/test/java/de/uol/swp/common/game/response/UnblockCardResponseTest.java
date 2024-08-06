package de.uol.swp.common.game.response;

import static org.junit.jupiter.api.Assertions.*;

import de.uol.swp.common.user.UserDTO;
import de.uol.swp.common.user.userdata.UserData;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class UnblockCardResponseTest {
  String gameId = "rwrew";
  boolean[] blocked = new boolean[] {true, false, false};
  UserDTO user = new UserDTO(UUID.randomUUID().toString(), "ddef", "sd", "qe@.de", new UserData(2));
  UnblockCardResponse response = new UnblockCardResponse(user, gameId, blocked);

  @Test
  void getUser() {
    assertEquals(user, response.getUser());
  }

  @Test
  void getGameId() {
    assertEquals(gameId, response.getGameId());
  }

  @Test
  void getBlocked() {
    assertEquals(blocked, response.getBlocked());
  }

  @Test
  void testEquals() {
    UnblockCardResponse response1 = response;
    assertEquals(response1, response);
  }

  @Test
  void testHashCode() {
    UnblockCardResponse response1 = response;
    assertEquals(response1.hashCode(), response.hashCode());
  }
}
