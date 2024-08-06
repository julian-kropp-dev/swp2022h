package de.uol.swp.common.helper;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import de.uol.swp.common.game.floor.Direction;
import de.uol.swp.common.game.floor.FloorPlanSetting;
import de.uol.swp.common.game.floor.FloorPlanSetting.FloorPlans;
import java.awt.Point;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class HelperMethodsTest {

  @Test
  void convertFloorPlanNormal() {
    Point c1 = new Point(1, 2);

    FloorPlanSetting[] floorPlanSettings = new FloorPlanSetting[4];
    floorPlanSettings[0] = new FloorPlanSetting(FloorPlans.CROSS);
    floorPlanSettings[1] = new FloorPlanSetting(FloorPlans.EMPTY);
    floorPlanSettings[2] = new FloorPlanSetting(FloorPlans.EMPTY);
    floorPlanSettings[3] = new FloorPlanSetting(FloorPlans.EMPTY);

    EnumMap<FloorPlans, Point> enumMapC1 = new EnumMap<>(FloorPlans.class);
    enumMapC1.put(FloorPlans.CROSS, c1);

    Map<Integer, EnumMap<FloorPlans, Point>> checkpoints = new HashMap<>();
    checkpoints.put(1, enumMapC1);
    String expectedFloorPlan =
        "064;217;267;000;001;214;014;001;000;001;000;000\n"
            + "314;213;0001;000;301;214;014;071;202;000;312;314\n"
            + "301;064;301;248;101;214;014;271;000;000;214;101\n"
            + "000;000;000;053;301;214;313;026;268;000;214;000\n"
            + "301;000;000;002;312;213;071;313;022;148;293;155\n"
            + "314;314;314;314;213;371;070;171;313;314;316;314\n"
            + "114;116;114;114;114;113;271;013;114;114;114;114\n"
            + "202;014;000;248;002;214;013;112;302;001;000;101\n"
            + "000;014;001;255;000;214;014;000;149;154;154;301\n"
            + "301;014;000;064;000;214;014;301;000;064;102;101\n"
            + "114;112;000;000;301;214;014;000;301;000;013;114\n"
            + "000;000;201;000;201;214;014;201;000;201;014;266";
    expectedFloorPlan = expectedFloorPlan.replaceAll("\\r\\n", "\n");
    String floorPlan =
        HelperMethods.convertFloorPlan(floorPlanSettings, checkpoints).replaceAll("\\r\\n", "\n");

    assertEquals(expectedFloorPlan, floorPlan);
  }

  @Test
  void convertFloorPlanTransformed() {
    Point c1 = new Point(2, 1);
    Point c2 = new Point(7, 4);

    FloorPlanSetting[] floorPlanSettings = new FloorPlanSetting[4];
    floorPlanSettings[0] = new FloorPlanSetting(FloorPlans.CROSS);
    floorPlanSettings[1] = new FloorPlanSetting(FloorPlans.EMPTY);
    floorPlanSettings[2] = new FloorPlanSetting(FloorPlans.ISLAND);
    floorPlanSettings[3] = new FloorPlanSetting(FloorPlans.EMPTY);

    EnumMap<FloorPlans, Point> enumMapC1 = new EnumMap<>(FloorPlans.class);
    EnumMap<FloorPlans, Point> enumMapC2 = new EnumMap<>(FloorPlans.class);
    enumMapC1.put(FloorPlans.CROSS, c1);
    enumMapC2.put(FloorPlans.ISLAND, c2);

    Map<Integer, EnumMap<FloorPlans, Point>> checkpoints = new HashMap<>();
    checkpoints.put(1, enumMapC1);
    checkpoints.put(2, enumMapC2);
    String expectedFloorPlan =
        "300;214;001;300;055;214;014;001;201;001;014;166;300;300;001;300;001;300;300;001;300;001;300;300\n"
            + "300;212;114;114;193;216;014;300;054;002;313;314;300;173;171;300;300;300;300;300;300;371;273;300\n"
            + "301;300;300;300;048;214;014;301;054;364;300;101;301;271;014;119;114;114;117;114;114;114;271;367\n"
            + "300;102;300;168;322;213;014;202;049;300;201;300;300;300;014;137;314;314;314;319;137;219;300;300\n"
            + "301;371;171;326;213;071;313;012;300;201;300;101;301;300;014;219;300;300;371;273;014;214;300;101\n"
            + "314;314;314;213;371;370;171;313;314;314;314;314;300;300;017;215;314;319;300;271;014;214;300;300\n"
            + "114;114;114;114;113;271;013;114;114;114;114;114;300;300;014;214;071;368;119;114;015;217;300;300\n"
            + "301;201;001;201;212;113;014;302;300;300;201;101;301;300;014;214;073;171;300;3002;019;214;300;101\n"
            + "300;300;148;353;302;214;014;148;155;364;300;300;300;300;019;137;119;114;114;114;137;214;300;300\n"
            + "167;300;201;300;300;214;014;300;301;300;300;101;301;071;138;314;314;314;317;314;319;138;071;101\n"
            + "117;113;3641;300;300;214;016;314;314;314;012;300;300;073;171;300;300;300;300;300;300;371;373;300\n"
            + "364;214;201;300;201;214;014;102;300;201;014;300;366;300;201;300;201;300;300;201;300;201;300;300\n";
    expectedFloorPlan = expectedFloorPlan.replaceAll("\\r\\n", "\n");
    String floorPlan =
        HelperMethods.convertFloorPlan(floorPlanSettings, checkpoints).replaceAll("\\r\\n", "\n");

    assertEquals(expectedFloorPlan, floorPlan);
    //
    //
    FloorPlanSetting[] floorPlanSettings2 = new FloorPlanSetting[4];
    floorPlanSettings2[0] = new FloorPlanSetting(FloorPlans.EMPTY);
    floorPlanSettings2[1] = new FloorPlanSetting(FloorPlans.CROSS);
    floorPlanSettings2[2] = new FloorPlanSetting(FloorPlans.EMPTY);
    floorPlanSettings2[3] = new FloorPlanSetting(FloorPlans.ISLAND);

    String floorPlan2 =
        HelperMethods.convertFloorPlan(floorPlanSettings2, checkpoints).replaceAll("\\r\\n", "\n");

    assertEquals(expectedFloorPlan, floorPlan2);
  }

  @Test
  void csvToIntArray() {
    String floorPlanCsv = "064;217;267\n" + "114;112;000";
    int[][] expectedIntArrayIds = {{64, 17, 67}, {14, 12, 0}};
    assertArrayEquals(expectedIntArrayIds, HelperMethods.csvToIntArrayId(floorPlanCsv));
    int[][] expectedIntArrayRot = {{0, 2, 2}, {1, 1, 0}};
    assertArrayEquals(expectedIntArrayRot, HelperMethods.csvToIntArrayRotation(floorPlanCsv));
  }

  @Test
  void buildCsvFrom2dArray() {
    String floorPlanCsv00 = "00a;00b;00c\n" + "00d;00e;00f";
    String floorPlanCsv01 = "01a;01b;01c\n" + "01d;01e;01f";
    String floorPlanCsv10 = "10a;10b;10c\n" + "10d;10e;10f";
    String floorPlanCsv11 = "11a;11b;11c\n" + "11d;11e;11f";
    //
    assertEquals(
        "", HelperMethods.buildCsvFrom2dArray(new String[][] {{null, null}, {null, null}}));
    //
    assertEquals(
        "00a;00b;00c\n" + "00d;00e;00f",
        HelperMethods.buildCsvFrom2dArray(new String[][] {{floorPlanCsv00, null}, {null, null}}));
    assertEquals(
        "01a;01b;01c\n" + "01d;01e;01f",
        HelperMethods.buildCsvFrom2dArray(new String[][] {{null, floorPlanCsv01}, {null, null}}));
    assertEquals(
        "10a;10b;10c\n" + "10d;10e;10f",
        HelperMethods.buildCsvFrom2dArray(new String[][] {{null, null}, {floorPlanCsv10, null}}));
    assertEquals(
        "11a;11b;11c\n" + "11d;11e;11f",
        HelperMethods.buildCsvFrom2dArray(new String[][] {{null, null}, {null, floorPlanCsv11}}));
    //
    assertEquals(
        "00a;00b;00c;01a;01b;01c"
            + System.lineSeparator()
            + "00d;00e;00f;01d;01e;01f"
            + System.lineSeparator(), // last \n inconsistent !!
        HelperMethods.buildCsvFrom2dArray(
            new String[][] {{floorPlanCsv00, floorPlanCsv01}, {null, null}}));
    assertEquals(
        "10a;10b;10c;11a;11b;11c"
            + System.lineSeparator()
            + "10d;10e;10f;11d;11e;11f"
            + System.lineSeparator(), // last \n inconsistent !!
        HelperMethods.buildCsvFrom2dArray(
            new String[][] {{null, null}, {floorPlanCsv10, floorPlanCsv11}}));
    assertEquals(
        "00a;00b;00c\n" + "00d;00e;00f" + System.lineSeparator() + "10a;10b;10c\n" + "10d;10e;10f",
        HelperMethods.buildCsvFrom2dArray(
            new String[][] {{floorPlanCsv00, null}, {floorPlanCsv10, null}}));
    assertEquals(
        "01a;01b;01c\n" + "01d;01e;01f" + System.lineSeparator() + "11a;11b;11c\n" + "11d;11e;11f",
        HelperMethods.buildCsvFrom2dArray(
            new String[][] {{null, floorPlanCsv01}, {null, floorPlanCsv11}}));
    //
    assertEquals(
        "00a;00b;00c;01a;01b;01c"
            + System.lineSeparator()
            + "00d;00e;00f;01d;01e;01f"
            + System.lineSeparator()
            + "10a;10b;10c;11a;11b;11c"
            + System.lineSeparator()
            + "10d;10e;10f;11d;11e;11f"
            + System.lineSeparator(), // last \n inconsistent !!
        HelperMethods.buildCsvFrom2dArray(
            new String[][] {{floorPlanCsv00, floorPlanCsv01}, {floorPlanCsv10, floorPlanCsv11}}));
  }

  @SuppressWarnings("UnnecessaryLocalVariable")
  @Test
  void rotateCsv() {
    String floorPlanCsv = "064;217;267\n" + "114;112;000";
    String expectedCsvNorth = floorPlanCsv;
    String expectedCsvEast =
        "214;164"
            + System.lineSeparator()
            + "212;317"
            + System.lineSeparator()
            + "100;367"
            + System.lineSeparator(); // last \n inconsistent !!
    String expectedCsvSouth = "200;312;314" + System.lineSeparator() + "067;017;264";
    String expectedCsvWest =
        "167;300"
            + System.lineSeparator()
            + "117;012"
            + System.lineSeparator()
            + "364;014"
            + System.lineSeparator(); // last \n inconsistent !!
    assertEquals(expectedCsvNorth, HelperMethods.rotateCsv(floorPlanCsv, Direction.NORTH));
    assertEquals(expectedCsvEast, HelperMethods.rotateCsv(floorPlanCsv, Direction.EAST));
    assertEquals(expectedCsvSouth, HelperMethods.rotateCsv(floorPlanCsv, Direction.SOUTH));
    assertEquals(expectedCsvWest, HelperMethods.rotateCsv(floorPlanCsv, Direction.WEST));
  }

  @Test
  void clampValue() {
    assertEquals(0, HelperMethods.clampValue(0));
    assertEquals(1, HelperMethods.clampValue(1));
    assertEquals(2, HelperMethods.clampValue(2));
    assertEquals(3, HelperMethods.clampValue(3));
    //
    assertEquals(0, HelperMethods.clampValue(-4));
    assertEquals(1, HelperMethods.clampValue(-3));
    assertEquals(2, HelperMethods.clampValue(-2));
    assertEquals(3, HelperMethods.clampValue(-1));
    //
    assertEquals(0, HelperMethods.clampValue(4));
    assertEquals(1, HelperMethods.clampValue(5));
    assertEquals(2, HelperMethods.clampValue(6));
    assertEquals(3, HelperMethods.clampValue(7));
  }
}
