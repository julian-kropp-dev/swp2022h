package de.uol.swp.client.lobby;

import de.uol.swp.common.game.floor.Direction;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * ImageMerger Class which has two methods to merge images and other important methods for image
 * processing.
 */
public class ImageMerger {

  private ImageMerger() {}

  /**
   * <a
   * href="https://stackoverflow.com/questions/16092573/i-want-to-merge-4-pictures-together-in-java">...</a>
   * joins two images horizontal
   */
  public static BufferedImage joinHorizontal(BufferedImage i1, BufferedImage i2, int mergeWidth) {
    BufferedImage imgClone =
        new BufferedImage(mergeWidth, i2.getHeight(), BufferedImage.TYPE_INT_ARGB);
    Graphics2D cloneG = imgClone.createGraphics();
    cloneG.drawImage(i2, 0, 0, null);
    cloneG.setComposite(AlphaComposite.getInstance(AlphaComposite.DST_IN, 0.5f));
    cloneG.drawImage(i2, 0, 0, null);

    BufferedImage result =
        new BufferedImage(
            i1.getWidth() + i2.getWidth() - mergeWidth,
            i1.getHeight(),
            BufferedImage.TYPE_INT_ARGB);
    Graphics2D g = result.createGraphics();
    g.drawImage(i1, 0, 0, null);
    g.drawImage(
        i2.getSubimage(mergeWidth, 0, i2.getWidth() - mergeWidth, i2.getHeight()),
        i1.getWidth(),
        0,
        null);
    g.drawImage(imgClone, i1.getWidth() - mergeWidth, 0, null);
    return result;
  }

  /**
   * <a
   * href="https://stackoverflow.com/questions/16092573/i-want-to-merge-4-pictures-together-in-java">...</a>
   * Joins two images vertical
   */
  public static BufferedImage joinVertical(BufferedImage i1, BufferedImage i2, int mergeHeight) {
    BufferedImage imgClone =
        new BufferedImage(i2.getWidth(), mergeHeight, BufferedImage.TYPE_INT_ARGB);
    Graphics2D cloneG = imgClone.createGraphics();
    cloneG.drawImage(i2, 0, 0, null);
    cloneG.setComposite(AlphaComposite.getInstance(AlphaComposite.DST_IN, 0.5f));
    cloneG.drawImage(i2, 0, 0, null);

    BufferedImage result =
        new BufferedImage(
            i1.getWidth(),
            i1.getHeight() + i2.getHeight() - mergeHeight,
            BufferedImage.TYPE_INT_ARGB);
    Graphics2D g = result.createGraphics();
    g.drawImage(i1, 0, 0, null);
    g.drawImage(
        i2.getSubimage(0, mergeHeight, i2.getWidth(), i2.getHeight() - mergeHeight),
        0,
        i1.getHeight(),
        null);
    g.drawImage(imgClone, 0, i1.getHeight() - mergeHeight, null);
    return result;
  }

  /**
   * rotates a BufferedImage on a specific direction. <a
   * href="https://stackoverflow.com/questions/37758061/rotate-a-buffered-image-in-java">...</a>
   *
   * @param inputImage is the BufferedImage to be rotated
   * @param direction how far image should be rotated
   * @return rotatedImage which has been rotated
   */
  public static BufferedImage rotate(BufferedImage inputImage, Direction direction) {
    if (direction == Direction.NORTH) {
      return inputImage;
    }
    int w = inputImage.getWidth();
    int h = inputImage.getHeight();
    BufferedImage rotatedImage = new BufferedImage(w, h, inputImage.getType());
    Graphics2D graphic = rotatedImage.createGraphics();
    graphic.rotate(
        Math.toRadians((double) direction.getNumber() * 90), (double) w / 2, h / (double) 2);
    graphic.drawImage(inputImage, null, 0, 0);
    graphic.dispose();
    return rotatedImage;
  }
}
