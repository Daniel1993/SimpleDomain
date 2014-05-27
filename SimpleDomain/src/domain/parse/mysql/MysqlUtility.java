/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package domain.parse.mysql;

import domain.DomainManager;
import domain.MultipleObject;
import domain.DomainObject;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import simplemysql.SimpleMySQL;
import simplemysql.query.clause.CreateTableClause;
import simplemysql.query.QueryBuilder;

/**
 *
 * @author Daniel
 */
final class MysqlUtility {

  private MysqlUtility() {
  }

  private final static List<String> processedTables = new ArrayList<>();
  private static boolean rootObjectTableInit = false;

  private static boolean checkType(String javaType, String... test) {
    boolean check = false;
    for (String t : test) {
      check = check || javaType.equals(t);
    }
    return check;
  }

  /**
   * Converts a java type to a MySQL type.
   *
   * @param javaType
   * @return
   */
  static String toMysqlType(String javaType) {
    if (checkType(javaType, byte.class.getName(), Byte.class.getName())) {
      return MysqlStrings.BYTE_MySQL_TYPE;
    } else if (checkType(javaType, short.class.getName(),
            Short.class.getName())) {
      return MysqlStrings.SHORT_MySQL_TYPE;
    } else if (checkType(javaType, int.class.getName(),
            Integer.class.getName())) {
      return MysqlStrings.INT_MySQL_TYPE;
    } else if (checkType(javaType, long.class.getName(),
            Long.class.getName())) {
      return MysqlStrings.LONG_MySQL_TYPE;
    } else if (checkType(javaType, boolean.class.getName(),
            Boolean.class.getName())) {
      return MysqlStrings.BOOLEAN_MySQL_TYPE;
    } else if (checkType(javaType, char.class.getName(),
            Character.class.getName())) {
      return MysqlStrings.CHAR_MySQL_TYPE;
    } else if (checkType(javaType, float.class.getName(),
            Float.class.getName())) {
      return MysqlStrings.FLOAT_MySQL_TYPE;
    } else if (checkType(javaType, double.class.getName(),
            Double.class.getName())) {
      return MysqlStrings.DOUBLE_MySQL_TYPE;
    } else if (checkType(javaType, String.class.getName())) {
      return MysqlStrings.STRING_MySQL_TYPE;
    } else {
      return "";
    }
    // TODO: DATE, DATETIME & TIMESTAMP
  }

  static Object valueOf(String type, String value) {
    if (value == null || value.equals("") || value.equals(
            MysqlStrings.NULL_VALUE)) {
      return null;
    }
    if (checkType(type, byte.class.getName(), Byte.class.getName())) {
      return Byte.parseByte(value);
    } else if (checkType(type, short.class.getName(),
            Short.class.getName())) {
      return Short.parseShort(value);
    } else if (checkType(type, int.class.getName(),
            Integer.class.getName())) {
      return Integer.parseInt(value);
    } else if (checkType(type, long.class.getName(),
            Long.class.getName())) {
      return Long.parseLong(value);
    } else if (checkType(type, boolean.class.getName(),
            Boolean.class.getName())) {
      return Boolean.parseBoolean(value);
    } else if (checkType(type, char.class.getName(),
            Character.class.getName())) {
      return value.charAt(0);
    } else if (checkType(type, float.class.getName(),
            Float.class.getName())) {
      return Float.parseFloat(value);
    } else if (checkType(type, double.class.getName(),
            Double.class.getName())) {
      return Double.parseDouble(value);
    } else if (checkType(type, String.class.getName())) {
      return value;
    } else {
      return "";
    }
    // TODO: DATE, DATETIME & TIMESTAMP
  }
  
  static Object valueOf(Class type, String value) {
    return valueOf(type.getName(), value);
  }

  /**
   *
   * @param obj
   * @return
   */
  static String toMysqlValue(Object obj) {
    if (obj == null) {
      return "NULL";
    } else if (obj instanceof String) {
      return ((String) obj).replaceAll("['\"\\\\]", "\\\\$0");
    } else if (obj instanceof DomainObject) {
      return String.valueOf(((DomainObject) obj).getOID());
    } else {
      return obj.toString();
    }
  }

  private static void createObjectTable(SimpleMySQL mysql, String tableName) {
    QueryBuilder query = new QueryBuilder();
    CreateTableClause table = new CreateTableClause();
    table.addColumn(MysqlStrings.OID, MysqlStrings.OID_MySQL_TYPE);
    table.addPrimaryKey(MysqlStrings.OID);
    query.CREATE_TABLE(true, tableName, table);
    Logger.getLogger(DomainManager.class.getName()).log(Level.INFO,
            query.toString());
    mysql.Query(query.toString());
  }

  private static void createMultipleObjectTable(SimpleMySQL mysql,
          String tableName) {
    QueryBuilder query = new QueryBuilder();
    CreateTableClause table = new CreateTableClause();
    table.addColumn(MysqlStrings.OID, MysqlStrings.OID_MySQL_TYPE);
    table.addColumn(MysqlStrings.MULTIPLE_OID, MysqlStrings.OID_MySQL_TYPE);
    table.addPrimaryKey(MysqlStrings.MULTIPLE_OID, MysqlStrings.OID);
    query.CREATE_TABLE(true, tableName, table);
    Logger.getLogger(DomainManager.class.getName()).log(Level.INFO,
            query.toString());
    mysql.Query(query.toString());
  }

  private static void addColumnThenModify(SimpleMySQL mysql, String tableName,
          String columnName, String columnType) {
    // tries to add the column
    QueryBuilder query = new QueryBuilder();
    query.ALTER_TABLE_ADD(true, tableName, columnName, columnType);
    Logger.getLogger(DomainManager.class.getName()).log(Level.INFO,
            query.toString());
    mysql.Query(query.toString());

    // tries to modify the column
    query = new QueryBuilder();
    query.ALTER_TABLE_MODIFY(true, tableName, columnName, columnType);
    Logger.getLogger(DomainManager.class.getName()).log(Level.INFO,
            query.toString());
    mysql.Query(query.toString());
  }

  static void setObjectTable(SimpleMySQL mysql, Class objType) {
    String tableName = objType.getName();
    if (!processedTables.contains(tableName)) {
      createObjectTable(mysql, tableName);
      for (Field f : objType.getDeclaredFields()) {
        addColumnThenModify(mysql, tableName, f.getName(),
                toMysqlType(f.getType().getName()));
      }
      processedTables.add(tableName);
    }
  }

  static void setMultipleObjectTable(SimpleMySQL mysql) {
    String tableName = MultipleObject.class.getName();
    if (!processedTables.contains(tableName)) {
      createMultipleObjectTable(mysql, tableName);
      processedTables.add(tableName);
    }
  }

  static void createRootTable(SimpleMySQL mysql) {
    if (!rootObjectTableInit) {
      QueryBuilder query = new QueryBuilder();
      CreateTableClause table = new CreateTableClause();
      table.addColumn(MysqlStrings.OID, MysqlStrings.OID_MySQL_TYPE);
      table.addColumn(MysqlStrings.TABLE_NAME,
              MysqlStrings.STRING_MySQL_TYPE);
      table.addPrimaryKey(MysqlStrings.OID);
      query.CREATE_TABLE(true, MysqlStrings.ROOT_TABLE_NAME, table);
      Logger.getLogger(DomainManager.class.getName()).log(Level.INFO,
              query.toString());
      mysql.Query(query.toString());
      rootObjectTableInit = true;
    }
  }
}
