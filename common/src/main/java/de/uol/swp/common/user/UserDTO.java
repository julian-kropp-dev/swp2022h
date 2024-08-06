package de.uol.swp.common.user;

import de.uol.swp.common.user.userdata.UserData;
import java.util.Objects;

/**
 * Objects of this class are used to transfer user data between the server and the clients.
 *
 * @see de.uol.swp.common.user.User
 * @see de.uol.swp.common.user.request.RegisterUserRequest
 * @see de.uol.swp.common.user.response.AllOnlineUsersResponse
 */
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public class UserDTO implements User {

  private static final long serialVersionUID = -3791025835001307406L;
  private final String uuid;
  private final String username;
  private final String password;
  private final String mail;
  private final UserData userData;

  /**
   * Constructor.
   *
   * @param uuid uuid of the user
   * @param username username of the user
   * @param password password the user uses
   * @param mail email address the user is registered to
   * @param userData data properties of the user
   */
  public UserDTO(String uuid, String username, String password, String mail, UserData userData) {
    if (Objects.nonNull(username) && Objects.nonNull(password) && Objects.nonNull(userData)) {
      this.uuid = uuid;
      this.username = username;
      this.password = password;
      this.mail = mail;
      this.userData = userData;
    } else {
      throw new IllegalArgumentException("Username, password or userData cannot be null");
    }
  }

  /**
   * Copy constructor leaving password variable empty.
   *
   * <p>This constructor is used for the user list, because it would be a major security flaw to
   * send all user data including passwords to everyone connected.
   *
   * @param user User object to copy the values of
   * @return UserDTO copy of User object having the password variable left empty
   */
  public static UserDTO createWithoutPassword(User user) {
    return new UserDTO(user.getUUID(), user.getUsername(), "", user.getMail(), user.getUserData());
  }

  @Override
  public String getUUID() {
    return uuid;
  }

  @Override
  public String getUsername() {
    return username;
  }

  @Override
  public String getPassword() {
    return password;
  }

  @Override
  public String getMail() {
    return mail;
  }

  @Override
  public User getWithoutPassword() {
    return new UserDTO(uuid, username, "", mail, userData);
  }

  @Override
  public int compareTo(User o) {
    return username.compareTo(o.getUsername());
  }

  @Override
  public UserData getUserData() {
    return userData;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    UserDTO userDTO = (UserDTO) o;
    return Objects.equals(uuid, userDTO.uuid);
  }

  @Override
  public int hashCode() {
    return Objects.hash(uuid);
  }
}
