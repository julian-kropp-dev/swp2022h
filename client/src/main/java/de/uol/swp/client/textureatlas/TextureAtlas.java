package de.uol.swp.client.textureatlas;

import de.uol.swp.client.Configuration;
import de.uol.swp.common.game.card.CardType;
import de.uol.swp.common.game.robot.Robots;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicBoolean;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.LoadException;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javax.imageio.ImageIO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Manages loading of textures. */
@SuppressWarnings("java:S1192")
public class TextureAtlas {

  private static final Logger LOG = LogManager.getLogger(TextureAtlas.class);

  private static final int RESOLUTION = 150;
  private static final int RESOLUTION_ASSETS = 150;
  private static final int RESOLUTION_AVATARS_HEIGHT = 234;
  private static final int RESOLUTION_AVATARS_WIDTH = 346;
  private static final int RESOLUTION_CARDS_HEIGHT = 1027;
  private static final int RESOLUTION_CARDS_WIDTH = 671;
  private static final String PATH = Configuration.getTextureAtlasPath();
  private static final String PATH_CARDS = Configuration.getTextureAtlasCardsPath();
  private static final String PATH_ASSETS = Configuration.getTextureAtlasAssetsPath();
  private static final String FLOOR_FIELD_SPRITES_BASE_PATH =
      Configuration.getFloorFieldSpritesBasePath();
  private static final String PATH_AVATARS = "/textures/avatarAtlas.png";
  private static BufferedImage atlas;
  private static BufferedImage atlasCards;
  private static BufferedImage atlasAssets;
  private static BufferedImage avatars;
  private static final Map<Integer, BufferedImage> floorFieldSpritesMap = new HashMap<>();

  private TextureAtlas() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * Loads the images for the game.
   *
   * @throws LoadException if there is an error loading the textures
   */
  public static void loadImages() throws LoadException {
    String loadExceptionMessage =
        "Es gab ein Fehler beim Laden der Texturen.\\nDas Programm wird beendet.";
    LOG.debug("Trying to load assets for game");
    long start = System.nanoTime();
    try {
      InputStream in = TextureAtlas.class.getResourceAsStream(PATH);
      assert in != null;
      atlas = ImageIO.read(in);
    } catch (IOException | AssertionError e) {
      LOG.fatal("Could not load Texture Atlas while doing this: {}", e.getMessage());
      throw new LoadException(loadExceptionMessage);
    }
    try {
      InputStream in = TextureAtlas.class.getResourceAsStream(PATH_CARDS);
      assert in != null;
      atlasCards = ImageIO.read(in);
    } catch (IOException | AssertionError e) {
      LOG.fatal("Could not load Cards Atlas while doing this: {}", e.getMessage());
      throw new LoadException(loadExceptionMessage);
    }
    try {
      InputStream in = TextureAtlas.class.getResourceAsStream(PATH_ASSETS);
      assert in != null;
      atlasAssets = ImageIO.read(in);
      long stop = System.nanoTime();
      LOG.info("Loaded floorPlan previews in {} ms", (stop - start) / 1000000L);
    } catch (IOException | AssertionError e) {
      LOG.fatal("Could not load Assets Atlas while doing this: {}", e.getMessage());
      throw new LoadException(loadExceptionMessage);
    }
    try {
      InputStream in = TextureAtlas.class.getResourceAsStream(PATH_AVATARS);
      assert in != null;
      avatars = ImageIO.read(in);
      long stop = System.nanoTime();
      LOG.info("Loaded avatar images in {} ms", (stop - start) / 1000000L);
    } catch (IOException | AssertionError e) {
      LOG.fatal("Could not load Avatars Atlas while doing this: {}", e.getMessage());
      throw new LoadException(loadExceptionMessage);
    }
    try {
      loadFloorFieldSprites();
    } catch (IOException e) {
      LOG.fatal("Could not load Assets Atlas while doing this: {}", e.getMessage());
    }
  }

  /**
   * Loads a tile from Atlas.
   *
   * <p>With the given ID the Method crops a tile out of the atlas. All images in the atlas are
   * chronologically numbered, starting in the upper left corner with 0.
   *
   * @param tileId The ID of the tile
   */
  public static Image getTileJavaFx(int tileId, int rotation) {

    if (tileId > ((atlas.getHeight() / RESOLUTION) * (atlas.getWidth() / RESOLUTION))) {
      LOG.debug("Could not load tile id {} from atlas.", tileId);
      return null;
    }

    int x = (tileId % (atlas.getWidth() / RESOLUTION)) * RESOLUTION; // 16
    int y = (tileId / (atlas.getWidth() / RESOLUTION)) * RESOLUTION; // 7

    BufferedImage image = new BufferedImage(RESOLUTION, RESOLUTION, BufferedImage.TYPE_INT_ARGB);
    image
        .getGraphics()
        .drawImage(atlas, 0, 0, RESOLUTION, RESOLUTION, x, y, x + RESOLUTION, y + RESOLUTION, null);

    return rotateTile(image, rotation);
  }

