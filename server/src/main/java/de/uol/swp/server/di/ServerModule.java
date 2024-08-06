package de.uol.swp.server.di;

import com.google.common.eventbus.EventBus;
import com.google.inject.AbstractModule;
import de.uol.swp.server.ServerConfig;
import de.uol.swp.server.game.GameManagement;
import de.uol.swp.server.lobby.LobbyManagement;
import de.uol.swp.server.usermanagement.store.MainMemoryBasedUserStore;
import de.uol.swp.server.usermanagement.store.MySQLUserStore;
import de.uol.swp.server.usermanagement.store.UserStore;

/** Module that provides classes needed by the Server. */
@SuppressWarnings("UnstableApiUsage")
public class ServerModule extends AbstractModule {

  private final EventBus bus = new EventBus();
  private final LobbyManagement lobbyManagement = new LobbyManagement();
  private final GameManagement gameManagement = new GameManagement();

  @SuppressWarnings({"java:S1450", "FieldCanBeLocal"})
  private UserStore store;

  private boolean isDatabaseActive;
  private boolean isTestMode;

  @Override
  protected void configure() {
    if (ServerConfig.getInstance().getDatabaseOn() && !isTestMode || isDatabaseActive) {
      store = new MySQLUserStore();
    } else {
      store = new MainMemoryBasedUserStore();
    }
    bind(UserStore.class).toInstance(store);
    bind(EventBus.class).toInstance(bus);
    bind(LobbyManagement.class).toInstance(lobbyManagement);
    bind(GameManagement.class).toInstance(gameManagement);
  }

  protected void testMode(boolean isDatabaseActive) {
    this.isDatabaseActive = isDatabaseActive;
    isTestMode = true;
  }
}
