package de.uol.swp.client.textureatlas;

import de.uol.swp.client.Configuration;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.LoadException;
import javafx.scene.image.Image;
import javax.imageio.ImageIO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * TextureAtlasRobots is a utility class that provides access to robot images from a texture atlas.
 * It allows loading and retrieving robot images based on the robot's name.
 */
public class TextureAtlasRobots {
  private static final Logger LOG = LogManager.getLogger(TextureAtlasRobots.class);
  private static final String PATH_ASSETS = Configuration.getRobotsImagePath();
  private static final int RESOLUTION_ASSETS = 500;
  private static BufferedImage robotImages;

  private TextureAtlasRobots() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * Loads the robot images from the robot texture atlas.
   *
   * @throws LoadException if there was an error loading the robot sprites.
   */
  public static void loadImages() throws LoadException {
    long start = System.nanoTime();
    try {
      InputStream in = TextureAtlas.class.getResourceAsStream(PATH_ASSETS);
      robotImages = ImageIO.read(in);
      long stop = System.nanoTime();
      LOG.info("Loaded robot images in {} ms", (stop - start) / 1000000L);
    } catch (IOException e) {
      LOG.fatal("Could not load robot sprites: {}", e.getMessage());
      throw new LoadException(
          "Es gab ein Fehler beim Laden der Texturen.\nDas Programm wird beendet.");
    }
  }

  /**
   * Retrieves the image for a specific robot from the robot texture atlas.
   *
   * @param robot The name of the robot.
   * @return The image of the robot.
   * @throws RuntimeException if the robot texture atlas is not ready.
   */
  @SuppressWarnings("java:S112")
  public static Image getRobotImage(String robot) {
    if (robotImages == null) {
      LOG.fatal("Texture-atlas was not ready");
      throw new RuntimeException();
    }
    int tileId = 10;
    switch (robot.toUpperCase()) {
      case "DUSTY":
        tileId = 0;
        break;
      case "GANDALF":
        tileId = 1;
        break;
      case "GREGOR":
        tileId = 2;
        break;
      case "OCEAN":
        tileId = 3;
        break;
      case "BOB":
        tileId = 4;
        break;
      case "ROCKY":
        tileId = 5;
        break;
      case "OLIVER":
        tileId = 6;
        break;
      case "ROSE":
        tileId = 7;
        break;
      default:
        break;
    }

    if (tileId
        > ((robotImages.getHeight() / RESOLUTION_ASSETS)
            * (robotImages.getWidth() / RESOLUTION_ASSETS))) {
      LOG.debug("Could not load tile id {} from atlas.", tileId);
      return null;
    }

    int x = (tileId % (robotImages.getWidth() / RESOLUTION_ASSETS)) * RESOLUTION_ASSETS;
    int y = (tileId / (robotImages.getWidth() / RESOLUTION_ASSETS)) * RESOLUTION_ASSETS;

    BufferedImage image =
        new BufferedImage(RESOLUTION_ASSETS, RESOLUTION_ASSETS, BufferedImage.TYPE_INT_ARGB);

    image
        .getGraphics()
        .drawImage(
            robotImages,
            0,
            0,
            RESOLUTION_ASSETS,
            RESOLUTION_ASSETS,
            x,
            y,
            x + RESOLUTION_ASSETS,
            y + RESOLUTION_ASSETS,
            null);

    return SwingFXUtils.toFXImage(image, null);
  }
}
