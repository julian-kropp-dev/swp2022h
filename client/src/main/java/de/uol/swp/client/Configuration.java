package de.uol.swp.client;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Configuration. */
@SuppressWarnings("java:S1075")
public class Configuration {
  private static final Logger LOG = LogManager.getLogger(Configuration.class);

  private static final String ATLAS_PATH = "/textures/atlas.png";
  private static final String CARDS_PATH = "/textures/atlasCards.png";
  private static final String ASSETS_PATH = "/textures/entity.png";
  private static final String FLOOR_FIELD_SPRITES_BASE_PATH = "/textures/floorfieldsprites/id";
  private static final String ROBOT_IMAGE_PATH = "/robots/robots.png";
  private static final String FLOOR_PLAN_IMAGE_ORIGINAL_PATH =
      "/textures/floorplans/originalfloorplan.png";
  private static final String FLOOR_PLAN_EMPTY_PATH = "/textures/floorplans/empty.jpeg";

  @SuppressWarnings("java:S1075")
  private static final String CONFIG_PATH_RESOURCES = "/config/Client.conf";

  private static final String CONFIG_PATH_EXTERNAL = "./Client.conf";
  private static final Properties DEFAULT_PROPERTIES = new Properties();
  private static final Properties PROPERTIES;
  private static final String LOCATION = "config.location";
  private static final String PROPERTY_NAME_PORT = "client.port";
  private static final String PROPERTY_NAME_HOST = "client.host";
  private static final String PROPERTY_NAME_LOGLEVEL = "client.logLevel";
  private static final String PROPERTY_NAME_LOG_TO_FILE = "client.logToFile";

  static {
    /* The default values are set here */
    DEFAULT_PROPERTIES.setProperty(PROPERTY_NAME_PORT, "52085");
    DEFAULT_PROPERTIES.setProperty(PROPERTY_NAME_HOST, "localhost");
    DEFAULT_PROPERTIES.setProperty(PROPERTY_NAME_LOGLEVEL, "debug");
    DEFAULT_PROPERTIES.setProperty(PROPERTY_NAME_LOG_TO_FILE, "true");
    DEFAULT_PROPERTIES.setProperty(LOCATION, "default");
    PROPERTIES = new Properties(DEFAULT_PROPERTIES);

    URL url = Configuration.class.getResource(CONFIG_PATH_RESOURCES);
    FileInputStream file = null;

    // Trying to get external file
    try {
      file = new FileInputStream(CONFIG_PATH_EXTERNAL);
    } catch (FileNotFoundException e) {
      LOG.warn("External config File does not exists");
      createFile();
    }

    try {
      PROPERTIES.load(file);
    } catch (Exception e) {
      try {
        assert url != null;
        try (InputStream fileInputStream = url.openStream()) {
          PROPERTIES.load(fileInputStream);
        }
      } catch (FileNotFoundException exception) {
        LOG.warn("The config file was not found. Using default properties. - {0}", exception);
      } catch (IOException exception) {
        LOG.warn("Error loading config file. Using default properties. - {0}", exception);
      }
    }

    LOG.info(
        "Config has been loaded. Used {}-config file as the sources", PROPERTIES.get(LOCATION));
  }

  private Configuration() {}

  public static String getHostName() {
    return PROPERTIES.getProperty(PROPERTY_NAME_HOST);
  }

  public static String getTextureAtlasPath() {
    return ATLAS_PATH;
  }

  public static String getTextureAtlasCardsPath() {
    return CARDS_PATH;
  }

  public static String getTextureAtlasAssetsPath() {
    return ASSETS_PATH;
  }

  public static String getFloorFieldSpritesBasePath() {
    return FLOOR_FIELD_SPRITES_BASE_PATH;
  }

  public static String getRobotsImagePath() {
    return ROBOT_IMAGE_PATH;
  }

  public static String getFloorplanImageOriginalPath() {
    return FLOOR_PLAN_IMAGE_ORIGINAL_PATH;
  }

  public static String getFloorPlanEmptyPath() {
    return FLOOR_PLAN_EMPTY_PATH;
  }

  /**
   * Getter for the Port.
   *
   * @return the port
   */
  public static int getDefaultPort() {
    int port = Integer.parseInt(DEFAULT_PROPERTIES.getProperty(PROPERTY_NAME_PORT));
    try {
      port = Integer.parseInt(PROPERTIES.getProperty(PROPERTY_NAME_PORT));
    } catch (Exception ignored) {
      // Empty on purpose
    }
    return port;
  }

  /**
   * Getter for the debug level.
   *
   * @return the debug level
   */
  public static String getDebugLevel() {
    return PROPERTIES.getProperty(PROPERTY_NAME_LOGLEVEL);
  }

  /**
   * Getter for the boolean if Log4j should log to a file.
   *
   * @return true if logging to a file
   */
  @SuppressWarnings({"java:S2130", "UnnecessaryBoxing"})
  public static boolean getLogToFile() {
    boolean returnValue =
        Boolean.valueOf(DEFAULT_PROPERTIES.getProperty(PROPERTY_NAME_LOG_TO_FILE));
    try {
      returnValue = Boolean.valueOf(PROPERTIES.getProperty(PROPERTY_NAME_LOG_TO_FILE));
    } catch (Exception ignored) {
      // Empty on purpose
    }
    return returnValue;
  }

  private static void createFile() {
    try (FileOutputStream fileOutputStream = new FileOutputStream(CONFIG_PATH_EXTERNAL)) {

      DEFAULT_PROPERTIES.setProperty(LOCATION, "external");
      DEFAULT_PROPERTIES.store(
          fileOutputStream,
          " This file configures the client settings\n"
              + "# Lines with a leading # are ignored\n"
              + "# Invalid values are ignored. "
              + "Settings that are not set correctly will be ignored.");
      LOG.info("Created default properties file");
    } catch (IOException e) {
      LOG.error("Can not create default properties file - {}", e.getMessage());
    } finally {
      DEFAULT_PROPERTIES.setProperty(LOCATION, "default");
    }
  }
}
