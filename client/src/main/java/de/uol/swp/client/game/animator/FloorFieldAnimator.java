package de.uol.swp.client.game.animator;

import de.uol.swp.client.textureatlas.TextureAtlas;
import java.util.ArrayList;
import java.util.List;
import javafx.animation.ParallelTransition;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.ImageView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class is used to animate the floor tiles. It is used to generate ImageViews for the GridPane
 * of the GamePresenter.
 */
public class FloorFieldAnimator {
  private static final Logger LOG = LogManager.getLogger(FloorFieldAnimator.class);
  private static final double OFFSET_X = 0;
  private static final double OFFSET_Y = 0;
  private static final double WIDTH = TextureAtlas.getResolution();
  private static final double HEIGHT = TextureAtlas.getResolution();
  // normal ImageViews
  private final List<ImageView> expressBelt = new ArrayList<>();
  private final List<ImageView> belt = new ArrayList<>();
  private final List<ImageView> pusher1 = new ArrayList<>();
  private final List<ImageView> pusher2 = new ArrayList<>();
  private final List<ImageView> pusher3 = new ArrayList<>();
  private final List<ImageView> pusher4 = new ArrayList<>();
  private final List<ImageView> pusher5 = new ArrayList<>();
  private final List<ImageView> gear = new ArrayList<>();
  private final List<ImageView> laser = new ArrayList<>();
  private final List<ImageView> repair = new ArrayList<>();

  // special ImageViews
  @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
  private final List<ImageView> expressBeltSP = new ArrayList<>();

  @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
  private final List<ImageView> beltSP = new ArrayList<>();

  @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
  private final List<ImageView> pusher1SP = new ArrayList<>();

  @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
  private final List<ImageView> pusher2SP = new ArrayList<>();

  @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
  private final List<ImageView> pusher3SP = new ArrayList<>();

  @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
  private final List<ImageView> pusher4SP = new ArrayList<>();

  @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
  private final List<ImageView> pusher5SP = new ArrayList<>();

  @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
  private final List<ImageView> press1SP = new ArrayList<>();

  @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
  private final List<ImageView> press2SP = new ArrayList<>();

  @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
  private final List<ImageView> press3SP = new ArrayList<>();

  @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
  private final List<ImageView> press4SP = new ArrayList<>();

  @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
  private final List<ImageView> press5SP = new ArrayList<>();

  @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
  private final List<ImageView> laserSP = new ArrayList<>();

  // no animation
  @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
  private final List<ImageView> noAnimation = new ArrayList<>();

  // Single animation list
  private final List<SpriteAnimation> expressBeltAnimation = new ArrayList<>();
  private final List<SpriteAnimation> beltAnimation = new ArrayList<>();
  private final List<SpriteAnimation> pusher1Animation = new ArrayList<>();
  private final List<SpriteAnimation> pusher2Animation = new ArrayList<>();
  private final List<SpriteAnimation> pusher3Animation = new ArrayList<>();
  private final List<SpriteAnimation> pusher4Animation = new ArrayList<>();
  private final List<SpriteAnimation> pusher5Animation = new ArrayList<>();
  private final List<SpriteAnimation> gearAnimation = new ArrayList<>();
  private final List<SpriteAnimation> press1Animation = new ArrayList<>();
  private final List<SpriteAnimation> press2Animation = new ArrayList<>();
  private final List<SpriteAnimation> press3Animation = new ArrayList<>();
  private final List<SpriteAnimation> press4Animation = new ArrayList<>();
  private final List<SpriteAnimation> press5Animation = new ArrayList<>();
  private final List<SpriteAnimation> laserAnimation = new ArrayList<>();
  private final List<SpriteAnimation> repairAnimation = new ArrayList<>();

  // Animatons
  private final ParallelTransition expressBeltParallelTransition = new ParallelTransition();
  private final ParallelTransition beltParallelTransition = new ParallelTransition();
  private final ParallelTransition pusher1ParallelTransition = new ParallelTransition();
  private final ParallelTransition pusher2ParallelTransition = new ParallelTransition();
  private final ParallelTransition pusher3ParallelTransition = new ParallelTransition();
  private final ParallelTransition pusher4ParallelTransition = new ParallelTransition();
  private final ParallelTransition pusher5ParallelTransition = new ParallelTransition();
  private final ParallelTransition press1ParallelTransition = new ParallelTransition();
  private final ParallelTransition press2ParallelTransition = new ParallelTransition();
  private final ParallelTransition press3ParallelTransition = new ParallelTransition();
  private final ParallelTransition press4ParallelTransition = new ParallelTransition();
  private final ParallelTransition press5ParallelTransition = new ParallelTransition();
  private final ParallelTransition gearParallelTransition = new ParallelTransition();
  private final ParallelTransition laserParallelTransition = new ParallelTransition();
  private final ParallelTransition repairParallelTransition = new ParallelTransition();

