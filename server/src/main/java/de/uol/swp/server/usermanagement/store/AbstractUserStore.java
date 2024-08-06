package de.uol.swp.server.usermanagement.store;

import de.uol.swp.common.user.Hashing;

/**
 * Base class for all kinds of different UserStores.
 *
 * @see de.uol.swp.server.usermanagement.store.UserStore
 */
public abstract class AbstractUserStore implements UserStore {

  /**
   * Calculates the hash for a given String.
   *
   * @implSpec the hash method used is sha3-512
   * @param toHash the String to calculate the hash for
   * @return String containing the calculated hash
   * @see de.uol.swp.common.user.Hashing
   */
  protected String hash(String toHash) {
    return Hashing.hashing(toHash);
  }
}
