package de.uol.swp.client.game.animator;

import de.uol.swp.client.textureatlas.TextureAtlas;
import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

/** This class is used for animating a sprite. */
public class SpriteAnimation extends Transition {

  private static final int HEIGHT = TextureAtlas.getResolution();
  private static final int WIDTH = TextureAtlas.getResolution();
  private static final Duration duration = Duration.seconds(1);
  private final ImageView imageView;
  private final int count;
  private final int columns;
  private final int offsetY;
  private final int offsetX;
  private int lastIndex;

  /**
   * The sprite animation animates a sprite by moving the viewport of the imageview. Therefor this
   * method requires an imageview and the number of frames. The boolean toggles if the frames are
   * counted from the top of the sprite or from the bottom.
   *
   * <p>Example: fromTop = true, frameCount = 10, total length of the sprite = 12 => Only the first
   * 10 frames are played.
   *
   * <p>Example: fromTop = false, frameCount = 2, total length of the sprite = 12 => Only the last 2
   * frames are played.
   *
   * <p>The number of frames is calculates with the image length and all frames are played.
   *
   * @param imageView The imagview
   */
  public SpriteAnimation(ImageView imageView) {
    this.imageView = imageView;
    Image image = imageView.getImage();
    this.count = (int) image.getHeight() / HEIGHT;
    this.columns = 1;
    this.offsetX = 0;
    this.offsetY = 0;
    setCycleDuration(duration);
    setInterpolator(Interpolator.LINEAR);
  }

  /**
   * The sprite animation animates a sprite by moving the viewport of the imageview. Therefor this
   * method requires an imageview and the number of frames. The boolean toggles if the frames are
   * counted from the top of the sprite or from the bottom.
   *
   * <p>Example: fromTop = true, frameCount = 10, total length of the sprite = 12 => Only the first
   * 10 frames are played.
   *
   * <p>Example: fromTop = false, frameCount = 2, total length of the sprite = 12 => Only the last 2
   * frames are played.
   *
   * @param imageView The imagview
   * @param frameCount Number of frames
   * @param fromTop Toggles the count direction
   */
  public SpriteAnimation(ImageView imageView, int frameCount, boolean fromTop) {
    if (fromTop) {
      this.imageView = imageView;
      this.count = frameCount;
      this.columns = 1;
      this.offsetX = 0;
      this.offsetY = 0;
      setCycleDuration(duration);
      setInterpolator(Interpolator.LINEAR);
    } else {
      this.imageView = imageView;
      this.count = frameCount;
      this.columns = 1;
      this.offsetX = 0;
      Image image = imageView.getImage();
      this.offsetY = getOffsetY(image, frameCount);
      setCycleDuration(duration);
      setInterpolator(Interpolator.LINEAR);
    }
  }

  /**
   * Overrides the interpolate method from Transition.
   *
   * @param k between 0 and 1
   */
  @Override
  protected void interpolate(double k) {
    if (k >= 1.0) {
      // Interpolation ist abgeschlossen, setzen Sie den Wert zurück
      imageView.setViewport(new Rectangle2D(offsetX, offsetY, WIDTH, HEIGHT));
      lastIndex = 0;
    } else {
      final int index = Math.min((int) Math.floor(k * count), count - 1);
      if (index != lastIndex) {
        final int x = (index % columns) * WIDTH + offsetX;
        final int y = (index / columns) * HEIGHT + offsetY;
        imageView.setViewport(new Rectangle2D(x, y, WIDTH, HEIGHT));
        lastIndex = index;
      }
    }
  }

  private int getOffsetY(Image image, int count) {
    int height1 = (int) image.getHeight();
    return height1 - (count * HEIGHT);
  }
}
