package de.uol.swp.server.usermanagement;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.sql.DataSource;
import org.dbunit.DataSourceBasedDBTestCase;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DataSourceDBUnitTest extends DataSourceBasedDBTestCase {

  private static final List<String> IGNORE_COLUM = List.of("USERID");
  private static int test2UUID = 0;

  @Override
  protected DataSource getDataSource() {
    JdbcDataSource dataSource = new JdbcDataSource();
    dataSource.setURL(
        "jdbc:h2:mem:default;DB_CLOSE_DELAY=-1;init=runscript from 'classpath:sqlTest/schema.sql'");
    dataSource.setUser("test1");
    dataSource.setPassword("test1");
    return dataSource;
  }

  @Override
  protected IDataSet getDataSet() throws Exception {
    return new FlatXmlDataSetBuilder()
        .build(getClass().getClassLoader().getResourceAsStream("sqlTest/data.xml"));
  }

  @Override
  protected DatabaseOperation getSetUpOperation() {
    return DatabaseOperation.NONE;
  }

  @Override
  protected DatabaseOperation getTearDownOperation() {
    return DatabaseOperation.NONE;
  }

  @BeforeEach
  void setUP() throws Exception {
    Connection conn = getDataSource().getConnection();
    conn.createStatement().execute("DELETE FROM CLIENTS");
    conn.createStatement()
        .execute(
            "INSERT INTO CLIENTS (Username, Password, Mail, AvatarID) VALUES ('Test0', 'test0', 'test0@swp2022h.de', '1')");
    conn.createStatement()
        .execute(
            "INSERT INTO CLIENTS (Username, Password, Mail, AvatarID) VALUES ('Test1', 'test1', 'test1@swp2022h.de', '1')");
    conn.createStatement()
        .execute(
            "INSERT INTO CLIENTS (Username, Password, Mail, AvatarID) VALUES ('Test2', 'test2', 'test2@swp2022h.de', '1')");
    PreparedStatement stmt =
        conn.prepareStatement("SELECT UserID FROM CLIENTS WHERE Username = 'Test2'");
    ResultSet rs = stmt.executeQuery();
    if (rs.next()) {
      test2UUID = rs.getInt("UserID");
    }
    conn.createStatement().execute("DELETE FROM STATISTIC");
    conn.createStatement()
        .execute(
            "INSERT INTO STATISTIC (UserID, placement, DateTime) VALUES ("
                + test2UUID
                + ", '1', TODAY)");
  }

  @AfterEach
  void stop() throws Exception {}

  @Test
  void setupTest() throws Exception {
    IDataSet expectedDataSet = getDataSet();
    ITable expectedTable = expectedDataSet.getTable("CLIENTS");
    IDataSet databaseDataSet = getConnection().createDataSet();
    ITable actualTable = databaseDataSet.getTable("CLIENTS");
    assertEquals(expectedTable.getRowCount(), actualTable.getRowCount());
    assertTrue(compare(expectedTable, actualTable, true, IGNORE_COLUM));
  }

  @Test
  void insertUser() throws Exception {
    try (InputStream is =
        getClass().getClassLoader().getResourceAsStream("sqlTest/expected-user.xml")) {
      IDataSet expectedDataSet = new FlatXmlDataSetBuilder().build(is);
      ITable expectedTable = expectedDataSet.getTable("CLIENTS");
      Connection conn = getDataSource().getConnection();

      conn.createStatement()
          .executeUpdate(
              "INSERT INTO CLIENTS (Username, Password, Mail, AvatarId) VALUES ('Test3','test3','test3@swp2022h.de','1')");
      ITable actualData = getConnection().createQueryTable("result_name", "SELECT * FROM CLIENTS");
      assertTrue(compare(expectedTable, actualData, false, IGNORE_COLUM));
    }
  }

  @Test
  void updateUser() throws Exception {
    try (InputStream is =
        getClass().getClassLoader().getResourceAsStream("sqlTest/update-user.xml")) {
      IDataSet expectedDataSet = new FlatXmlDataSetBuilder().build(is);
      ITable expectedTable = expectedDataSet.getTable("CLIENTS");
      Connection conn = getDataSource().getConnection();

      conn.createStatement()
          .executeUpdate(
              "UPDATE CLIENTS SET Username='Test3', Password='test3', Mail='test3@swp2022h.de', AvatarId='1' WHERE Username='Test1'");
      ITable actualData = getConnection().createQueryTable("result_name", "SELECT * FROM CLIENTS");
      assertTrue(compare(expectedTable, actualData, false, IGNORE_COLUM));
    }
  }

  @Test
  void updateUsername() throws Exception {
    try (InputStream is =
        getClass().getClassLoader().getResourceAsStream("sqlTest/update-username.xml")) {
      IDataSet expectedDataSet = new FlatXmlDataSetBuilder().build(is);
      ITable expectedTable = expectedDataSet.getTable("CLIENTS");
      Connection conn = getDataSource().getConnection();

      conn.createStatement()
          .executeUpdate("UPDATE CLIENTS SET Username = 'Manuel' WHERE Username = 'Test1'");
      ITable actualData = getConnection().createQueryTable("result_name", "SELECT * FROM CLIENTS");
      assertTrue(compare(expectedTable, actualData, false, IGNORE_COLUM));
    }
  }

  @Test
  void setAvatarID() throws Exception {
    try (InputStream is =
        getClass().getClassLoader().getResourceAsStream("sqlTest/update-avatar.xml")) {
      IDataSet expectedDataSet = new FlatXmlDataSetBuilder().build(is);
      ITable expectedTable = expectedDataSet.getTable("CLIENTS");
      Connection conn = getDataSource().getConnection();

      conn.createStatement()
          .executeUpdate("UPDATE CLIENTS SET AvatarID = 5 WHERE Username = 'Test2'");
      ITable actualData = getConnection().createQueryTable("result_name", "SELECT * FROM CLIENTS");
      assertTrue(compare(expectedTable, actualData, false, IGNORE_COLUM));
    }
  }

  @Test
  void findUser() throws Exception {
    try (InputStream is =
        getClass().getClassLoader().getResourceAsStream("sqlTest/find-user.xml")) {
      IDataSet expectedDataSet = new FlatXmlDataSetBuilder().build(is);
      ITable expectedTable = expectedDataSet.getTable("CLIENTS");

      ITable actualData =
          getConnection()
              .createQueryTable(
                  "result_name",
                  "SELECT UserID, Username, Password, Mail, AvatarId FROM CLIENTS WHERE Username = 'Test1' AND Password = 'test1'");
      assertTrue(compare(expectedTable, actualData, false, IGNORE_COLUM));
    }
  }

  @Test
  void findUserUUID() throws Exception {
    try (InputStream is =
        getClass().getClassLoader().getResourceAsStream("sqlTest/find-user-uuid.xml")) {
      IDataSet expectedDataSet = new FlatXmlDataSetBuilder().build(is);
      ITable expectedTable = expectedDataSet.getTable("CLIENTS");

      ITable actualData =
          getConnection()
              .createQueryTable(
                  "result_name",
                  "SELECT UserID, Username, Password, Mail, AvatarId FROM CLIENTS WHERE UserID = "
                      + test2UUID);
      assertTrue(compare(expectedTable, actualData, false, IGNORE_COLUM));
    }
  }

  @Test
  void findUUID() throws Exception {
    try (InputStream is =
        getClass().getClassLoader().getResourceAsStream("sqlTest/find-uuid.xml")) {
      IDataSet expectedDataSet = new FlatXmlDataSetBuilder().build(is);
      ITable expectedTable = expectedDataSet.getTable("CLIENTS");

      ITable actualData =
          getConnection()
              .createQueryTable(
                  "result_name", "SELECT UserID, Username FROM CLIENTS WHERE Username = 'Test2'");
      assertTrue(compare(expectedTable, actualData, false, IGNORE_COLUM));
    }
  }

  @Test
  void getAllUser() throws Exception {
    try (InputStream is =
        getClass().getClassLoader().getResourceAsStream("sqlTest/get-all-users.xml")) {
      IDataSet expectedDataSet = new FlatXmlDataSetBuilder().build(is);
      ITable expectedTable = expectedDataSet.getTable("CLIENTS");

      ITable actualData =
          getConnection()
              .createQueryTable(
                  "result_name", "SELECT UserID, Username, Mail, AvatarId FROM CLIENTS");
      assertTrue(compare(expectedTable, actualData, false, IGNORE_COLUM));
    }
  }

  @Test
  void getUserData() throws Exception {
    try (InputStream is =
        getClass().getClassLoader().getResourceAsStream("sqlTest/get-user-data.xml")) {
      IDataSet expectedDataSet = new FlatXmlDataSetBuilder().build(is);
      ITable expectedTable = expectedDataSet.getTable("STATISTIC");

      ITable actualData =
          getConnection()
              .createQueryTable(
                  "result_name",
                  "SELECT placement, AvatarId From CLIENTS LEFT JOIN STATISTIC S on CLIENTS.UserID = S.UserID WHERE CLIENTS.UserID = "
                      + test2UUID);
      assertTrue(compare(expectedTable, actualData, false, IGNORE_COLUM));
    }
  }

  @Test
  void removeUser() throws Exception {
    try (InputStream is =
        getClass().getClassLoader().getResourceAsStream("sqlTest/remove-user.xml")) {
      IDataSet expectedDataSet = new FlatXmlDataSetBuilder().build(is);
      ITable expectedTable = expectedDataSet.getTable("CLIENTS");
      Connection conn = getDataSource().getConnection();

      conn.createStatement().executeUpdate("DELETE FROM CLIENTS WHERE Username = 'Test2'");
      ITable actualData = getConnection().createQueryTable("result_name", "SELECT * FROM CLIENTS");
      assertTrue(compare(expectedTable, actualData, false, IGNORE_COLUM));
    }
  }

  @Test
  void removeUserStatistic() throws Exception {
    Connection conn = getDataSource().getConnection();
    conn.createStatement()
        .executeUpdate(
            "DELETE FROM STATISTIC WHERE UserID = (SELECT UserID FROM CLIENTS WHERE UserID = "
                + test2UUID
                + ")");
    ITable actualData = getConnection().createQueryTable("result_name", "SELECT * FROM STATISTIC");
    assertEquals(actualData.getRowCount(), 0);
  }

  /**
   * Method in order to compare two ITables.
   *
   * @param expected Expected Table
   * @param actual Actual Table
   * @param orderMatters Actual Table ca be a subset of the expected table
   * @return true if the table are equal
   */
  private boolean compare(
      ITable expected, ITable actual, boolean orderMatters, List<String> ignoreColumns) {
    List<String> expectedColumns = new ArrayList<>();
    List<String> actualColumns = new ArrayList<>();
    List<String[]> expectedRows = new ArrayList<>();
    List<String[]> actualRows = new ArrayList<>();
    if (expected.getRowCount() != actual.getRowCount() && orderMatters) {
      return false;
    }
    try {
      for (Column column : expected.getTableMetaData().getColumns()) {
        expectedColumns.add(column.getColumnName());
      }
    } catch (DataSetException e) {
      throw new RuntimeException(e);
    }
    try {
      for (Column column : actual.getTableMetaData().getColumns()) {
        actualColumns.add(column.getColumnName());
      }
    } catch (DataSetException e) {
      throw new RuntimeException(e);
    }
    expectedColumns.removeAll(ignoreColumns);
    actualColumns.removeAll(ignoreColumns);
    if (!expectedColumns.equals(actualColumns)) {
      return false;
    }
    for (int i = 0; i < expected.getRowCount(); i++) {
      String[] row = new String[expectedColumns.size()];
      for (int j = 0; j < expectedColumns.size(); j++) {
        try {
          row[j] = expected.getValue(i, expectedColumns.get(j)).toString();
        } catch (DataSetException e) {
          throw new RuntimeException(e);
        }
      }
      expectedRows.add(row);
    }
    for (int i = 0; i < actual.getRowCount(); i++) {
      String[] row = new String[actualColumns.size()];
      for (int j = 0; j < actualColumns.size(); j++) {
        try {
          row[j] = actual.getValue(i, expectedColumns.get(j)).toString();
          // if(expectedColumns.get(j)).toString().equals())

        } catch (DataSetException e) {
          throw new RuntimeException(e);
        }
      }
      actualRows.add(row);
    }
    if (orderMatters) {
      for (int i = 0; i < expectedRows.size(); i++) {
        if (!Arrays.equals(expectedRows.get(i), actualRows.get(i))) {
          return false;
        }
      }
      return true;
    } else {
      for (String[] actualRow : actualRows) {
        boolean exists = false;
        for (String[] row : expectedRows) {
          if (Arrays.equals(actualRow, row)) {
            exists = true;
            break;
          }
        }
        if (exists) {
          return true;
        }
      }
      return false;
    }
  }
}