  /**
   * Retrieves the image for the specified tile ID with the given rotation.
   *
   * @param tileId the ID of the tile
   * @param rotation the rotation of the tile (in degrees)
   * @return the image for the tile with the specified ID and rotation
   */
  public static Image getAssets(int tileId, int rotation) {
    if (tileId
        > ((atlasAssets.getHeight() / RESOLUTION_ASSETS)
            * (atlasAssets.getWidth() / RESOLUTION_ASSETS))) {
      LOG.debug("Could not load tile id {} from atlas.", tileId);
      return null;
    }

    int x = (tileId % (atlasAssets.getWidth() / RESOLUTION_ASSETS)) * RESOLUTION_ASSETS;
    int y = (tileId / (atlasAssets.getWidth() / RESOLUTION_ASSETS)) * RESOLUTION_ASSETS;

    BufferedImage image =
        new BufferedImage(RESOLUTION_ASSETS, RESOLUTION_ASSETS, BufferedImage.TYPE_INT_ARGB);
    image
        .getGraphics()
        .drawImage(
            atlasAssets,
            0,
            0,
            RESOLUTION_ASSETS,
            RESOLUTION_ASSETS,
            x,
            y,
            x + RESOLUTION_ASSETS,
            y + RESOLUTION_ASSETS,
            null);

    return rotateTile(image, rotation);
  }

  /**
   * Returns a WritableImage object representing the specified card from the card atlas image, with
   * an optional card number displayed on the card.
   *
   * @param cardId The ID of the card to retrieve from the atlas
   * @param cardNumber The number to display on the card, or 0 if no number should be displayed
   * @return A WritableImage object representing the specified card with the optional card number
   */
  public static WritableImage getCard(int cardId, int cardNumber) {
    if (cardId > ((atlasCards.getHeight() / RESOLUTION) * (atlasCards.getWidth() / RESOLUTION))) {
      LOG.debug("Could not load tile id {} from atlas.", cardId);
      return null;
    }

    int x = (cardId % (atlasCards.getWidth() / RESOLUTION_CARDS_WIDTH)) * RESOLUTION_CARDS_WIDTH;
    int y = (cardId / (atlasCards.getWidth() / RESOLUTION_CARDS_WIDTH)) * RESOLUTION_CARDS_HEIGHT;
    BufferedImage image =
        new BufferedImage(
            RESOLUTION_CARDS_WIDTH, RESOLUTION_CARDS_HEIGHT, BufferedImage.TYPE_INT_ARGB);
    image
        .getGraphics()
        .drawImage(
            atlasCards,
            0,
            0,
            RESOLUTION_CARDS_WIDTH,
            RESOLUTION_CARDS_HEIGHT,
            x,
            y,
            x + RESOLUTION_CARDS_WIDTH,
            y + RESOLUTION_CARDS_HEIGHT,
            null);

    if (cardNumber != 0) {
      Graphics graphics = image.createGraphics();
      Font font = new Font("VoiceActivatedBB", Font.BOLD, 150);
      graphics.setFont(font);
      graphics.setColor(new Color(0, 0, 0));
      graphics.drawString(String.valueOf(cardNumber), 140, 180);
    }

    return SwingFXUtils.toFXImage(image, null);
  }

  /**
   * Converts the specified writable image to grayscale.
   *
   * @param image the writable image to convert
   * @return the converted grayscale image
   */
  public static WritableImage toGrayscale(WritableImage image) {

    PixelReader pixelReader = image.getPixelReader();
    PixelWriter pixelWriter = image.getPixelWriter();

    for (int y = 0; y < image.getHeight(); y++) {
      for (int x = 0; x < image.getWidth(); x++) {
        javafx.scene.paint.Color color = pixelReader.getColor(x, y);
        double gray = 0.3 * color.getRed() + 0.59 * color.getGreen() + 0.11 * color.getBlue();

        // convert to grayscale
        pixelWriter.setColor(
            x, y, new javafx.scene.paint.Color(gray, gray, gray, color.getOpacity()));
      }
    }

    return image;
  }

