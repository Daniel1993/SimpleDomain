/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package domain.mysql;

import domain.DomainObject;
import domain.MultipleObject;
import domain.Parser;
import domain.attr.Attr;
import domain.attr.AttrCheck;
import domain.attr.AttrUtility;
import domain.attr.Pair;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import simplemysql.SimpleMySQL;
import simplemysql.SimpleMySQLResult;
import simplemysql.query.clause.InsertClauseColumns;
import simplemysql.query.clause.InsertClauseValues;
import simplemysql.query.QueryBuilder;
import simplemysql.query.clause.WhereClause;

/**
 *
 * @author Daniel
 */
public final class MySQLParser implements Parser {

  private final static MySQLParser parser = new MySQLParser();
  private static long lastOid = 0;
  private final static Object monitor = new Object();

  public static MySQLParser getInstance() {
    return parser;
  }

  private final SimpleMySQL mysql = new SimpleMySQL();

  private MySQLParser() {
  }

  @Override
  public void parseAttrs(Stack<Pair<Class, List<Attr>>> attrs) {
    Pair<Class, List<Attr>> domainAttrs = attrs.pop();
    Utility.setObjectTable(mysql, domainAttrs.first);

    // gets the oid from the given attributes
    long oid = 0;
    for (Attr a : domainAttrs.second) {
      if (a.getName().equals(MySQLStrings.OID)) {
        oid = (long) a.getValue();
        break;
      }
    }

    // delete a previous value
    QueryBuilder deletePrevDomainObj = new QueryBuilder();
    WhereClause where = new WhereClause();
    where.compareWithValue(MySQLStrings.OID, "=", String.valueOf(oid));
    deletePrevDomainObj.
            DELETE(true).
            FROM(MySQLStrings.ROOT_TABLE_NAME).
            WHERE(where);
    mysql.Query(deletePrevDomainObj.toString());

    // insert the object as DomainObject
    QueryBuilder insertDomainObj = new QueryBuilder();
    InsertClauseColumns columns = new InsertClauseColumns();
    InsertClauseValues values = new InsertClauseValues();
    for (Attr a
            : domainAttrs.second) {
      columns.addColumns(a.getName());
      values.addValues(Utility.parseToMySQLValue(a.getValue()));
    }
    insertDomainObj.INSERT(domainAttrs.first.getName(), columns, values);
    mysql.Query(insertDomainObj.toString());

    // foreach super class parse his attributes
    for (Pair<Class, List<Attr>> p : attrs) {
      Utility.setObjectTable(mysql, p.first);

      // delete a previous value
      QueryBuilder deletePrevObj = new QueryBuilder();
      where = new WhereClause().compareWithColumn(MySQLStrings.OID, "=",
              String.valueOf(oid));
      deletePrevObj.
              DELETE(true).
              FROM(p.first.getName()).
              WHERE(where);
      mysql.Query(deletePrevObj.toString());

      // insert the object
      QueryBuilder insertObj = new QueryBuilder();
      columns = new InsertClauseColumns().addColumns(MySQLStrings.OID);
      values = new InsertClauseValues().addValues(String.valueOf(oid));
      for (Attr a : p.second) {
        columns.addColumns(a.getName());
        values.addValues(Utility.parseToMySQLValue(a.getValue()));
      }
      insertObj.INSERT(p.first.getName(), columns, values);
      mysql.Query(insertObj.toString());
    }
  }

  @Override
  public void setRoot(Stack<Class> type, long oid) {
    // tries to create the root table
    Utility.createRootTable(mysql);

    QueryBuilder findDomainObject = new QueryBuilder();
    WhereClause where = new WhereClause();
    where.compareWithValue(MySQLStrings.OID, "=", "" + oid);
    findDomainObject.SELECT(true).
            FROM(DomainObject.class.getName()).
            WHERE(where);

    // tries to find the domainObject to be root
    SimpleMySQLResult search;
    search = mysql.Query(findDomainObject.toString());
    if (search.getNumRows() != 0) {
      // object found insert in rootObject
      Map<String, String> fetch = search.FetchAssoc();

      QueryBuilder deletePrevRoot = new QueryBuilder();
      deletePrevRoot.DELETE(true).FROM(MySQLStrings.ROOT_TABLE_NAME);
      mysql.Query(deletePrevRoot.toString());

      QueryBuilder insertObject = new QueryBuilder();
      InsertClauseColumns columns = new InsertClauseColumns();
      InsertClauseValues values = new InsertClauseValues();

      columns.addColumns(MySQLStrings.OID, MySQLStrings.TABLE_NAME);
      values.addValues(fetch.get(MySQLStrings.OID),
              fetch.get(MySQLStrings.OBJECT_NAME));

      insertObject.INSERT(MySQLStrings.ROOT_TABLE_NAME, columns, values);
      mysql.Query(insertObject.toString());
    } else {
      // object not found
    }
  }

