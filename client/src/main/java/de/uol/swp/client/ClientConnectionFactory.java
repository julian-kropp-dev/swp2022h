package de.uol.swp.client;

/**
 * Factory for use of injecting the ClientConnection via giuce.
 *
 * @see de.uol.swp.client.di.ClientModule
 */
public interface ClientConnectionFactory {

  /**
   * Creates an instance of the class ClientConnection.
   *
   * @param host Hostname of the server the sever application is running on
   * @param port The port the server application is listening on
   * @return The ClientConnection object the application is using
   * @see de.uol.swp.client.ClientConnection
   */
  ClientConnection create(String host, int port);
}
