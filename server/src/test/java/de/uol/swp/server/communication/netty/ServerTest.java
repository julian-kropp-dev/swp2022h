package de.uol.swp.server.communication.netty;

import static org.junit.jupiter.api.Assertions.*;

import io.netty.channel.ChannelHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ServerTest {

  private static final int TEST_PORT = 8080;
  private Server server;
  private Thread serverThread;

  @BeforeEach
  public void setup() {
    ChannelHandler mockedHandler = Mockito.mock(ChannelHandler.class);
    server = new Server(mockedHandler);
    serverThread =
        new Thread(
            () -> {
              try {
                server.start(TEST_PORT);
              } catch (InterruptedException e) {
                // ignore
              } catch (Exception e) {
                Assertions.fail("Server could not start", e);
              }
            });
    serverThread.start();
  }

  @AfterEach
  void tearDown() throws InterruptedException {
    serverThread.interrupt();
    serverThread.join();
  }

  @Test
  void testServerStart() {
    Assertions.assertTrue(serverThread.isAlive());
  }
}
