package de.uol.swp.server;

import com.google.inject.Singleton;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** ServerConfig Class. */
@Singleton
@SuppressWarnings({"java:S2130", "java:S3077", "UnnecessaryBoxing"})
public class ServerConfig {

  private static final Logger LOG = LogManager.getLogger(ServerConfig.class);
  private static final String CONFIG_FILE_NAME = "/config/Server.conf";
  private static final String CONFIG_FILE_NEXT_TO_JAR = "./Server.conf";
  private static final String PROPERTYNAME_PORT = "server.port";
  private static final String PROPERTYNAME_MOCKUP = "server.MockupMode";
  private static final String PROPERTYNAME_LOGTOFILE = "server.logToFile";
  private static final String PROPERTYNAME_LOGLEVEL = "server.logLevel";
  private static final String PROPERTYNAME_THREAD = "server.thread";
  private static final String PROPERTYNAME_AI_THREAD = "server.ai_thread";
  private static final String PROPERTYNAME_THRESHOLD = "server.threshold";
  private static final String PROPERTYNAME_VERSION = "server.version";
  private static volatile ServerConfig instance = null;
  private final Properties defaultProperties = new Properties();
  private final Properties properties;

  /** Load the configfile and set default values if necessary. */
  @SuppressWarnings("java:S2629")
  private ServerConfig() {
    LOG.debug("ServerConfig created: {}{}", this, (instance == null) ? "" : " - DUPLICATION!");
    /* The default values are set here */
    defaultProperties.setProperty(PROPERTYNAME_PORT, "52085");
    defaultProperties.setProperty(PROPERTYNAME_MOCKUP, "false");
    defaultProperties.setProperty(PROPERTYNAME_LOGTOFILE, "false");
    defaultProperties.setProperty(PROPERTYNAME_LOGLEVEL, "info");
    defaultProperties.setProperty(PROPERTYNAME_THREAD, "5");
    defaultProperties.setProperty(PROPERTYNAME_AI_THREAD, "5");
    defaultProperties.setProperty(PROPERTYNAME_THRESHOLD, "2000");
    defaultProperties.setProperty(PROPERTYNAME_VERSION, "intern");

    properties = new Properties(defaultProperties);
    URL url = getClass().getResource(CONFIG_FILE_NAME);
    try (FileInputStream file = new FileInputStream(CONFIG_FILE_NEXT_TO_JAR)) {
      properties.load(file);
    } catch (IOException e) {
      LOG.info("Switch to resources server config");
      try  {
        FileInputStream fileInputStream = new FileInputStream(url.getFile());
        properties.load(fileInputStream);
      } catch (FileNotFoundException exception) {
        LOG.warn("The config file was not found {}", exception.getMessage());
      } catch (IOException | SecurityException exception) {
        LOG.warn("Error loading config file. {}", exception.getMessage());
      }
    }
    LOG.info("Config file ({}) has been loaded.", properties.getProperty(PROPERTYNAME_VERSION));
  }

  /** Return to the old Instance of the ServerConfig. */
  public static ServerConfig getInstance() {
    if (instance == null) {
      instance = new ServerConfig();
    }
    return instance;
  }

  /** Return the Port value of the Server. */
  public int getPort() {
    int port = Integer.parseInt(defaultProperties.getProperty(PROPERTYNAME_PORT));
    try {
      port = Integer.parseInt(properties.getProperty(PROPERTYNAME_PORT));
    } catch (Exception ignored) {
      // Empty on purpose
    }
    return port;
  }

  /**
   * Getter for the database address.
   *
   * @return database address as a String
   */
  @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
  public String getMySQLServerAddress() {
    return properties.getProperty("database.MySQLServerAddress");
  }

  /**
   * Getter for the database port.
   *
   * @return database port as a String
   */
  @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
  public String getMySQLServerPort() {
    return properties.getProperty("database.MySQLServerPort");
  }

  /**
   * Getter for the database name.
   *
   * @return database name as a String
   */
  @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
  public String getMySQLServerDatabase() {
    return properties.getProperty("database.MySQLServerDatabase");
  }

  /**
   * Getter for the database username to login.
   *
   * @return database username to login as a String
   */
  @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
  public String getMySQLServerUsername() {
    return properties.getProperty("database.MySQLServerUsername");
  }

  /**
   * Getter for the database password to login.
   *
   * @return database password to login as a String
   */
  @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
  public String getMySQLServerPassword() {
    return properties.getProperty("database.MySQLServerPassword");
  }

  /**
   * Getter for the option, to use the database.
   *
   * @return database option as a boolean
   */
  public boolean getDatabaseOn() {
    boolean returnValue = Boolean.valueOf(defaultProperties.getProperty("database.active"));
    try {
      returnValue = Boolean.valueOf(properties.getProperty("database.active"));
    } catch (Exception ignored) {
      // Empty on purpose
    }
    return returnValue;
  }

  /** Returns whether the server is in MockupMode. */
  public boolean isMockupMode() {
    boolean returnValue = Boolean.valueOf(defaultProperties.getProperty(PROPERTYNAME_MOCKUP));
    try {
      returnValue = Boolean.valueOf(properties.getProperty(PROPERTYNAME_MOCKUP));
    } catch (Exception ignored) {
      // Empty on purpose
    }
    return returnValue;
  }

  /**
   * Returns the LogLevel configured in the config file.
   *
   * @return The LogLevel as a String
   */
  public String getLogLevel() {
    return String.valueOf(properties.getProperty(PROPERTYNAME_LOGLEVEL));
  }

  /**
   * Returns whether the server should log to a file.
   *
   * @return True if the server should log to a file
   */
  public boolean logToFile() {
    boolean returnValue = Boolean.valueOf(defaultProperties.getProperty(PROPERTYNAME_LOGTOFILE));
    try {
      returnValue = Boolean.valueOf(properties.getProperty(PROPERTYNAME_LOGTOFILE));
    } catch (Exception ignored) {
      // Empty on purpose
    }
    return returnValue;
  }

  /**
   * Getter for the number of threads.
   *
   * @return the number of threads that the gameLogicService can use.
   */
  public int getThreadCount() {
    int count = Integer.parseInt(defaultProperties.getProperty(PROPERTYNAME_THREAD));
    try {
      count = Integer.parseInt(properties.getProperty(PROPERTYNAME_THREAD));
    } catch (Exception ignored) {
      // Empty on purpose
    }
    return count;
  }

  /**
   * Getter for the number of AI threads.
   *
   * @return the number of AI threads that the aiService can use.
   */
  public int getAiThreadCount() {
    int count = Integer.parseInt(defaultProperties.getProperty(PROPERTYNAME_AI_THREAD));
    try {
      count = Integer.parseInt(properties.getProperty(PROPERTYNAME_AI_THREAD));
    } catch (Exception ignored) {
      // Empty on purpose
    }
    return count;
  }

  /**
   * Get the Threshold for the CardRequest for the server in ms.
   *
   * @return the threshold
   */
  public int getThreshold() {
    int threshold = Integer.parseInt(defaultProperties.getProperty(PROPERTYNAME_THRESHOLD));
    try {
      threshold = Integer.parseInt(properties.getProperty(PROPERTYNAME_THRESHOLD));
    } catch (Exception ignored) {
      // Empty on purpose
    }
    return threshold;
  }
}
