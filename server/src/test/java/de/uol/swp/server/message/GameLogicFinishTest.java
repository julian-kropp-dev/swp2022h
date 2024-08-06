package de.uol.swp.server.message;

import static org.junit.jupiter.api.Assertions.*;

import de.uol.swp.common.game.dto.Step;
import de.uol.swp.common.lobby.Lobby;
import de.uol.swp.common.lobby.dto.LobbyDTO;
import de.uol.swp.common.user.UserDTO;
import de.uol.swp.common.user.userdata.UserData;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class GameLogicFinishTest {
  String gameId = "ssss";
  Step step = new Step(gameId);
  Lobby lobby =
      new LobbyDTO(
          gameId,
          new UserDTO(UUID.randomUUID().toString(), "sad", "sd", "qe@.de", new UserData(2)));
  GameLogicFinish gameLogicFinish = new GameLogicFinish(step, lobby);

  @Test
  void getLobby() {
    assertEquals(lobby, gameLogicFinish.getLobby());
  }

  @Test
  void getStep() {
    assertEquals(step, gameLogicFinish.getStep());
  }

  @Test
  void testEquals() {
    String gameId = "eeee";
    Step step = new Step(gameId);
    Lobby lobby =
        new LobbyDTO(
            gameId,
            new UserDTO(UUID.randomUUID().toString(), "sad", "sd", "qe@.de", new UserData(2)));
    GameLogicFinish gameLogicFinish1 = new GameLogicFinish(step, lobby);

    assertEquals(gameLogicFinish, gameLogicFinish);
    assertNotEquals(gameLogicFinish1, gameLogicFinish);
  }

  @Test
  void testHashCode() {
    GameLogicFinish gameLogicFinish1 = gameLogicFinish;
    assertEquals(gameLogicFinish.hashCode(), gameLogicFinish1.hashCode());
  }
}