  /**
   * Retrieves the simple card image for the specified card type.
   *
   * @param cardType the card type for which to retrieve the simple card image
   * @return the simple card image for the card type
   */
  public static Image getSimpleCardImage(CardType cardType) {
    int tileId;
    int rotation = 0;
    switch (cardType) {
      case MOVE1:
      case MOVE2:
      case MOVE3:
        tileId = 22;
        break;
      case BACKUP:
        tileId = 22;
        rotation = 180;
        break;
      case TURN_LEFT:
        tileId = 24;
        break;
      case TURN_RIGHT:
        tileId = 23;
        break;
      case U_TURN:
        tileId = 25;
        break;
      default:
        tileId = -1;
    }
    if (tileId < 0) {
      return null;
    }
    return getAssets(tileId, rotation);
  }

  /**
   * Retrieves the number image for the specified card type.
   *
   * @param cardType the card type for which to retrieve the number image
   * @return the number image for the card type
   */
  public static Image getNumberForCard(CardType cardType) {
    int tileId;
    int rotation = 0;
    switch (cardType) {
      case MOVE1:
        tileId = 29;
        break;
      case MOVE2:
        tileId = 30;
        break;
      case MOVE3:
        tileId = 31;
        break;
      default:
        tileId = -1;
    }
    if (tileId < 0) {
      return null;
    }
    return getAssets(tileId, rotation);
  }

  /**
   * Retrieves a marker image for the specified robot.
   *
   * @param robot the robot for which to retrieve the marker image
   * @return the marker image for the robot
   */
  public static Image getMarker(Robots robot) {
    int tileId = robot.ordinal() + 14;
    int rotation = 0;
    return getAssets(tileId, rotation);
  }

  /**
   * Retrieves a floor sprite image based on the specified ID and rotation.
   *
   * @param id the ID of the floor sprite
   * @param rotation the rotation angle for the sprite
   * @return the floor sprite image
   */
  public static Image getFloorSprite(int id, int rotation) {
    if (floorFieldSpritesMap.containsKey(id)) {
      if (id == 0 || id == 1 || id == 2 || id == 64 || id == 65 || id == 70 || id == 71 || id == 73
          || id == 96) {
        return rotateTile(floorFieldSpritesMap.get(id), rotation);
      }
      BufferedImage[] images = getFloorSpriteTile(floorFieldSpritesMap.get(id), rotation);
      BufferedImage rotatedSprite = concatenateImagesVertically(images);
      return SwingFXUtils.toFXImage(rotatedSprite, null);
    }
    return null;
  }

  public static Image getAvatar(int id) {
    int trueId = id - 1;
    int x = (trueId % (avatars.getWidth() / RESOLUTION_AVATARS_WIDTH)) * RESOLUTION_AVATARS_WIDTH;
    int y = (trueId / (avatars.getWidth() / RESOLUTION_AVATARS_WIDTH)) * RESOLUTION_AVATARS_HEIGHT;

    BufferedImage image;
    image = avatars.getSubimage(x, y, RESOLUTION_AVATARS_WIDTH, RESOLUTION_AVATARS_HEIGHT);

    return SwingFXUtils.toFXImage(image, null);
  }

  /**
   * Retrieves an array of floor sprite tiles from a sprite image with the specified rotation.
   *
   * @param imageSprite the sprite image containing the floor tiles
   * @param rotation the rotation angle for the tiles
   * @return an array of rotated floor sprite tiles
   */
  private static BufferedImage[] getFloorSpriteTile(BufferedImage imageSprite, int rotation) {
    int imageCount = imageSprite.getHeight() / RESOLUTION;
    BufferedImage[] output = new BufferedImage[imageCount];
    for (int subId = 0; subId < imageCount; subId++) {
      int y = subId * RESOLUTION;
      int x = 0;
      BufferedImage image = imageSprite.getSubimage(x, y, RESOLUTION, RESOLUTION);
      output[subId] = rotateImage(image, rotation);
    }
    return output;
  }

