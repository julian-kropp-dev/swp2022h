package de.uol.swp.server.communication;

import static org.junit.jupiter.api.Assertions.*;

import de.uol.swp.common.user.User;
import de.uol.swp.common.user.UserDTO;
import de.uol.swp.common.user.userdata.UserData;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UUIDSessionTest {
  private UUIDSession session;
  private User user;

  @BeforeEach
  void setUp() {
    user = new UserDTO("12342134", "qwerqewr", "password", "mail@.de", new UserData(2));
    session = (UUIDSession) UUIDSession.create(user);
  }

  @Test
  void getSessionId_newSession_returnsValidUUID() {
    String sessionId = session.getSessionId();

    assertNotNull(sessionId);
    assertTrue(isValidUUID(sessionId));
  }

  @Test
  void getUser_sessionWithUser_returnsUser() {
    User result = session.getUser();

    assertNotNull(result);
    assertEquals(user, result);
  }

  private boolean isValidUUID(String uuidString) {
    try {
      UUID.fromString(uuidString);
      return true;
    } catch (IllegalArgumentException e) {
      return false;
    }
  }
}
