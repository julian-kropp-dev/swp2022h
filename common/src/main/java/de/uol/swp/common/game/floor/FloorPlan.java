package de.uol.swp.common.game.floor;

import de.uol.swp.common.game.floor.operation.Checkpoint;
import de.uol.swp.common.helper.HelperMethods;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/** The floor plan creates a graph of floor field nodes and creates root fields for the robots. */
public class FloorPlan implements Serializable {

  private static final long serialVersionUID = 635819516349775880L;
  private final LinkedList<FloorField> robotRoot = new LinkedList<>();
  private final Map<Integer, FloorField> checkpoints = new HashMap<>();
  // 0: EmptyField, 1: Wall on one side, 2: L-Wall on two sides, 66-69: repair fields
  private final List<Integer> validIds = new ArrayList<>(Arrays.asList(0, 1, 2, 66, 67, 68, 69));
  private final boolean isTestmode;
  private FloorField[][] floorFields;
  private String floorPlanAsString;

  /**
   * Constructor to create a floor plan from a csv file.
   *
   * @param path the path of the file
   */
  public FloorPlan(Path path) {
    this(path, false);
  }

  /**
   * Constructor only for Tests.
   *
   * @param path the path of the file
   * @param testMode is in testmode
   */
  public FloorPlan(Path path, boolean testMode) {
    isTestmode = testMode;
    String matrix;
    try {
      matrix = Files.readString(path, Charset.defaultCharset());
    } catch (IOException e) {
      throw new IllegalStateException("IO Exception");
    }
    FloorField[][] fields = create(matrix);
    if (fields.length == 0) {
      throw new IllegalArgumentException("The matrix has wrong IDs or shape");
    }
    create(fields);
  }

  /**
   * Constructor to create a floor plan of a csv like string.
   *
   * @param matrix the csv like string
   */
  public FloorPlan(String matrix) {
    this(matrix, false);
  }

  /**
   * Constructor only for Tests.
   *
   * @param matrix the csv like string
   * @param testMode is in testmode
   */
  public FloorPlan(String matrix, boolean testMode) {
    isTestmode = testMode;
    FloorField[][] fields = create(matrix);
    if (fields.length == 0) {
      throw new IllegalArgumentException("The matrix has wrong IDs or shape");
    }
    create(fields);
  }

  @SuppressWarnings("java:S3776")
  private FloorField[][] create(String string) {
    String[] array = string.split("\\R");
    String[][] matrixWithoutStart = new String[array.length][];
    for (int i = 0; i < array.length; i++) {
      matrixWithoutStart[i] = array[i].split(";");
    }
    if (!check(matrixWithoutStart)) {
      return new FloorField[0][0];
    }
    String[][] matrix;
    if (!isTestmode) {
      matrix = addStartArea(matrixWithoutStart);
    } else {
      matrix = matrixWithoutStart;
    }
    if (matrix.length == 0) {
      return new FloorField[0][0];
    }
    FloorField edge = new FloorField(Direction.NORTH, Library.get("64"), -1, -1);
    FloorField[][] result = new FloorField[matrix.length + 2][matrix[0].length + 2];
    for (int i = 0; i < matrix.length + 2; i++) {
      for (int j = 0; j < matrix[0].length + 2; j++) {
        if (i == 0 || j == 0 || i == matrix.length + 1 || j == matrix[0].length + 1) {
          result[i][j] = edge;
        } else {
          Direction direction =
              Direction.get(Integer.parseInt(matrix[i - 1][j - 1].substring(0, 1)));
          BasicFloorField floorField = Library.get(matrix[i - 1][j - 1].substring(1, 3));
          if (direction == null || floorField == null) {
            return new FloorField[0][0];
          }
          boolean isCheckpoint = false;
          if (matrix[i - 1][j - 1].length() > 3) {
            int c = Integer.parseInt(matrix[i - 1][j - 1].substring(3, 4));
            if (c < 1 || c > 6) {
              throw new IllegalArgumentException("Amount of checkpoints not in range.");
            }
            floorField = floorField.makeCheckpoint(c);
            isCheckpoint = true;
          }
          FloorField field = new FloorField(direction, floorField, j - 1, i - 1);
          result[i][j] = field;

          if (isCheckpoint) {
            if (validIds.contains(Integer.parseInt(field.getBasicFloorField().getId()))) {
              checkpoints.put(
                  ((Checkpoint) field.getBasicFloorField().getOperations().get(7)).getNumber(),
                  field);
            } else {
              throw new IllegalArgumentException(
                  "Checkpoint on the field: '"
                      + field.getBasicFloorField().getId()
                      + "' has an invalid position.");
            }
          }
        }
      }
    }
    floorPlanAsString = HelperMethods.stringArrayToString(matrix);
    floorFields = result;
    return result;
  }

