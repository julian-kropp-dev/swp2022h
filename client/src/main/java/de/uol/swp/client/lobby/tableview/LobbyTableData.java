package de.uol.swp.client.lobby.tableview;

/**
 * This class is a helper class for displaying the information in a table.
 *
 * <p>Is contains all information for the tableview.
 */
public class LobbyTableData {
  private final String lobbyId;
  private final String lobbyTitle;
  private final String lobbyOwner;
  private final int lobbyAvailableSlots;
  private final int lobbyMaximalSlots;
  private final String status;
  private final boolean variantOne;
  private final boolean variantTwo;
  private final boolean variantThree;

  /**
   * The default constructor to set the data for the table.
   *
   * @param lobbyId The name of the lobby (id)
   * @param lobbyTitle the title of the lobby
   * @param lobbyOwner The current owner of the lobby
   * @param lobbyAvailableSlots current available slots
   * @param lobbyMaximalSlots maximal slots in the lobby
   * @param status the current status of the lobby
   * @param variantOne the status of the variant one
   * @param variantTwo the status of the variant two
   * @param variantThree the status of the variant three
   */
  @SuppressWarnings("java:S107")
  public LobbyTableData(
      String lobbyId,
      String lobbyTitle,
      String lobbyOwner,
      int lobbyAvailableSlots,
      int lobbyMaximalSlots,
      String status,
      boolean variantOne,
      boolean variantTwo,
      boolean variantThree) {
    this.lobbyId = lobbyId;
    this.lobbyTitle = lobbyTitle;
    this.lobbyOwner = lobbyOwner;
    this.lobbyAvailableSlots = lobbyAvailableSlots;
    this.lobbyMaximalSlots = lobbyMaximalSlots;
    this.status = status;
    this.variantOne = variantOne;
    this.variantTwo = variantTwo;
    this.variantThree = variantThree;
  }

  /**
   * Getter for the lobbyId. Netted for displayed in the LobbyList
   *
   * @return the 4 String-length of the lobbyId
   */
  public String getLobbyId() {
    return lobbyId;
  }

  /**
   * Getter for the LobbyTitle. Netted for displayed in the LobbyList
   *
   * @return the lobby title
   */
  @SuppressWarnings("unused")
  public String getLobbyTitle() {
    return lobbyTitle;
  }

  /**
   * Getter for the LobbyOwner. Needed for display in the table in LobbyList
   *
   * @return the LobbyOwner
   */
  @SuppressWarnings("unused")
  public String getLobbyOwner() {
    return lobbyOwner;
  }

  /**
   * Getter for the available slots in the Lobby. Netted for displayed in the LobbyList
   *
   * @return the available slots
   */
  @SuppressWarnings("unused")
  public int getLobbyAvailableSlots() {
    return lobbyAvailableSlots;
  }

  /**
   * Getter for the maximum slots in the Lobby. Netted for displayed in the LobbyList
   *
   * @return the maximum slots
   */
  @SuppressWarnings("unused")
  public int getLobbyMaximalSlots() {
    return lobbyMaximalSlots;
  }

  /**
   * Getter for the status of a Lobby. Netted for displayed in the LobbyList
   *
   * @return the status of a Lobby
   */
  @SuppressWarnings("unused")
  public String getStatus() {
    return status;
  }

  /**
   * Getter for the variant one Netted for displayed in the LobbyList.
   *
   * @return is the variant one is active
   */
  @SuppressWarnings("unused")
  public String getVariantOne() {
    return variantOne ? "Ja" : "Nein";
  }

  /**
   * Getter for the variant two Netted for displayed in the LobbyList.
   *
   * @return is the variant two is active
   */
  @SuppressWarnings("unused")
  public String getVariantTwo() {
    return variantTwo ? "Ja" : "Nein";
  }

  /**
   * Getter for the variant three Netted for displayed in the LobbyList.
   *
   * @return is the variant three is active
   */
  @SuppressWarnings("unused")
  public String getVariantThree() {
    return variantThree ? "Ja" : "Nein";
  }
}
