package de.uol.swp.common.lobby.message;

import de.uol.swp.common.lobby.LobbyOptions;
import de.uol.swp.common.lobby.LobbyOptions.LobbyStatus;
import java.util.Objects;

/** The message is called on an affected response to update the displayed lobby title. */
public class UpdateLobbyListMessage extends AbstractLobbyMessage {

  private static final long serialVersionUID = -4285272999529758233L;

  private final String title;
  private final int loggedInUser;
  private final int slots;
  private final String ownerName;
  private final LobbyOptions.LobbyStatus status;

  private final boolean variantOne;
  private final boolean variantTwo;
  private final boolean variantThree;

  /** Default constructor. */
  @SuppressWarnings("java:S107")
  public UpdateLobbyListMessage(
      String lobbyId,
      String title,
      int loggedInUsers,
      int slots,
      LobbyOptions.LobbyStatus status,
      String ownerName,
      boolean variantOne,
      boolean variantTwo,
      boolean variantThree) {
    this.title = title;
    this.lobbyId = lobbyId;
    this.loggedInUser = loggedInUsers;
    this.slots = slots;
    this.status = status;
    this.ownerName = ownerName;
    this.variantOne = variantOne;
    this.variantTwo = variantTwo;
    this.variantThree = variantThree;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    UpdateLobbyListMessage that = (UpdateLobbyListMessage) o;
    return Objects.equals(lobbyId, that.lobbyId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(lobbyId);
  }

  public int getLoggedInUser() {
    return loggedInUser;
  }

  public int getSlots() {
    return slots;
  }

  public LobbyStatus getStatus() {
    return status;
  }

  public String getOwnerName() {
    return ownerName;
  }

  @Override
  public String getLobbyId() {
    return lobbyId;
  }

  public String getTitle() {
    return title;
  }

  public boolean isVariantOne() {
    return variantOne;
  }

  public boolean isVariantTwo() {
    return variantTwo;
  }

  public boolean isVariantThree() {
    return variantThree;
  }
}