  /** Constructor. */
  public FloorFieldAnimator() {
    LOG.debug("Created {}", this.getClass().getSimpleName());
  }

  /**
   * This method generate an ImageView with an Image.
   *
   * @param id The id of the image
   * @param rotation The rotation in degrees (0, 90, 180, 270)
   * @return ImageView for the GridPane
   */
  @SuppressWarnings({"java:S1871", "java:S3776"})
  public ImageView getImageView(int id, int rotation) {
    ImageView imageView = new ImageView(TextureAtlas.getFloorSprite(id, rotation));
    imageView.setViewport(new Rectangle2D(OFFSET_X, OFFSET_Y, WIDTH, HEIGHT));
    if (id == 0 || id == 1 || id == 2) {
      noAnimation.add(imageView);
      return imageView;
    } else if (id >= 3 && id <= 11) {
      expressBelt.add(imageView);
      return imageView;
    } else if (id >= 12 && id <= 26) {
      belt.add(imageView);
      return imageView;
    } else if (id == 32) {
      pusher1.add(imageView);
      return imageView;
    } else if (id == 33) {
      pusher1.add(imageView);
      pusher3.add(imageView);
      pusher5.add(imageView);
      return imageView;
    } else if (id == 34) {
      pusher2.add(imageView);
      return imageView;
    } else if (id == 35) {
      pusher2.add(imageView);
      pusher4.add(imageView);
      return imageView;
    } else if (id == 36) {
      pusher3.add(imageView);
      return imageView;
    } else if (id == 37 || id == 38) {
      gear.add(imageView);
      return imageView;
    } else if (id == 39) {
      beltSP.add(imageView);
      press1SP.add(imageView);
      press5SP.add(imageView);
      return imageView;
    } else if (id == 40 || id == 41) {
      beltSP.add(imageView);
      press2SP.add(imageView);
      press4SP.add(imageView);
      return imageView;
    } else if (id == 42) {
      beltSP.add(imageView);
      press3SP.add(imageView);
      return imageView;
    } else if (id >= 48 && id <= 56) {
      laser.add(imageView);
      return imageView;
    } else if (id >= 64 && id <= 65) {
      noAnimation.add(imageView);
      return imageView;
    } else if (id >= 66 && id <= 69) {
      repair.add(imageView);
      return imageView;
    } else if (id >= 70 && id <= 71) {
      noAnimation.add(imageView);
      return imageView;
    } else if (id == 72) {
      laser.add(imageView);
      return imageView;
    } else if (id == 73) {
      noAnimation.add(imageView);
      return imageView;
    } else if (id >= 80 && id <= 86) {
      expressBeltSP.add(imageView);
      laserSP.add(imageView);
      return imageView;
    } else if (id >= 87 && id <= 93) {
      beltSP.add(imageView);
      laserSP.add(imageView);
      return imageView;
    } else if (id == 94) {
      expressBeltSP.add(imageView);
      pusher1SP.add(imageView);
      pusher3SP.add(imageView);
      pusher5SP.add(imageView);
      return imageView;
    } else if (id == 95) {
      expressBeltSP.add(imageView);
      pusher2SP.add(imageView);
      pusher4SP.add(imageView);
      return imageView;
    } else if (id == 96) {
      laser.add(imageView);
      return imageView;
    } else if (id == 98 || id == 99) {
      laser.add(imageView);
      return imageView;
    }
    LOG.error("Unused id was fetched. Returning empty ImageView.");
    return new ImageView();
  }

  /** This method is used to generate the animations. */
  public void start() {
    buildNormalAnimations();
    buildSpecialAnimations();
    generateParallelAnimations();
  }

  private void buildNormalAnimations() {
    for (ImageView imageView : expressBelt) {
      expressBeltAnimation.add(new SpriteAnimation(imageView));
    }
    for (ImageView imageView : belt) {
      beltAnimation.add(new SpriteAnimation(imageView));
    }
    for (ImageView imageView : pusher1) {
      pusher1Animation.add(new SpriteAnimation(imageView));
    }
    for (ImageView imageView : pusher2) {
      pusher2Animation.add(new SpriteAnimation(imageView));
    }
    for (ImageView imageView : pusher3) {
      pusher3Animation.add(new SpriteAnimation(imageView));
    }
    for (ImageView imageView : pusher4) {
      pusher4Animation.add(new SpriteAnimation(imageView));
    }
    for (ImageView imageView : pusher5) {
      pusher5Animation.add(new SpriteAnimation(imageView));
    }
    for (ImageView imageView : gear) {
      gearAnimation.add(new SpriteAnimation(imageView));
    }
    for (ImageView imageView : laser) {
      laserAnimation.add(new SpriteAnimation(imageView));
    }
    for (ImageView imageView : repair) {
      repairAnimation.add(new SpriteAnimation(imageView));
    }
  }

