package de.uol.swp.common.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.uol.swp.common.user.userdata.UserData;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * Test Class for the UserDTO
 *
 * @author Marco Grawunder
 * @since 2019-09-04
 */
class UserDTOTest {

  private static final User defaultUser =
      new UserDTO(
          UUID.randomUUID().toString(), "marco", "marco", "marco@grawunder.de", new UserData(1));
  private static final User secondsUser =
      new UserDTO(
          UUID.randomUUID().toString(), "marco2", "marco", "marco@grawunder.de", new UserData(1));

  /**
   * This test check whether the username can be null
   *
   * <p>If the constructor does not throw an Exception the test fails
   *
   * @since 2019-09-04
   */
  @Test
  @SuppressWarnings("java:S5778")
  void createUserWithEmptyName() {
    assertThrows(
        IllegalArgumentException.class, () -> new UserDTO("", null, "", "", new UserData(1)));
  }

  /**
   * This test check whether the password can be null
   *
   * <p>If the constructor does not throw an Exception the test fails
   *
   * @since 2019-09-04
   */
  @Test
  @SuppressWarnings("java:S5778")
  void createUserWithEmptyPassword() {
    assertThrows(
        IllegalArgumentException.class, () -> new UserDTO("", "", null, "", new UserData(1)));
  }

  /**
   * This test checks if the copy constructor works correctly
   *
   * <p>This test fails if any of the fields mismatch or the objects are not considered equal
   *
   * @since 2019-09-04
   */
  @Test
  void createWithExistingUser() {

    User newUser = defaultUser;

    // Test with equals method
    assertEquals(defaultUser, newUser);

    // Test every attribute
    assertEquals(defaultUser.getUsername(), newUser.getUsername());
    assertEquals(defaultUser.getPassword(), newUser.getPassword());
    assertEquals(defaultUser.getMail(), newUser.getMail());
  }

  /**
   * This test checks if the createWithoutPassword function generates the Object correctly
   *
   * <p>This test fails if the usernames or emails do not match or the password is not empty.
   *
   * @since 2019-09-04
   */
  @Test
  void createWithExistingUserWithoutPassword() {
    User newUser = UserDTO.createWithoutPassword(defaultUser);

    // Test every attribute
    assertEquals(defaultUser.getUsername(), newUser.getUsername());
    assertEquals("", newUser.getPassword());
    assertEquals(defaultUser.getMail(), newUser.getMail());

    // Test with equals method
    assertEquals(defaultUser, newUser);
  }

  /**
   * This test checks if the getWithoutPassword function generates the Object correctly
   *
   * <p>This test fails if the usernames do not match or the password is not empty.
   *
   * @since 2019-09-04
   */
  @Test
  void getWithoutPassword() {
    User userWithoutPassword = defaultUser.getWithoutPassword();

    assertEquals("", userWithoutPassword.getPassword());
    assertEquals(defaultUser.getUsername(), userWithoutPassword.getUsername());
  }

  /**
   * Test if two different users are equal
   *
   * <p>This test fails if they are considered equal
   *
   * @since 2019-09-04
   */
  @Test
  void usersNotEquals_User() {
    assertNotEquals(defaultUser, secondsUser);
  }

  /**
   * Test of compare function
   *
   * <p>This test compares two different users. It fails if the function returns that both of them
   * are equal.
   *
   * @since 2019-09-04
   */
  @Test
  @SuppressWarnings("java:S3415")
  void userCompare() {
    assertNotEquals(defaultUser.compareTo(secondsUser), 0);
  }

  /**
   * Test if the HashCode of a copied object matches the one of the original
   *
   * <p>This test fails if the codes do not match
   *
   * @since 2019-09-04
   */
  @SuppressWarnings("UnnecessaryLocalVariable")
  @Test
  void testHashCode() {
    User newUser = defaultUser;
    assertEquals(newUser.hashCode(), defaultUser.hashCode());
  }
}
