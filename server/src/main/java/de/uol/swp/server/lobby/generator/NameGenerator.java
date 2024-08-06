package de.uol.swp.server.lobby.generator;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashSet;

/** Creates Random ID for Lobby Name. */
public class NameGenerator {
  protected static final HashSet<String> generatedIDs = new HashSet<>();
  private static final SecureRandom random = new SecureRandom();
  private static final Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();

  private NameGenerator() {
    throw new IllegalStateException();
  }

  /**
   * Generates a unique name for a lobby.
   *
   * @return generated name with 4 upper case characters
   */
  public static String generate() {
    boolean generated = false;
    String id = null;
    while (!generated) {
      byte[] buffer = new byte[3];
      random.nextBytes(buffer);
      id = encoder.encodeToString(buffer);
      if (id.matches("^[A-Z]*$")) {
        generated = generatedIDs.add(id);
      }
    }
    return id;
  }

  /**
   * Mark an ID (Lobby Name) as unused, so that it can be used for a new lobby.
   *
   * @param id ID that is not in use anymore
   */
  public static void markAsUnused(String id) {
    generatedIDs.remove(id);
  }
}
