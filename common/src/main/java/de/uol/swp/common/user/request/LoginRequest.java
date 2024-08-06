package de.uol.swp.common.user.request;

import de.uol.swp.common.message.AbstractRequestMessage;
import java.util.Objects;

/** A request send from client to server, trying to log in with username and password. */
public class LoginRequest extends AbstractRequestMessage {

  private static final long serialVersionUID = 7793454958390539421L;
  private String username;
  private String password;

  /**
   * Constructor.
   *
   * @param username username the user tries to log in with
   * @param password password the user tries to log in with
   */
  public LoginRequest(String username, String password) {
    this.username = username;
    this.password = password;
  }

  @Override
  public boolean authorizationNeeded() {
    return false;
  }

  /**
   * Getter for the username variable.
   *
   * @return String containing the username the user tries to log in with
   * @since 2017-03-17
   */
  public String getUsername() {
    return username;
  }

  /**
   * Setter for the username variable.
   *
   * @param username String containing the new username
   */
  public void setUsername(String username) {
    this.username = username;
  }

  /**
   * Getter for the password variable.
   *
   * @return String containing the password the user tries to log in with
   */
  public String getPassword() {
    return password;
  }

  /**
   * Setter for the password variable.
   *
   * @param password String containing the new password
   */
  public void setPassword(String password) {
    this.password = password;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LoginRequest that = (LoginRequest) o;
    return Objects.equals(username, that.username) && Objects.equals(password, that.password);
  }

  @Override
  public int hashCode() {
    return Objects.hash(username, password);
  }
}
