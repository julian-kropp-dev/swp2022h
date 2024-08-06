package de.uol.swp.client.textureatlas;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.LoadException;
import javafx.scene.image.Image;
import javax.imageio.ImageIO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** TextureAtlas for instructions. */
public class TextureAtlasInstruction {
  private static final Logger LOG = LogManager.getLogger(TextureAtlasInstruction.class);
  private static final String PATH_INSTRUCTIONS = "/instructions/instructions.png";
  private static final int RESOLUTION_HEIGHT = 2340;
  private static final int RESOLUTION_WIDTH = 1654;
  private static BufferedImage instructions;
  private static final Map<Integer, Integer> lookUpTabel = new HashMap<>();

  private TextureAtlasInstruction() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * Loads the images for the instructions.
   *
   * @throws LoadException if there is an error loading the textures
   */
  public static void loadImages() throws LoadException {
    LOG.debug("Trying to load instruction");
    long start = System.nanoTime();
    try {
      InputStream in = TextureAtlas.class.getResourceAsStream(PATH_INSTRUCTIONS);
      assert in != null;
      instructions = ImageIO.read(in);
      long stop = System.nanoTime();
      LOG.info("Loaded instructions in {} ms", (stop - start) / 1000000L);
    } catch (IOException | AssertionError e) {
      LOG.fatal("Could not load instruction: {}", e.getMessage());
      throw new LoadException(
          "Es gab ein Fehler beim Laden der Texturen.\nDas Programm wird beendet.");
    }
    // Map page number to id
    lookUpTabel.put(1, 0);
    lookUpTabel.put(10, 1);
    lookUpTabel.put(11, 2);
    lookUpTabel.put(12, 3);
    lookUpTabel.put(6, 4);
    lookUpTabel.put(2, 5);
    lookUpTabel.put(3, 6);
    lookUpTabel.put(4, 7);
    lookUpTabel.put(5, 8);
    lookUpTabel.put(8, 9);
    lookUpTabel.put(9, 10);
    lookUpTabel.put(7, 11);
  }

  /**
   * Getter for the image for one page of the instructions.
   *
   * @param pageNumber The page number
   * @return The page as an image
   */
  public static Image getPage(int pageNumber) {

    int trueId = lookUpTabel.get(pageNumber);
    int x = (trueId % (instructions.getWidth() / RESOLUTION_WIDTH)) * RESOLUTION_WIDTH;
    int y = (trueId / (instructions.getWidth() / RESOLUTION_WIDTH)) * RESOLUTION_HEIGHT;

    BufferedImage image;
    image = instructions.getSubimage(x, y, RESOLUTION_WIDTH, RESOLUTION_HEIGHT);

    return SwingFXUtils.toFXImage(image, null);
  }
}