  @Override
  public DomainObject loadObj() {
    Utility.createRootTable(mysql);

    QueryBuilder findDomainObject = new QueryBuilder();
    findDomainObject.SELECT(true).
            FROM(MySQLStrings.ROOT_TABLE_NAME);
    SimpleMySQLResult search;
    search = mysql.Query(findDomainObject.toString());

    if (search.getNumRows() != 0) {
      // object found insert in rootObject
      Map<String, String> fetch = search.FetchAssoc();
      try {
        Class reqObj = Class.forName(fetch.get(MySQLStrings.TABLE_NAME));
        return loadObj(AttrUtility.objectChain(reqObj),
                Long.parseLong(fetch.get(MySQLStrings.OID)));
      } catch (ClassNotFoundException ex) {
        Logger.getLogger(MySQLParser.class.getName()).
                log(Level.SEVERE, null, ex);
      }
    } else {
      // object not found
    }

    return null;
  }

  @Override
  public DomainObject loadObj(Stack<Class> type, long oid) {
    Class domainObject = type.pop();
    Utility.setObjectTable(mysql, domainObject.getClass());
    QueryBuilder query = new QueryBuilder();
    WhereClause where = new WhereClause();
    where.compareWithValue(MySQLStrings.OID, "=",
            Utility.parseToMySQLValue(oid));
    query.SELECT(true).FROM(DomainObject.class.getName()).WHERE(where);
    SimpleMySQLResult mysqlObj;
    mysqlObj = mysql.Query(query.toString());
    if (mysqlObj.getNumRows() != 0) {
      Class lastType = type.get(type.size()-1);
      try {
        DomainObject retObj = (DomainObject) lastType.newInstance();
        retObj.setOID(oid);
        if (AttrCheck.isMultipleObject(lastType)) {
          return retObj;
        }
        for (Class c : type) {
          query = new QueryBuilder();
          query.SELECT(true).
                  FROM(c.getName()).
                  WHERE(where);
          mysqlObj = mysql.Query(query.toString());
          Map<String, String> fetchedObj = mysqlObj.FetchAssoc();
          fetchedObj.remove(MySQLStrings.OID);
          for (String k : fetchedObj.keySet()) {
            Field f = c.getDeclaredField(k);
            if (AttrCheck.isPrimitive(f.getType())) {
              f.setAccessible(true);
              f.set(retObj, Utility.valueOf(f.getType().getName(),
                      fetchedObj.get(k)));
            }
          }
        }
        return retObj;
      } catch (InstantiationException | IllegalAccessException |
              NoSuchFieldException | SecurityException ex) {
        Logger.getLogger(MySQLParser.class.getName()).
                log(Level.SEVERE, null, ex);
      }
    }
    return null;
  }

  /**
   *
   * @param args MySQL host, database, user and pass
   * @return
   */
  @Override
  public boolean initialize(String... args) {
    if (args.length != 4) {
      // cannot initialize
      return false;
    }
    // receives host, user, pass then database
    return mysql.connect(args[0], args[2], args[3], args[1]);
  }

  @Override
  public void transactionBegin() {
    mysql.transactionBegin();
  }

  @Override
  public void transactionCommit() {
    mysql.transactionCommit();
  }

  @Override
  public void transactionRollback() {
    mysql.transactionRollback();
  }

  @Override
  public boolean deleteObj(Stack<Class> type) {
    //To change body of generated methods, choose Tools | Templates.
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public boolean deleteObj(Stack<Class> type, long oid) {
    //To change body of generated methods, choose Tools | Templates.
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public long getOid() {
    long retOid;
    synchronized (monitor) {
      if (lastOid == 0) {
        lastOid = OidTableUtility.loadStateFromIDSTable(mysql);
        retOid = lastOid;
        lastOid += 1;
        OidTableUtility.setIDSTable(Utility.parseToMySQLValue(lastOid),
                mysql);
      } else {
        retOid = lastOid;
        lastOid += 1;
        OidTableUtility.setIDSTable(Utility.parseToMySQLValue(lastOid),
                mysql);
      }
    }
    return retOid;
  }

  @Override
  public boolean addObjToMultObj(long oid, long multObjOid) {
    Utility.setObjectTable(mysql, MultipleObject.class);
    
    return false;
  }

  @Override
  public boolean removeObjFromMultObj(long oid, long multObjOid) {
    //To change body of generated methods, choose Tools | Templates.
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public List<DomainObject> searchObjsInMultObj(String param, Object value, long multObjOid) {
    //To change body of generated methods, choose Tools | Templates.
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public List<DomainObject> getAllObjsInMultObj() {
    //To change body of generated methods, choose Tools | Templates.
    throw new UnsupportedOperationException("Not supported yet.");
  }
}
