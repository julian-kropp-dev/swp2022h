package de.uol.swp.client.textureatlas;

import de.uol.swp.client.Configuration;
import de.uol.swp.common.game.floor.FloorPlanSetting.FloorPlans;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.LoadException;
import javafx.scene.image.Image;
import javax.imageio.ImageIO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The TextureAtlasFloorPlans class represents a texture atlas for floor plans in a game. It
 * provides methods to load and retrieve floor plan images from the atlas.
 */
public class TextureAtlasFloorPlans {
  private static final Logger LOG = LogManager.getLogger(TextureAtlasFloorPlans.class);
  private static final String PATH_ASSETS_ORIGINAL = Configuration.getFloorplanImageOriginalPath();
  private static final String PATH_ASSETS_EMPTY = Configuration.getFloorPlanEmptyPath();
  private static final int RESOLUTION_ASSETS = 900;
  private static BufferedImage floorPlanImage;
  private static BufferedImage emptyImage;

  private TextureAtlasFloorPlans() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * Loads the images for the floor plan previews.
   *
   * @throws LoadException if there is an error loading the textures
   */
  public static void loadImages() throws LoadException {
    LOG.debug("Trying to load floorPlan previews");
    long start = System.nanoTime();
    try {
      InputStream in = TextureAtlas.class.getResourceAsStream(PATH_ASSETS_EMPTY);
      emptyImage = ImageIO.read(in);
    } catch (IOException e) {
      LOG.fatal("Could not load floorplan sprites: {}", e.getMessage());
      throw new LoadException(
          "Es gab ein Fehler beim Laden der Texturen.\nDas Programm wird beendet.");
    }
    try {
      InputStream in = TextureAtlas.class.getResourceAsStream(PATH_ASSETS_ORIGINAL);
      floorPlanImage = ImageIO.read(in);
      long stop = System.nanoTime();
      LOG.info("Loaded floorPlan previews in {} ms", (stop - start) / 1000000L);
    } catch (IOException e) {
      LOG.fatal("Could not load floorplan sprites: {}", e.getMessage());
      throw new LoadException(
          "Es gab ein Fehler beim Laden der Texturen.\nDas Programm wird beendet.");
    }
  }

  public static Image getFloorPlanImage(FloorPlans floorPlan) {
    return SwingFXUtils.toFXImage(
        Objects.requireNonNull(getFloorPlanBufferedImage(floorPlan)), null);
  }

  /**
   * Retrieves the buffered image for the specified floor plan.
   *
   * @param floorPlan the floor plan for which to retrieve the buffered image
   * @return the buffered image for the floor plan
   */
  @SuppressWarnings("java:S112")
  public static BufferedImage getFloorPlanBufferedImage(FloorPlans floorPlan) {
    if (floorPlanImage == null) {
      LOG.fatal("Texture-atlas was not ready");
      throw new RuntimeException();
    }
    int tileId = 10;
    switch (floorPlan) {
      case CANNERY_ROW:
        tileId = 0;
        break;
      case CROSS:
        tileId = 1;
        break;
      case MAELSTROM:
        tileId = 2;
        break;
      case EXCHANGE:
        tileId = 3;
        break;
      case ISLAND:
        tileId = 4;
        break;
      case PIT_MAZE:
        tileId = 5;
        break;
      case EMPTY:
        tileId = 6;
        break;
      default:
        LOG.debug("Can not find FloorPlan");
        break;
    }
    if (tileId == 6) {
      return emptyImage;
    }
    if (tileId
        > ((floorPlanImage.getHeight() / RESOLUTION_ASSETS)
            * (floorPlanImage.getWidth() / RESOLUTION_ASSETS))) {
      LOG.error("Could not find image for floorplan {}", floorPlan);
      return null;
    }

    int x = (tileId % (floorPlanImage.getWidth() / RESOLUTION_ASSETS)) * RESOLUTION_ASSETS;
    int y = (tileId / (floorPlanImage.getWidth() / RESOLUTION_ASSETS)) * RESOLUTION_ASSETS;

    BufferedImage image;
    image = floorPlanImage.getSubimage(x, y, RESOLUTION_ASSETS, RESOLUTION_ASSETS);

    return image;
  }
}