  @SuppressWarnings("java:S3776")
  private void create(FloorField[][] fields) {
    for (int i = 0; i < fields.length; i++) {
      for (int j = 0; j < fields[i].length; j++) {
        if (i == 0 || j == 0) {
          continue;
        }
        if (j < fields[0].length - 1
            && fields[i][j]
                .getBasicFloorField()
                .getPossibleNeighbours()
                .contains(
                    Direction.NORTH.rotate(
                        Direction.get((4 - fields[i][j].getDirection().getNumber()) % 4)))
            && fields[i - 1][j]
                .getBasicFloorField()
                .getPossibleNeighbours()
                .contains(
                    Direction.SOUTH.rotate(
                        Direction.get((4 - fields[i - 1][j].getDirection().getNumber()) % 4)))) {
          fields[i][j].setNorth(fields[i - 1][j]);
          fields[i - 1][j].setSouth(fields[i][j]);
        }
        if (i < fields.length - 1
            && fields[i][j]
                .getBasicFloorField()
                .getPossibleNeighbours()
                .contains(
                    Direction.WEST.rotate(
                        Direction.get((4 - fields[i][j].getDirection().getNumber()) % 4)))
            && fields[i][j - 1]
                .getBasicFloorField()
                .getPossibleNeighbours()
                .contains(
                    Direction.EAST.rotate(
                        Direction.get((4 - fields[i][j - 1].getDirection().getNumber()) % 4)))) {
          fields[i][j].setWest(fields[i][j - 1]);
          fields[i][j - 1].setEast(fields[i][j]);
        }
        String fieldId = fields[i][j].getBasicFloorField().getId();

        if ((fieldId.equals("96") && !isTestmode) || (fieldId.equals("97") && isTestmode)) {
          robotRoot.add(fields[i][j]);
        }
      }
    }
  }

  private boolean check(String[][] fields) {
    if (fields.length % FloorPlanSetting.FLOOR_PLAN_SIZE != 0
        || fields[0].length % FloorPlanSetting.FLOOR_PLAN_SIZE != 0) {
      return false;
    }
    for (String[] field : fields) {
      if (field.length != fields[0].length) {
        return false;
      }
    }
    return true;
  }

  @SuppressWarnings("java:S3776")
  private String[][] addStartArea(String[][] matrix) {
    String[] wallFieldArray = {
      "000", "000", "101", "000", "101", "000", "000", "101", "000", "101", "000", "000"
    };
    String[] startFieldSmallArray = {
      "196", "196", "000", "196", "000", "196", "196", "000", "196", "000", "196", "196"
    };
    String[] startFieldBigArray = {
      "196", "000", "196", "000", "196", "196", "196", "196", "000", "196", "000", "196"
    };

    int rows = matrix.length;
    int cols = matrix[0].length;
    String[][] copyMatrix = new String[rows][cols + 4];
    if (matrix.length / FloorPlanSetting.FLOOR_PLAN_SIZE == 1) {

      for (int i = 0; i < rows; i++) {
        if (wallFieldArray[i].startsWith("1")) {
          copyMatrix[i][0] = wallFieldArray[i].replaceFirst("1", "3");
        } else {
          copyMatrix[i][0] = wallFieldArray[i];
        }
        copyMatrix[i][1] = startFieldSmallArray[i];
        copyMatrix[i][2] = "000";
        copyMatrix[i][3] = wallFieldArray[i];
        System.arraycopy(matrix[i], 0, copyMatrix[i], 4, cols);
      }
      return copyMatrix;

    } else if (matrix.length / FloorPlanSetting.FLOOR_PLAN_SIZE == 2) {

      for (int i = 0; i < rows; i++) {
        int k;
        if (i < FloorPlanSetting.FLOOR_PLAN_SIZE) {
          k = i;
        } else {
          k = i - FloorPlanSetting.FLOOR_PLAN_SIZE;
        }
        if (wallFieldArray[k].startsWith("1")) {
          copyMatrix[i][0] = wallFieldArray[k].replaceFirst("1", "3");
        } else {
          copyMatrix[i][0] = wallFieldArray[k];
        }
        if (i <= 5 || i > (FloorPlanSetting.FLOOR_PLAN_SIZE + 5)) {
          copyMatrix[i][1] = "000";
        } else {
          copyMatrix[i][1] = startFieldBigArray[i - 6];
        }
        copyMatrix[i][2] = "000";
        copyMatrix[i][3] = wallFieldArray[k];
        System.arraycopy(matrix[i], 0, copyMatrix[i], 4, cols);
      }
      return copyMatrix;
    } else {
      return new String[0][0];
    }
  }

  public String getFloorPlanAsString() {
    return floorPlanAsString;
  }

  /**
   * Getter for the root fields for the robots.
   *
   * @return the root fields of the robots
   */
  public FloorField getStart() {
    if (!robotRoot.isEmpty()) {
      return robotRoot.remove();
    }
    return null;
  }

  /**
   * Getter for the checkpoints.
   *
   * @return the floor-fields with checkpoints
   */
  public Map<Integer, FloorField> getCheckpoints() {
    return checkpoints;
  }

  /**
   * Getter for the FloorField.
   *
   * @return the floor-fields in an array
   */
  public FloorField getFloorFields(int x, int y) {
    return floorFields[x][y];
  }
}