  /**
   * Rotates a BufferedImage by the specified angle.
   *
   * @param buffImage the BufferedImage to rotate
   * @param angle the angle of rotation in degrees
   * @return the rotated BufferedImage
   */
  @SuppressWarnings("checkstyle:LocalVariableName")
  private static BufferedImage rotateImage(BufferedImage buffImage, double angle) {
    double radian = Math.toRadians(angle);
    double sin = Math.abs(Math.sin(radian));
    double cos = Math.abs(Math.cos(radian));

    int width = buffImage.getWidth();
    int height = buffImage.getHeight();

    int nWidth = (int) Math.floor(width * cos + height * sin);
    int nHeight = (int) Math.floor(height * cos + width * sin);

    BufferedImage rotatedImage = new BufferedImage(nWidth, nHeight, BufferedImage.TYPE_INT_ARGB);

    Graphics2D graphics = rotatedImage.createGraphics();

    graphics.setRenderingHint(
        RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

    graphics.translate((nWidth - width) / 2, (nHeight - height) / 2);
    graphics.rotate(radian, (width / 2f), (height / 2f));
    graphics.drawImage(buffImage, 0, 0, null);
    graphics.dispose();

    return rotatedImage;
  }

  /**
   * Concatenates an array of BufferedImage vertically to create a single image.
   *
   * @param images the array of BufferedImage to concatenate
   * @return the concatenated BufferedImage
   */
  private static BufferedImage concatenateImagesVertically(BufferedImage[] images) {
    int maxWidth = 0;
    int totalHeight = 0;

    for (BufferedImage image : images) {
      maxWidth = Math.max(maxWidth, image.getWidth());
      totalHeight += image.getHeight();
    }

    BufferedImage resultImage =
        new BufferedImage(maxWidth, totalHeight, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2d = resultImage.createGraphics();

    int y = 0;
    for (BufferedImage image : images) {
      g2d.drawImage(image, 0, y, null);
      y += image.getHeight();
    }

    g2d.dispose();

    return resultImage;
  }

  /**
   * Rotates a Tile
   *
   * <p>With a given image and a rotation, this method gives back a rotated image.
   *
   * @param tile tile to be rotated
   * @param rotation Rotation in degree (Clockwise)
   */
  private static WritableImage rotateTile(BufferedImage tile, int rotation) {
    AffineTransform transform = new AffineTransform();
    transform.rotate(Math.toRadians(rotation), tile.getWidth() / 2D, tile.getHeight() / 2D);

    AffineTransformOp op = new AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR);
    BufferedImage rotatedBufferedImage = op.filter(tile, null);

    return SwingFXUtils.toFXImage(rotatedBufferedImage, null);
  }

  /**
   * This method loads all floor filed sprites from the floor filed sprites directory.
   *
   * @throws IOException if an image could not be loaded
   */
  private static void loadFloorFieldSprites() throws IOException {
    AtomicBoolean failed = new AtomicBoolean(false);
    List<String> imagePaths = new ArrayList<>(generateIdList());
    imagePaths.forEach(
        imagePath -> {
          try {
            InputStream in = TextureAtlas.class.getResourceAsStream(imagePath);
            assert in != null;
            BufferedImage bufferedImage = ImageIO.read(in);
            Integer id = extractIdFromPath(imagePath);
            floorFieldSpritesMap.put(id, bufferedImage);
          } catch (IOException | AssertionError | NoSuchElementException e) {
            failed.set(true);
          }
        });

    if (failed.get()) {
      throw new IOException();
    }
  }

  private static Integer extractIdFromPath(String path) {
    int startIndex = path.lastIndexOf("id");
    int endIndex = path.lastIndexOf(".png");
    if (startIndex == -1 || endIndex == -1 || startIndex + 2 >= endIndex) {
      throw new NoSuchElementException();
    }
    return Integer.valueOf(path.substring(startIndex + 2, endIndex));
  }

  private static List<String> generateIdList() {
    List<String> idList = new ArrayList<>();

    String basePath = FLOOR_FIELD_SPRITES_BASE_PATH;
    String extension = ".png";

    for (int i = 0; i <= 26; i++) {
      String id = String.format("%02d", i);
      String path = basePath + id + extension;
      idList.add(path);
    }
    for (int i = 32; i <= 42; i++) {
      String id = String.format("%02d", i);
      String path = basePath + id + extension;
      idList.add(path);
    }
    for (int i = 48; i <= 56; i++) {
      String id = String.format("%02d", i);
      String path = basePath + id + extension;
      idList.add(path);
    }
    for (int i = 64; i <= 73; i++) {
      String id = String.format("%02d", i);
      String path = basePath + id + extension;
      idList.add(path);
    }
    for (int i = 80; i <= 96; i++) {
      String id = String.format("%02d", i);
      String path = basePath + id + extension;
      idList.add(path);
    }
    for (int i = 98; i <= 99; i++) {
      String id = String.format("%02d", i);
      String path = basePath + id + extension;
      idList.add(path);
    }
    return idList;
  }

  public static int getResolution() {
    return RESOLUTION;
  }
}
