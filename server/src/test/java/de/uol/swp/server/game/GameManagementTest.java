package de.uol.swp.server.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.uol.swp.common.game.floor.Direction;
import de.uol.swp.common.game.floor.FloorPlanSetting;
import de.uol.swp.common.game.floor.FloorPlanSetting.FloorPlans;
import de.uol.swp.common.game.robot.Robots;
import de.uol.swp.common.lobby.Lobby;
import de.uol.swp.common.lobby.dto.LobbyDTO;
import de.uol.swp.common.user.User;
import de.uol.swp.common.user.UserDTO;
import de.uol.swp.common.user.userdata.UserData;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * The GameManagementTest class represents a unit test for the GameManagement class, which is
 * responsible for managing games on the server.
 */
class GameManagementTest {
  private static final User defaultUser =
      new UserDTO(
          UUID.randomUUID().toString(), "julian", "julian", "julian@swp.de", new UserData(1));
  private static final FloorPlanSetting[] floorPlanSettings = {
    new FloorPlanSetting(FloorPlans.CROSS), new FloorPlanSetting(FloorPlans.EMPTY),
    new FloorPlanSetting(FloorPlans.EMPTY), new FloorPlanSetting(FloorPlans.EMPTY)
  };
  private static final Direction[] floorPlansOnlyDirection =
      new Direction[] {
        Direction.NORTH, Direction.NORTH, Direction.NORTH, Direction.NORTH,
      };
  private static final GameManagement gameManagement = new GameManagement();
  private final Optional<Lobby> lobby = Optional.of(new LobbyDTO("qm34", defaultUser));

  /**
   * Tests the creation of a new game. Asserts that the list of games managed by gameManagement is
   * initially empty, creates a new game using the createGame method with the given lobby, and then
   * asserts that the list of games is no longer empty. Additionally, it checks that the game's
   * name, owner's username, password, and email are as expected.
   */
  @Test
  void createGameTest() {
    assertTrue(gameManagement.getGames().isEmpty());
    lobby.get().setRobotSelected(Robots.DUSTY, defaultUser);
    lobby.get().getOptions().setFloorPlanSettings(floorPlanSettings);
    gameManagement.createGame(lobby);
    assertFalse(gameManagement.getGames().isEmpty());
    assertEquals("qm34", gameManagement.getGames().get(lobby).getGameId());
    assertEquals(
        "julian", gameManagement.getGames().get(lobby).getLobby().getOwner().getUsername());
    assertEquals(
        "julian", gameManagement.getGames().get(lobby).getLobby().getOwner().getPassword());
    assertEquals(
        "julian@swp.de", gameManagement.getGames().get(lobby).getLobby().getOwner().getMail());
  }
}
