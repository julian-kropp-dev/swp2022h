package de.uol.swp.common.lobby;

import static org.junit.jupiter.api.Assertions.assertTrue;

import de.uol.swp.common.SerializationTestHelper;
import de.uol.swp.common.lobby.message.UserJoinedLobbyMessage;
import de.uol.swp.common.lobby.message.UserLeftLobbyMessage;
import de.uol.swp.common.lobby.message.UserReadyMessage;
import de.uol.swp.common.lobby.request.CreateLobbyRequest;
import de.uol.swp.common.lobby.request.LobbyJoinUserRequest;
import de.uol.swp.common.lobby.request.LobbyLeaveUserRequest;
import de.uol.swp.common.user.UserDTO;
import de.uol.swp.common.user.userdata.UserData;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class LobbyMessageSerializableTest {

  private static final UserDTO defaultUser =
      new UserDTO(
          UUID.randomUUID().toString(), "marco", "marco", "marco@grawunder.de", new UserData(1));

  @Test
  void testLobbyMessagesSerializable() {
    assertTrue(
        SerializationTestHelper.checkSerializableAndDeserializable(
            new CreateLobbyRequest("test", defaultUser, new LobbyOptions()),
            CreateLobbyRequest.class));
    assertTrue(
        SerializationTestHelper.checkSerializableAndDeserializable(
            new LobbyJoinUserRequest("test", defaultUser), LobbyJoinUserRequest.class));
    assertTrue(
        SerializationTestHelper.checkSerializableAndDeserializable(
            new LobbyLeaveUserRequest("test", defaultUser), LobbyLeaveUserRequest.class));
    assertTrue(
        SerializationTestHelper.checkSerializableAndDeserializable(
            new UserJoinedLobbyMessage("test", defaultUser), UserJoinedLobbyMessage.class));
    assertTrue(
        SerializationTestHelper.checkSerializableAndDeserializable(
            new UserLeftLobbyMessage("test", defaultUser), UserLeftLobbyMessage.class));
    assertTrue(
        SerializationTestHelper.checkSerializableAndDeserializable(
            new UserReadyMessage("test", defaultUser, false), UserReadyMessage.class));
  }
}
