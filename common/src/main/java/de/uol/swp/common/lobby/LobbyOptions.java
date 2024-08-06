package de.uol.swp.common.lobby;

import de.uol.swp.common.game.floor.FloorPlanSetting;
import de.uol.swp.common.game.floor.FloorPlanSetting.FloorPlans;
import de.uol.swp.common.helper.HelperMethods;
import java.awt.Point;
import java.io.Serializable;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Class containing all options for the lobby of a game. */
public class LobbyOptions implements Serializable {
  private static final long serialVersionUID = -6507265153864991052L;
  private static final Logger LOG = LogManager.getLogger(LobbyOptions.class);
  private int slot;
  private int aiPlayerCount;
  private boolean privateLobby;
  private LobbyStatus lobbyStatus;
  private String lobbyTitle;
  private int turnLimit;
  private boolean activeLasers;
  private int[] aiDifficulty;
  private boolean activeWeakDuplicated;
  private boolean switchOffRoboter;
  private FloorPlanSetting[] floorPlansSettings;
  private Map<Integer, EnumMap<FloorPlans, Point>> checkpointsPosition = new HashMap<>();
  private boolean spectatorModeActive;
  private int maxSpectators = 64;

  /** Default constructor with default values. All Option are set to their default value. */
  public LobbyOptions() {
    slot = 2;
    aiPlayerCount = 0;
    privateLobby = false;
    lobbyStatus = LobbyStatus.WAITING;
    lobbyTitle = "Lobby";
    turnLimit = 20000;
    activeLasers = false;
    activeWeakDuplicated = false;
    switchOffRoboter = false;
    aiDifficulty = new int[] {1, 1, 1, 1, 1, 1};
    floorPlansSettings =
        new FloorPlanSetting[] {
          new FloorPlanSetting(FloorPlans.EMPTY), new FloorPlanSetting(FloorPlans.EMPTY),
          new FloorPlanSetting(FloorPlans.EMPTY), new FloorPlanSetting(FloorPlans.EMPTY)
        };
    spectatorModeActive = true;
  }

  /**
   * Returns settings as human-readable string. Lobby title and status are excluded.
   *
   * @return Text
   */
  public String toText() {
    return "LobbyOptions: Slots: "
        + slot
        + " | Private Lobby: "
        + privateLobby
        + " | TurnLimit: "
        + turnLimit
        + " | Laser aktiv: "
        + activeLasers
        + " | WeakDuplicated: "
        + activeWeakDuplicated
        + " | SwitchOff: "
        + switchOffRoboter;
  }

  /**
   * Getter for slots.
   *
   * @return number of slots
   */
  public int getSlot() {
    return slot;
  }

  /**
   * Setter for slots checks if number is valid.
   *
   * @param slot the new number of slots
   * @return if the number is valid
   */
  public boolean setSlot(int slot) {
    if (slot < 1 || slot > 8) {
      return false;
    }
    this.slot = slot;
    return true;
  }

  /**
   * Getter for the aiPlayerCount.
   *
   * @return number of aiPlayCount
   */
  public int getAiPlayerCount() {
    return aiPlayerCount;
  }

  /**
   * Setter for the aiPlayerCount and checks if the given number is valid.
   *
   * @param aiPlayerCount the new number of aiPlayers
   * @return if the number is valid
   */
  public boolean setAiPlayerCount(int aiPlayerCount) {
    if (aiPlayerCount > 8 - slot || aiPlayerCount < 0) {
      return false;
    }
    this.aiPlayerCount = aiPlayerCount;
    return true;
  }

  /**
   * Getter for the privacy of the lobby.
   *
   * @return True if a lobby is private.
   */
  public boolean isPrivateLobby() {
    return privateLobby;
  }

  /**
   * Setter for the privacy of a lobby.
   *
   * <p>Set to true if a lobby should be private and only join able by its ID.
   *
   * @param privateLobby True if Lobby is private.
   */
  public void setPrivateLobby(boolean privateLobby) {
    this.privateLobby = privateLobby;
  }

  /**
   * Returns the max turn time.
   *
   * @return time in milliseconds
   */
  public int getTurnLimit() {
    return turnLimit;
  }

  /**
   * Returns the max turn time.
   *
   * @return turnLimit as String in Format XXs
   */
  public String getTurnLimitString() {
    int number = getTurnLimit() / 1000;
    String string = Integer.toString(number);
    return string + "s";
  }

  /**
   * Set the time for one turn.
   *
   * @param turnLimit in milliseconds
   */
  public boolean setTurnLimit(int turnLimit) {
    if (turnLimit < 0) {
      return false;
    }
    this.turnLimit = turnLimit;
    return true;
  }

  /**
   * Set the time for one turn.
   *
   * @param turnLimit as String in Seconds: XXs
   */
  public boolean setTurnLimit(String turnLimit) {
    turnLimit = turnLimit.replace("s", "");
    try {
      int number = Integer.parseInt(turnLimit);
      setTurnLimit(number * 1000);
      return number >= 0;
    } catch (NumberFormatException e) {
      LOG.debug("Invalid Number");
      return false;
    }
  }

  /**
   * Getter for the LobbyStatus.
   *
   * @return the LobbyStatus
   */
  public LobbyStatus getLobbyStatus() {
    return lobbyStatus;
  }

