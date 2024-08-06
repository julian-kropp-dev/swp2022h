package de.uol.swp.client;

import javafx.stage.Stage;

/**
 * Factory for use of injecting the SceneManager via guice.
 *
 * @see de.uol.swp.client.di.ClientModule
 */
public interface SceneManagerFactory {

  /**
   * Creates an instance of the SceneManager.
   *
   * @param primaryStage The primary stage used by the javafx application
   * @return The SceneManger used inside the client
   * @see de.uol.swp.client.SceneManager
   */
  SceneManager create(Stage primaryStage);
}
