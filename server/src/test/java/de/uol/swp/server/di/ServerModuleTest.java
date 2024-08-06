package de.uol.swp.server.di;

import static org.junit.jupiter.api.Assertions.*;

import com.google.common.eventbus.EventBus;
import com.google.inject.Guice;
import com.google.inject.Injector;
import de.uol.swp.server.game.GameManagement;
import de.uol.swp.server.lobby.LobbyManagement;
import de.uol.swp.server.usermanagement.store.MainMemoryBasedUserStore;
import de.uol.swp.server.usermanagement.store.MySQLUserStore;
import de.uol.swp.server.usermanagement.store.UserStore;
import org.junit.jupiter.api.Test;

@SuppressWarnings("UnstableApiUsage")
class ServerModuleTest {

  @Test
  void testBindings_withDatabase() {
    ServerModule module = new ServerModule();
    module.testMode(true);
    Injector injector = Guice.createInjector(module);

    assertNotNull(injector.getInstance(UserStore.class));
    assertNotNull(injector.getInstance(EventBus.class));
    assertNotNull(injector.getInstance(LobbyManagement.class));
    assertNotNull(injector.getInstance(GameManagement.class));

    assertTrue(injector.getInstance(UserStore.class) instanceof MySQLUserStore);
  }

  @Test
  void testBindings_withoutDatabase() {
    ServerModule module = new ServerModule();
    module.testMode(false);
    Injector injector = Guice.createInjector(module);

    assertNotNull(injector.getInstance(UserStore.class));
    assertNotNull(injector.getInstance(EventBus.class));
    assertNotNull(injector.getInstance(LobbyManagement.class));
    assertNotNull(injector.getInstance(GameManagement.class));

    assertTrue(injector.getInstance(UserStore.class) instanceof MainMemoryBasedUserStore);
  }
}
