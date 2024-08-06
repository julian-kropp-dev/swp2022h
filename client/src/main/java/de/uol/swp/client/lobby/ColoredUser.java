package de.uol.swp.client.lobby;

import javafx.scene.paint.Color;

/** Represents a user with a specific color in a lobby. */
public class ColoredUser {
  private final String username;
  private final Color color;

  /**
   * Constructs a new ColoredUser object with the given username and color.
   *
   * @param username the username of the user
   * @param color the color associated with the user
   */
  public ColoredUser(String username, Color color) {
    this.username = username;
    this.color = color;
  }

  /**
   * Gets the username of the user.
   *
   * @return the username
   */
  public String getUsername() {
    return username;
  }

  /**
   * Gets the color associated with the user.
   *
   * @return the color
   */
  public Color getColor() {
    return color;
  }

  @Override
  public String toString() {
    return username;
  }
}
