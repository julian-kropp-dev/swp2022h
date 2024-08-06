package de.uol.swp.common.helper;

import com.google.inject.Singleton;
import de.uol.swp.common.game.floor.Direction;
import de.uol.swp.common.game.floor.FloorPlanSetting;
import de.uol.swp.common.game.floor.FloorPlanSetting.FloorPlans;
import java.awt.Point;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Class containing helper Methods for cvs-String manipulation. */
@Singleton
public class HelperMethods {
  private static final Logger LOG = LogManager.getLogger(HelperMethods.class);

  private HelperMethods() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * Builds a csv-String based on the plans selected by the users.
   *
   * @param floorPlanSettings Enumeration for the different FloorFlans
   * @return csv-String
   * @throws IllegalStateException if the plan does not exist as a csv-file
   */
  public static String convertFloorPlan(
      FloorPlanSetting[] floorPlanSettings, Map<Integer, EnumMap<FloorPlans, Point>> checkpoints)
      throws IllegalStateException {
    String[][] csvArray = new String[2][2];
    FloorPlanSetting[] alignedFloorPlanSettings = alignFloorPlan(floorPlanSettings);
    Map<Integer, EnumMap<FloorPlans, Point>> transformedcheckpoints =
        transformPoint(checkpoints, floorPlanSettings);
    for (int i = 0; i < alignedFloorPlanSettings.length; i++) {
      int row = i / 2;
      int col = i % 2;
      if (alignedFloorPlanSettings[i].getFloorPlansEnum() == FloorPlans.EMPTY) {
        csvArray[row][col] = null;
      } else {
        String resourceName =
            "/floorPlans/"
                + alignedFloorPlanSettings[i].getFloorPlansEnum().name().toLowerCase()
                + ".csv";
        try {
          URL path = HelperMethods.class.getResource(resourceName);
          InputStream inputStream = path.openStream();
          String content;
          try (Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8)) {
            content = scanner.useDelimiter("\\A").next();
          }
          String csvString = content;
          csvArray[row][col] = rotateCsv(csvString, alignedFloorPlanSettings[i].getDirection());
          for (int j = 1; j <= 6; j++) {
            if (transformedcheckpoints != null
                && transformedcheckpoints.containsKey(j)
                && transformedcheckpoints
                    .get(j)
                    .containsKey(alignedFloorPlanSettings[i].getFloorPlansEnum())) {
              csvArray[row][col] =
                  mergeCheckpointsIn2dArray(
                      csvArray[row][col],
                      transformedcheckpoints
                          .get(j)
                          .get(alignedFloorPlanSettings[i].getFloorPlansEnum()),
                      j);
            }
          }

        } catch (Exception e) {
          LOG.error("The plan with path {} does not exists", resourceName);
          throw new IllegalStateException("IO Exception");
        }
      }
    }
    return buildCsvFrom2dArray(csvArray);
  }

  private static FloorPlanSetting[] alignFloorPlan(FloorPlanSetting[] floorPlanSettings) {
    if (floorPlanSettings.length != 1 && isVerticalLeft(floorPlanSettings)) {
      FloorPlanSetting[] alignedFloorPlanSettings = new FloorPlanSetting[4];
      alignedFloorPlanSettings[0] = floorPlanSettings[0];
      alignedFloorPlanSettings[0].setDirection(
          Direction.get((floorPlanSettings[0].getDirection().ordinal() - 1 + 4) % 4));
      alignedFloorPlanSettings[1] = floorPlanSettings[2];
      alignedFloorPlanSettings[1].setDirection(
          Direction.get((floorPlanSettings[2].getDirection().ordinal() - 1 + 4) % 4));
      alignedFloorPlanSettings[2] = new FloorPlanSetting(FloorPlans.EMPTY);
      alignedFloorPlanSettings[3] = new FloorPlanSetting(FloorPlans.EMPTY);
      return alignedFloorPlanSettings;
    } else if (floorPlanSettings.length != 1 && isVerticalRight(floorPlanSettings)) {
      FloorPlanSetting[] alignedFloorPlanSettings = new FloorPlanSetting[4];
      alignedFloorPlanSettings[0] = floorPlanSettings[1];
      alignedFloorPlanSettings[0].setDirection(
          Direction.get((floorPlanSettings[1].getDirection().ordinal() - 1 + 4) % 4));
      alignedFloorPlanSettings[1] = floorPlanSettings[3];
      alignedFloorPlanSettings[1].setDirection(
          Direction.get((floorPlanSettings[3].getDirection().ordinal() - 1 + 4) % 4));
      alignedFloorPlanSettings[2] = new FloorPlanSetting(FloorPlans.EMPTY);
      alignedFloorPlanSettings[3] = new FloorPlanSetting(FloorPlans.EMPTY);
      return alignedFloorPlanSettings;
    }
    return floorPlanSettings;
  }

  private static Map<Integer, EnumMap<FloorPlans, Point>> transformPoint(
      Map<Integer, EnumMap<FloorPlans, Point>> checkpoints, FloorPlanSetting[] floorPlanSettings) {
    if (floorPlanSettings.length == 1) {
      return checkpoints;
    }
    if (isVerticalLeft(floorPlanSettings) || isVerticalRight(floorPlanSettings)) {
      Map<Integer, EnumMap<FloorPlans, Point>> transformedPoints = new HashMap<>();
      for (Map.Entry<Integer, EnumMap<FloorPlans, Point>> entry : checkpoints.entrySet()) {
        Integer key = entry.getKey();
        EnumMap<FloorPlans, Point> floorPlanMap = entry.getValue();
        EnumMap<FloorPlans, Point> rotatedFloorPlanMap = new EnumMap<>(FloorPlans.class);
        for (Map.Entry<FloorPlans, Point> entry1 : floorPlanMap.entrySet()) {
          FloorPlans floorPlan = entry1.getKey();
          Point originalPoint = entry1.getValue();

          int originalRow = originalPoint.x;
          int originalColumn = originalPoint.y;
          int sizeOfMatrix = FloorPlanSetting.FLOOR_PLAN_SIZE;

          int newRow = (sizeOfMatrix - 1) - originalColumn;

          Point rotatedPoint = new Point(newRow, originalRow);
          rotatedFloorPlanMap.put(floorPlan, rotatedPoint);
        }
        transformedPoints.put(key, rotatedFloorPlanMap);
      }
      return transformedPoints;
    } else {
      return checkpoints;
    }
  }

  private static boolean isVerticalLeft(FloorPlanSetting[] floorPlanSettings) {
    return (floorPlanSettings[0].getFloorPlansEnum() != FloorPlans.EMPTY
        && floorPlanSettings[1].getFloorPlansEnum() == FloorPlans.EMPTY
        && floorPlanSettings[2].getFloorPlansEnum() != FloorPlans.EMPTY
        && floorPlanSettings[3].getFloorPlansEnum() == FloorPlans.EMPTY);
  }

  private static boolean isVerticalRight(FloorPlanSetting[] floorPlanSettings) {
    return (floorPlanSettings[0].getFloorPlansEnum() == FloorPlans.EMPTY
        && floorPlanSettings[1].getFloorPlansEnum() != FloorPlans.EMPTY
        && floorPlanSettings[2].getFloorPlansEnum() == FloorPlans.EMPTY
        && floorPlanSettings[3].getFloorPlansEnum() != FloorPlans.EMPTY);
  }

  private static String mergeCheckpointsIn2dArray(String string, Point checkpoint, int number) {
    String[] rows = string.split("\\R");
    String[] cols = rows[checkpoint.x].split(";");

    if (cols[checkpoint.y].length() == 3) {
      cols[checkpoint.y] = cols[checkpoint.y] + number;
    } else {
      cols[checkpoint.y] = cols[checkpoint.y].substring(0, 3) + number;
    }

    rows[checkpoint.x] = String.join(";", cols);
    String returnString = String.join(System.lineSeparator(), rows);

    if (returnString.endsWith("\\R")) {
      returnString = returnString.substring(0, returnString.length() - 1);
    }

    return returnString;
  }

  @SuppressWarnings("java:S3776")
  protected static String buildCsvFrom2dArray(String[][] csvArray) {
    StringBuilder csvBuilder = new StringBuilder();
    boolean alreadyJoined = false;
    if (csvArray[0][0] != null && csvArray[0][1] != null) {
      csvBuilder.append(joinHorizontal(csvArray[0][0], csvArray[0][1]));
      alreadyJoined = true;
    }
    if (csvArray[1][0] != null && csvArray[1][1] != null) {
      csvBuilder.append(joinHorizontal(csvArray[1][0], csvArray[1][1]));
      alreadyJoined = true;
    }
    if (csvArray[0][0] != null && csvArray[1][0] != null && !alreadyJoined) {
      csvBuilder.append(csvArray[0][0]).append(System.lineSeparator());
      csvBuilder.append(csvArray[1][0]);
      alreadyJoined = true;
    }
    if (csvArray[0][1] != null && csvArray[1][1] != null && !alreadyJoined) {
      csvBuilder.append(csvArray[0][1]).append(System.lineSeparator());
      csvBuilder.append(csvArray[1][1]);
      alreadyJoined = true;
    }
    if (!alreadyJoined) {
      if (csvArray[0][0] != null) {
        return csvArray[0][0];
      }
      if (csvArray[1][0] != null) {
        return csvArray[1][0];
      }
      if (csvArray[0][1] != null) {
        return csvArray[0][1];
      }
      if (csvArray[1][1] != null) {
        return csvArray[1][1];
      }
    }

    String returnString = csvBuilder.toString();
    if (returnString.endsWith("\\R")) {
      returnString = returnString.substring(0, returnString.length() - 1);
    }
    return returnString;
  }

  /**
   * Joins two csv-Strings horizontally.
   *
   * @param a String for the left side
   * @param b String for the right side
   * @return String a and b as one.
   */
  public static String joinHorizontal(String a, String b) {
    StringBuilder joinedCsv = new StringBuilder();
    String[] rows1 = a.split("\\R");
    String[] rows2 = b.split("\\R");
    for (int i = 0; i < rows1.length; i++) {
      String row = rows1[i] + ";" + rows2[i] + System.lineSeparator();
      joinedCsv.append(row);
    }
    return joinedCsv.toString();
  }

  /**
   * Rotates the given CSV string by the specified direction.
   *
   * @param csv the CSV string to rotate
   * @param rotationAngle the angle to rotate the CSV string by
   * @return the rotated CSV string
   */
  @SuppressWarnings("java:S3776")
  protected static String rotateCsv(String csv, Direction rotationAngle) {
    String[] rows = csv.split("\\R");
    StringBuilder result = new StringBuilder();

    switch (rotationAngle) {
      case EAST: // 90 +1
        for (int i = 0; i < rows[0].split(";").length; i++) {
          for (int j = rows.length - 1; j >= 0; j--) {
            String[] cols = rows[j].split(";");
            result.append(replaceFirstChar(cols[i], 1));
            if (j != 0) {
              result.append(";");
            }
          }
          result.append(System.lineSeparator());
        }
        break;
      case SOUTH: // 180 + 2
        for (int i = rows.length - 1; i >= 0; i--) {
          String[] cols = rows[i].split(";");
          for (int j = cols.length - 1; j >= 0; j--) {
            result.append(replaceFirstChar(cols[j], 2));
            if (j != 0) {
              result.append(";");
            }
          }
          if (i != 0) {
            result.append(System.lineSeparator());
          }
        }
        break;
      case WEST: // 270 + 3
        for (int i = rows[0].split(";").length - 1; i >= 0; i--) {
          for (int j = 0; j < rows.length; j++) {
            String[] cols = rows[j].split(";");
            result.append(replaceFirstChar(cols[i], 3));
            if (j != rows.length - 1) {
              result.append(";");
            }
          }
          result.append(System.lineSeparator());
        }
        break;
      default:
        // Default to 0 degree rotation
        result.append(csv);
        break;
    }

    return result.toString();
  }

  /**
   * Converts a CSV string into a two-dimensional integer array for the id. The input CSV string
   * should have rows separated by a newline character and columns separated by a semicolon
   * character. Each value in the CSV string should be a non-negative integer.
   *
   * @param csvString a CSV string to be converted into an integer array
   * @return a two-dimensional integer array representing the values in the input CSV string
   * @throws NumberFormatException if any value in the CSV string is not a non-negative integer
   * @throws NullPointerException if the input CSV string is null
   */
  public static int[][] csvToIntArrayId(String csvString) {
    String[] rows = csvString.split("\\R");
    int[][] array = new int[rows.length][];
    for (int i = 0; i < rows.length; i++) {
      String[] cols = rows[i].split(";");
      int[] row = new int[cols.length];
      for (int j = 0; j < cols.length; j++) {
        row[j] = Integer.parseInt(cols[j].substring(1, 3));
      }
      array[i] = row;
    }
    return array;
  }

  /**
   * Converts a two-dimensional string array into a single string.
   *
   * @param stringArray a two-dimensional string array to be converted into a single string
   * @return a singe string with Semicolons and linebreaks in the end of a line.
   */
  public static String stringArrayToString(String[][] stringArray) {
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < stringArray.length; i++) {
      for (int j = 0; j < stringArray[i].length; j++) {
        result.append(stringArray[i][j]);
        if (j < stringArray[i].length - 1) {
          result.append(";");
        }
      }
      if (i < stringArray.length - 1) {
        result.append(System.lineSeparator());
      }
    }
    return result.toString();
  }

  /**
   * Converts a CSV string into a two-dimensional integer array for the rotation. The input CSV
   * string should have rows separated by a newline character and columns separated by a semicolon
   * character. Each value in the CSV string should be a non-negative integer.
   *
   * @param csvString a CSV string to be converted into an integer array
   * @return a two-dimensional integer array representing the values in the input CSV string
   * @throws NumberFormatException if any value in the CSV string is not a non-negative integer
   * @throws NullPointerException if the input CSV string is null
   */
  public static int[][] csvToIntArrayRotation(String csvString) {
    String[] rows = csvString.split("\\R");
    int[][] array = new int[rows.length][];
    for (int i = 0; i < rows.length; i++) {
      String[] cols = rows[i].split(";");
      int[] row = new int[cols.length];
      for (int j = 0; j < cols.length; j++) {
        row[j] = Integer.parseInt(cols[j].substring(0, 1));
      }
      array[i] = row;
    }
    return array;
  }

  /**
   * Clamps the given integer value to the range of 0 to 3 (inclusive). If the input value is less
   * than 0, it will add 4 until it is within the range. If the input value is greater than 3, it
   * will subtract 4 until it is within the range.
   *
   * @param value the input value to be clamped
   * @return the clamped value within the range of 0 to 3 (inclusive)
   */
  protected static int clampValue(int value) {
    while (value < 0 || value > 3) {
      if (value < 0) {
        value += 4;
      } else {
        value -= 4;
      }
    }
    return value;
  }

  /**
   * Replaces the third character of a string with a new character that is the original value
   * incremented by 1, clamped to the range of 0 to 3.
   *
   * @param str the input string
   * @return a new string with the third character replaced by the incremented value
   */
  private static String replaceFirstChar(String str, int increment) {
    int firstChar = Character.getNumericValue(str.charAt(0));
    int newFirstChar = clampValue(firstChar + increment);
    char newChar = Character.forDigit(newFirstChar, 10);
    return newChar + str.substring(1);
  }
}
