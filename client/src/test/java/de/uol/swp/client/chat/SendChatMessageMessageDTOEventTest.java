package de.uol.swp.client.chat;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.uol.swp.common.chat.ChatMessageDTO;
import de.uol.swp.common.user.UserDTO;
import de.uol.swp.common.user.userdata.UserData;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class SendChatMessageMessageDTOEventTest {
  private final UserDTO testuser =
      new UserDTO(
          UUID.randomUUID().toString(), "testuser", "testuser", "testuser", new UserData(1));
  private final String testMessage = "testMassage";
  private final String lobbyId = "TESTNAME";
  private final ChatMessageDTO testChatMessageDTO =
      new ChatMessageDTO(testuser, testMessage, lobbyId);

  @Test
  void getMassage() {
    assertEquals(testChatMessageDTO.getMessage(), testMessage);
  }
}
