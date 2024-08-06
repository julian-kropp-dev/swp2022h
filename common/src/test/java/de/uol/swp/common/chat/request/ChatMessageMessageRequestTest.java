package de.uol.swp.common.chat.request;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.uol.swp.common.chat.ChatMessageDTO;
import de.uol.swp.common.game.GameOptions;
import de.uol.swp.common.user.UserDTO;
import de.uol.swp.common.user.userdata.UserData;
import java.util.UUID;
import org.junit.jupiter.api.Test;

@SuppressWarnings("ALL")
class ChatMessageMessageRequestTest {

  private final UserDTO testUser =
      new UserDTO(
          UUID.randomUUID().toString(),
          "testUser",
          "testPassword",
          "test@email.com",
          new UserData(1));
  private final String testMessage = "testMessage";
  private final String testLobby = "testLobby";
  private final ChatMessageDTO chatMessageDTO =
      new ChatMessageDTO(testUser, testMessage, testLobby);
  private final ChatMessageRequest chatMessageRequest = new ChatMessageRequest(chatMessageDTO);

  @Test
  void getMassage() {
    assertEquals(chatMessageDTO, chatMessageRequest.getMessage());
  }

  /** This test checks if the implementation of the equals method in the class is correct. */
  @Test
  @SuppressWarnings("java:S5785")
  void equalsTest() {
    ChatMessageRequest msg = new ChatMessageRequest(chatMessageDTO);
    ChatMessageRequest msg2 = msg;
    Object msg3 = msg;
    ChatMessageRequest msg4 =
        new ChatMessageRequest(new ChatMessageDTO(testUser, "Hallo 123", "Abcd"));
    assertTrue(msg.equals(msg2));
    assertFalse(msg2.equals(new Object()));
    assertFalse(msg2.equals(null));
    assertFalse(msg2.equals(new GameOptions()));
    assertTrue(msg.equals(msg3));
    assertFalse(msg.equals(msg4));
  }
}
