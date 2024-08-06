package de.uol.swp.common.user;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** The Hashing class provides a method for generating a SHA3-512 hash of a given input string. */
public class Hashing {
  private static final String SALT = "6538sg968gMaikWarHiershää#+ä#+6ä7Moin#zwäb#sz";
  private static final Logger LOG = LogManager.getLogger(Hashing.class);

  private Hashing() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * Hashes the input string using the SHA3-512 algorithm.
   *
   * @param toHash The input string to be hashed
   * @return A string representation of the hashed input
   * @throws RuntimeException if the SHA3-512 algorithm is not supported
   */
  public static String hashing(String toHash) {
    String out = null;
    String buffer = toHash;
    toHash = toHash + SALT;
    byte[] toHashAsByteArray = toHash.getBytes();
    byte[] hash;
    try {
      MessageDigest md = MessageDigest.getInstance("SHA3-512");
      hash = md.digest(toHashAsByteArray);
      out = new String(hash);
    } catch (NoSuchAlgorithmException e) {
      LOG.error("Error hashing string {}", buffer);
    }
    return out;
  }
}