  /** Setter for the LobbyStatus. */
  public void setLobbyStatus(LobbyStatus lobbyStatus) {
    this.lobbyStatus = lobbyStatus;
  }

  /**
   * Setter for the Lobby Title.
   *
   * @param lobbyTitle LobbyTitle
   */
  public boolean setLobbyTitle(String lobbyTitle) {
    if (lobbyTitle.isEmpty()) {
      return false;
    }
    this.lobbyTitle = lobbyTitle;
    return true;
  }

  /**
   * Getter for the Lobby Title.
   *
   * @return Lobby Title
   */
  public String getLobbyTitle() {
    return lobbyTitle;
  }

  public boolean isActiveLasers() {
    return activeLasers;
  }

  public void setActiveLasers(boolean activeLasers) {
    this.activeLasers = activeLasers;
  }

  public int[] getAiDifficulty() {
    return this.aiDifficulty;
  }

  public void setAiDifficulty(int[] aiDifficulty) {
    this.aiDifficulty = aiDifficulty;
  }

  public boolean isWeakDuplicatedActive() {
    return activeWeakDuplicated;
  }

  public void setWeakDuplicatedActive(boolean activeWeakDuplicate) {
    this.activeWeakDuplicated = activeWeakDuplicate;
  }

  public boolean isSwitchOffRoboter() {
    return switchOffRoboter;
  }

  public void setSwitchOffRoboter(boolean switchOffRoboter) {
    this.switchOffRoboter = switchOffRoboter;
  }

  /**
   * Getter for the combined FloorPlan CSV-String. The String is in the right orientation as
   * specified by the user.
   *
   * @return CSV-String of the FloorPlan
   */
  public String getFloorPlanAsString() {
    return HelperMethods.convertFloorPlan(floorPlansSettings, checkpointsPosition);
  }

  /** Getter for FloorPlanSetting array. */
  public FloorPlanSetting[] getFloorPlanSettings() {
    return floorPlansSettings;
  }

  /**
   * Checks again if map is valid. Setter for FloorPlanSettings.
   *
   * @param floorPlansSettings is an array with the floorPlans
   */
  public void setFloorPlanSettings(FloorPlanSetting[] floorPlansSettings) {
    int numberMaps = 0;
    for (FloorPlanSetting item : floorPlansSettings) {
      if (item.getFloorPlansEnum() != FloorPlans.EMPTY) {
        numberMaps++;
      }
    }
    if (numberMaps == 0
        || numberMaps == 3
        || (floorPlansSettings[0].getFloorPlansEnum() != FloorPlans.EMPTY
            && floorPlansSettings[1].getFloorPlansEnum() == FloorPlans.EMPTY
            && floorPlansSettings[2].getFloorPlansEnum() == FloorPlans.EMPTY
            && floorPlansSettings[3].getFloorPlansEnum() != FloorPlans.EMPTY)
        || (floorPlansSettings[0].getFloorPlansEnum() == FloorPlans.EMPTY
            && floorPlansSettings[1].getFloorPlansEnum() != FloorPlans.EMPTY
            && floorPlansSettings[2].getFloorPlansEnum() != FloorPlans.EMPTY
            && floorPlansSettings[3].getFloorPlansEnum() == FloorPlans.EMPTY)) {
      LOG.info("Invalid floorPlans combination. Won't be saved in the LobbyOptions.");
      return;
    }
    this.floorPlansSettings = floorPlansSettings;
  }

  /** Getter for number of set FloorPlans. */
  public int getNumberFloorPlans() {
    int numberPlans = 0;
    for (FloorPlanSetting item : floorPlansSettings) {
      if (item.getFloorPlansEnum() != FloorPlans.EMPTY) {
        numberPlans++;
      }
    }
    return numberPlans;
  }

  /**
   * Get the checkpoint-map with the position on which floor plan.
   *
   * @return checkpoint-map
   */
  public Map<Integer, EnumMap<FloorPlans, Point>> getCheckpointsPosition() {
    return checkpointsPosition;
  }

  /**
   * Set the checkpoint for the game.
   *
   * @param checkpointsPosition checkpoint-map
   */
  public void setCheckpointsPosition(Map<Integer, EnumMap<FloorPlans, Point>> checkpointsPosition) {
    this.checkpointsPosition = checkpointsPosition;
  }

  /** Returns whether spectator mode is active. */
  public boolean isSpectatorModeActive() {
    return this.spectatorModeActive;
  }

  /**
   * Setter for spectatorModeActive.
   *
   * @param spectatorModeActive if spectators are allowed
   */
  public void setSpectatorModeActive(boolean spectatorModeActive) {
    this.spectatorModeActive = spectatorModeActive;
  }

  /** Getter for maxSpectators. */
  public int getMaxSpectators() {
    return this.maxSpectators;
  }

  /** Setter for maxSpectators. */
  public void setMaxSpectators(int maxSpectators) {
    this.maxSpectators = maxSpectators;
  }

  /** Specifies the status of the lobby (ENUM). */
  public enum LobbyStatus implements Serializable {
    WAITING,
    READY,
    STARTING,
    INGAME,
    END
  }
}