  private void buildSpecialAnimations() {
    for (ImageView imageView : expressBeltSP) {
      expressBeltAnimation.add(new SpriteAnimation(imageView, 15, true));
    }
    for (ImageView imageView : beltSP) {
      beltAnimation.add(new SpriteAnimation(imageView, 15, true));
    }
    for (ImageView imageView : pusher1SP) {
      pusher1Animation.add(new SpriteAnimation(imageView, 13, false));
    }
    for (ImageView imageView : pusher2SP) {
      pusher2Animation.add(new SpriteAnimation(imageView, 13, false));
    }
    for (ImageView imageView : pusher3SP) {
      pusher3Animation.add(new SpriteAnimation(imageView, 13, false));
    }
    for (ImageView imageView : pusher4SP) {
      pusher4Animation.add(new SpriteAnimation(imageView, 13, false));
    }
    for (ImageView imageView : pusher5SP) {
      pusher5Animation.add(new SpriteAnimation(imageView, 13, false));
    }
    for (ImageView imageView : press1SP) {
      press1Animation.add(new SpriteAnimation(imageView, 12, false));
    }
    for (ImageView imageView : press2SP) {
      press2Animation.add(new SpriteAnimation(imageView, 12, false));
    }
    for (ImageView imageView : press3SP) {
      press3Animation.add(new SpriteAnimation(imageView, 12, false));
    }
    for (ImageView imageView : press4SP) {
      press4Animation.add(new SpriteAnimation(imageView, 12, false));
    }
    for (ImageView imageView : press5SP) {
      press5Animation.add(new SpriteAnimation(imageView, 12, false));
    }
    for (ImageView imageView : laserSP) {
      laserAnimation.add(new SpriteAnimation(imageView, 4, false));
    }
  }

  private void generateParallelAnimations() {
    expressBeltParallelTransition.getChildren().addAll(expressBeltAnimation);

    beltParallelTransition.getChildren().addAll(expressBeltAnimation);
    beltParallelTransition.getChildren().addAll(beltAnimation);

    pusher1ParallelTransition.getChildren().addAll(pusher1Animation);
    pusher2ParallelTransition.getChildren().addAll(pusher2Animation);
    pusher3ParallelTransition.getChildren().addAll(pusher3Animation);
    pusher4ParallelTransition.getChildren().addAll(pusher4Animation);
    pusher5ParallelTransition.getChildren().addAll(pusher5Animation);

    press1ParallelTransition.getChildren().addAll(press1Animation);
    press2ParallelTransition.getChildren().addAll(press2Animation);
    press3ParallelTransition.getChildren().addAll(press3Animation);
    press4ParallelTransition.getChildren().addAll(press4Animation);
    press5ParallelTransition.getChildren().addAll(press5Animation);

    gearParallelTransition.getChildren().addAll(gearAnimation);
    laserParallelTransition.getChildren().addAll(laserAnimation);
    repairParallelTransition.getChildren().addAll(repairAnimation);
  }

  public ParallelTransition getBeltParallelTransition() {
    return beltParallelTransition;
  }

  public ParallelTransition getExpressBeltParallelTransition() {
    return expressBeltParallelTransition;
  }

  public ParallelTransition getPusher1ParallelTransition() {
    return pusher1ParallelTransition;
  }

  public ParallelTransition getPusher2ParallelTransition() {
    return pusher2ParallelTransition;
  }

  public ParallelTransition getPusher3ParallelTransition() {
    return pusher3ParallelTransition;
  }

  public ParallelTransition getPusher4ParallelTransition() {
    return pusher4ParallelTransition;
  }

  public ParallelTransition getPusher5ParallelTransition() {
    return pusher5ParallelTransition;
  }

  public ParallelTransition getPress1ParallelTransition() {
    return press1ParallelTransition;
  }

  public ParallelTransition getPress2ParallelTransition() {
    return press2ParallelTransition;
  }

  public ParallelTransition getPress3ParallelTransition() {
    return press3ParallelTransition;
  }

  public ParallelTransition getPress4ParallelTransition() {
    return press4ParallelTransition;
  }

  public ParallelTransition getPress5ParallelTransition() {
    return press5ParallelTransition;
  }

  public ParallelTransition getGearParallelTransition() {
    return gearParallelTransition;
  }

  public ParallelTransition getLaserParallelTransition() {
    return laserParallelTransition;
  }

  public ParallelTransition getRepairParallelTransition() {
    return repairParallelTransition;
  }
}
