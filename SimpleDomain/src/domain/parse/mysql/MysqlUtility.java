/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package domain.parse.mysql;

import domain.DomainManager;
import domain.MultipleObject;
import domain.DomainObject;
import domain.attr.AttrCheck;
import domain.attr.AttrUtility;
import static domain.parse.mysql.MysqlStrings.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import simplemysql.SimpleMySQL;
import simplemysql.SimpleMySQLResult;
import simplemysql.exception.ResultException;
import simplemysql.exception.SimpleMySQLException;
import simplemysql.query.clause.CreateTableClause;
import simplemysql.query.QueryBuilder;
import simplemysql.query.clause.WhereClause;

/**
 *
 * @author Daniel
 */
final class MysqlUtility {

  static final Map<Long, DomainObject> loadedObjs = new HashMap<>();

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

  static Stack<Class> getClassChain(SimpleMySQL mysql, long oid) {
    try {
      QueryBuilder query = new QueryBuilder();
      WhereClause where = new WhereClause();
      where.compareWithValue(OID, "=", "" + oid);
      query.SELECT(true, OBJECT_NAME);
      query.FROM(DomainObject.class.getName());
      query.WHERE(where);
      SimpleMySQLResult savedObj;
      savedObj = mysql.Query(query.toString());

      if (savedObj.getNumRows() == 0) {
        return null;
      }

      Map<String, String> fetchObjName = savedObj.FetchAssoc();
      Class objType = null;
      try {
        objType = Class.forName(fetchObjName.get(OBJECT_NAME));
      } catch (ClassNotFoundException ex) {
        Logger.getLogger(MysqlParser.class.getName()).
                log(Level.SEVERE, null, ex);
      }

      if (objType == null) {
        return null;
      }

      return AttrUtility.objectChain(objType);
    } catch (SimpleMySQLException | ResultException ex) {
      Logger.getLogger(MysqlUtility.class.getName()).log(Level.SEVERE, null, ex);
    }

    return null;
  }

  /**
   * Converts a java type to a MySQL type.
   *
   * @param javaType
   * @return
   */
  static String toMysqlType(Class type) {
    String javaType = type.getName();
    if (checkType(javaType, byte.class.getName(), Byte.class.getName())) {
      return BYTE_MySQL_TYPE;
    } else if (checkType(javaType, short.class.getName(),
            Short.class.getName())) {
      return SHORT_MySQL_TYPE;
    } else if (checkType(javaType, int.class.getName(),
            Integer.class.getName())) {
      return INT_MySQL_TYPE;
    } else if (checkType(javaType, long.class.getName(),
            Long.class.getName())) {
      return LONG_MySQL_TYPE;
    } else if (checkType(javaType, boolean.class.getName(),
            Boolean.class.getName())) {
      return BOOLEAN_MySQL_TYPE;
    } else if (checkType(javaType, char.class.getName(),
            Character.class.getName())) {
      return CHAR_MySQL_TYPE;
    } else if (checkType(javaType, float.class.getName(),
            Float.class.getName())) {
      return FLOAT_MySQL_TYPE;
    } else if (checkType(javaType, double.class.getName(),
            Double.class.getName())) {
      return DOUBLE_MySQL_TYPE;
    } else if (checkType(javaType, String.class.getName())) {
      return STRING_MySQL_TYPE;
    } else if (AttrCheck.isDomainObject(type)) {
      return OID_MySQL_TYPE;
    } else {
      return "";
    }
    // TODO: DATE, DATETIME & TIMESTAMP
  }

  static Object valueOf(String type, String value) {
    if (value == null || value.equals("") || value.equals(
            NULL_VALUE)) {
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
    try {
      QueryBuilder query = new QueryBuilder();
      CreateTableClause table = new CreateTableClause();
      table.addColumn(OID, OID_MySQL_TYPE);
      table.addPrimaryKey(OID);
      query.CREATE_TABLE(true, tableName, table);
      Logger.getLogger(DomainManager.class.getName()).log(Level.INFO,
              query.toString());
      mysql.Query(query.toString());
    } catch (SimpleMySQLException ex) {
      Logger.getLogger(MysqlUtility.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  private static void createMultipleObjectTable(SimpleMySQL mysql,
          String tableName) {
    try {
      QueryBuilder query = new QueryBuilder();
      CreateTableClause table = new CreateTableClause();
      table.addColumn(OID, OID_MySQL_TYPE);
      table.addColumn(MULTIPLE_OID, OID_MySQL_TYPE);
      table.addPrimaryKey(MULTIPLE_OID, OID);
      query.CREATE_TABLE(true, tableName, table);
      Logger.getLogger(DomainManager.class.getName()).log(Level.INFO,
              query.toString());
      mysql.Query(query.toString());
    } catch (SimpleMySQLException ex) {
      Logger.getLogger(MysqlUtility.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  private static void addColumnThenModify(SimpleMySQL mysql, String tableName,
          String columnName, String columnType) {
    try {
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
    } catch (SimpleMySQLException ex) {
      Logger.getLogger(MysqlUtility.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  static void setObjectTable(SimpleMySQL mysql, Class objType) {
    String tableName = objType.getName();
    if (!processedTables.contains(tableName)) {
      createObjectTable(mysql, tableName);
      for (Field f : objType.getDeclaredFields()) {
        addColumnThenModify(mysql, tableName, f.getName(),
                toMysqlType(f.getType()));
      }
      processedTables.add(tableName);
    }
  }

  static void setMultipleObjectTable(SimpleMySQL mysql) {
    String tableName = MULTIPLE_OBJECT;
    if (!processedTables.contains(tableName)) {
      createMultipleObjectTable(mysql, tableName);
      processedTables.add(tableName);
    }
  }

  static void createRootTable(SimpleMySQL mysql) {
    if (!rootObjectTableInit) {
      try {
        QueryBuilder query = new QueryBuilder();
        CreateTableClause table = new CreateTableClause();
        table.addColumn(OID, OID_MySQL_TYPE);
        table.addColumn(TABLE_NAME, STRING_MySQL_TYPE);
        table.addPrimaryKey(OID);
        query.CREATE_TABLE(true, ROOT_TABLE_NAME, table);
        Logger.getLogger(DomainManager.class.getName()).log(Level.INFO,
                query.toString());
        mysql.Query(query.toString());
        rootObjectTableInit = true;
      } catch (SimpleMySQLException ex) {
        Logger.getLogger(MysqlUtility.class.getName()).log(Level.SEVERE, null,
                ex);
      }
    }
  }
}
