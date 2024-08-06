package de.uol.swp.common.lobby;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.uol.swp.common.game.floor.FloorPlanSetting;
import de.uol.swp.common.game.floor.FloorPlanSetting.FloorPlans;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Test class for the LobbyOptions. */
class LobbyOptionsTest {
  private LobbyOptions options;

  @BeforeEach
  void setUp() {
    options = new LobbyOptions();
  }

  @Test
  void toText() {
    assertEquals(
        "LobbyOptions: Slots: 2 | Private Lobby: false | TurnLimit: 20000 | Laser aktiv: false | WeakDuplicated: false | SwitchOff: false",
        options.toText());
    options.setSlot(3);
    options.setTurnLimit(2);
    options.setTurnLimit(-2);
    assertEquals(
        "LobbyOptions: Slots: 3 | Private Lobby: false | TurnLimit: 2 | Laser aktiv: false | WeakDuplicated: false | SwitchOff: false",
        options.toText());
  }

  @Test
  void getSlot() {
    assertNotEquals(9, options.getSlot());
    assertEquals(2, options.getSlot());
  }

  @Test
  void setSlot() {
    assertTrue(options.setSlot(3));
    assertTrue(options.setSlot(6));
    assertFalse(options.setSlot(0));
    assertFalse(options.setSlot(9));
    assertTrue(options.setSlot(4));
    assertEquals(4, options.getSlot());
  }

  @Test
  void getAiPlayerCount() {
    assertEquals(0, options.getAiPlayerCount());
  }

  @Test
  void setAiPlayerCount() {
    assertTrue(options.setAiPlayerCount(3));
    assertEquals(3, options.getAiPlayerCount());
    assertFalse(options.setAiPlayerCount(-3));
  }

  @Test
  void isPrivateLobby() {
    assertFalse(options.isPrivateLobby());
  }

  @Test
  void setPrivateLobby() {
    options.setPrivateLobby(true);
    assertTrue(options.isPrivateLobby());
  }

  @Test
  void getTurnLimit() {
    assertEquals(20000, options.getTurnLimit());
  }

  @Test
  void getTurnLimitString() {
    assertTrue(options.setTurnLimit("10s"));
    assertEquals(10000, options.getTurnLimit());
  }

  @Test
  void setTurnLimit() {
    options.setTurnLimit(15000);
    assertEquals(15000, options.getTurnLimit());
    assertFalse(options.setTurnLimit(-3));
  }

  @Test
  void testSetTurnLimit() {
    assertTrue(options.setTurnLimit("20s"));
    assertEquals(20000, options.getTurnLimit());
    assertFalse(options.setTurnLimit(-2));
  }

  @Test
  void setLobbyStatus() {
    options.setLobbyStatus(LobbyOptions.LobbyStatus.STARTING);
    assertEquals(LobbyOptions.LobbyStatus.STARTING, options.getLobbyStatus());
  }

  @Test
  void getLobbyStatus() {
    assertEquals(LobbyOptions.LobbyStatus.WAITING, options.getLobbyStatus());
  }

  @Test
  void setLobbyTitle() {
    assertFalse(options.setLobbyTitle(""));
    assertTrue(options.setLobbyTitle("Hey"));
  }

  @Test
  void getLobbyTitle() {
    assertEquals("Lobby", options.getLobbyTitle());
  }

  @Test
  void isActiveLasers() {
    assertFalse(options.isActiveLasers());
    options.setActiveLasers(true);
    assertTrue(options.isActiveLasers());
  }

  /** This test tests all methods with invalid values. */
  @Test
  void testInvalidValues() {
    assertFalse(options.setSlot(0));
    assertFalse(options.setSlot(9));

    assertFalse(options.setAiPlayerCount(-1));
    assertFalse(options.setAiPlayerCount(9));

    options.setTurnLimit("invalid");
    assertEquals(20000, options.getTurnLimit());
  }

  /**
   * This test tests if the correct FloorPlan has been loaded in the LobbyOptions and also if it's
   * valid. Direction is not important, so it's set all NORTH here.
   */
  @Test
  void getFloorPlanSettingsTest() {
    // Because this plan is invalid there should be EMPTY FloorPlans in the LobbyOptions.
    FloorPlanSetting[] invalidPlan1 = {
      new FloorPlanSetting(FloorPlans.EMPTY), new FloorPlanSetting(FloorPlans.CROSS),
      new FloorPlanSetting(FloorPlans.EXCHANGE), new FloorPlanSetting(FloorPlans.EMPTY)
    };
    options.setFloorPlanSettings(invalidPlan1);
    for (int i = 0; i < 4; i++) {
      assertEquals(
          options.getFloorPlanSettings()[i].getFloorPlansEnum().toString(),
          FloorPlans.EMPTY.toString());
    }

    // This is a correct plan. It should be stored in LobbyOptions.
    FloorPlanSetting[] plan1 = {
      new FloorPlanSetting(FloorPlans.EMPTY), new FloorPlanSetting(FloorPlans.CROSS),
      new FloorPlanSetting(FloorPlans.EMPTY), new FloorPlanSetting(FloorPlans.EMPTY)
    };
    options.setFloorPlanSettings(plan1);
    for (int i = 0; i < 4; i++) {
      assertEquals(
          options.getFloorPlanSettings()[i].getFloorPlansEnum().toString(),
          plan1[i].getFloorPlansEnum().toString());
    }

    // This is a correct plan. It replaces plan1.
    FloorPlanSetting[] plan2 = {
      new FloorPlanSetting(FloorPlans.CROSS), new FloorPlanSetting(FloorPlans.EXCHANGE),
      new FloorPlanSetting(FloorPlans.ISLAND), new FloorPlanSetting(FloorPlans.MAELSTROM)
    };
    options.setFloorPlanSettings(plan2);
    for (int i = 0; i < 4; i++) {
      assertEquals(
          options.getFloorPlanSettings()[i].getFloorPlansEnum().toString(),
          plan2[i].getFloorPlansEnum().toString());
    }

    // Because this plan is invalid, so there should still be plan2 in the LobbyOptions.
    FloorPlanSetting[] invalidPlan2 = {
      new FloorPlanSetting(FloorPlans.EMPTY), new FloorPlanSetting(FloorPlans.CROSS),
      new FloorPlanSetting(FloorPlans.EXCHANGE), new FloorPlanSetting(FloorPlans.EMPTY)
    };
    options.setFloorPlanSettings(invalidPlan2);
    for (int i = 0; i < 4; i++) {
      assertEquals(
          options.getFloorPlanSettings()[i].getFloorPlansEnum().toString(),
          plan2[i].getFloorPlansEnum().toString());
    }
  }
}
