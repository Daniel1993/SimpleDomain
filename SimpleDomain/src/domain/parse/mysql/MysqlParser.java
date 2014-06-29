/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package domain.parse.mysql;

import domain.DomainObject;
import domain.MultipleObject;
import domain.parse.Parser;
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
import static domain.parse.mysql.MysqlUtility.*;
import static domain.parse.mysql.MysqlStrings.*;
import java.util.ArrayList;
import simplemysql.exception.ResultException;
import simplemysql.exception.SimpleMySQLException;
import simplemysql.query.clause.FromClause;

/**
 *
 * @author Daniel
 */
public final class MysqlParser implements Parser {

  private final static MysqlParser parser = new MysqlParser();
  private static long lastOid = 0;
  private final static Object monitor = new Object();

  public static MysqlParser getInstance() {
    return parser;
  }

  private final SimpleMySQL mysql = new SimpleMySQL();

  private MysqlParser() {
  }

  @Override
  public void saveObj(Stack<Pair<Class, List<Attr>>> attrs) {
    try {
      Pair<Class, List<Attr>> domainAttrs = attrs.pop();
      setObjectTable(mysql, domainAttrs.first);

      // gets the oid from the given attributes
      long oid = 0;
      for (Attr a : domainAttrs.second) {
        if (a.getName().equals(OID)) {
          oid = (long) a.getValue();
          break;
        }
      }

      // delete a previous value
      QueryBuilder deletePrevDomainObj = new QueryBuilder();
      WhereClause where = new WhereClause();
      where.compareWithValue(OID, "=", String.valueOf(oid));
      deletePrevDomainObj.
              DELETE(true).
              FROM(domainAttrs.first.getName()).
              WHERE(where);
      mysql.Query(deletePrevDomainObj.toString());

      // insert the object as DomainObject
      QueryBuilder insertDomainObj = new QueryBuilder();
      InsertClauseColumns columns = new InsertClauseColumns();
      InsertClauseValues values = new InsertClauseValues();
      for (Attr a : domainAttrs.second) {
        columns.addColumns(a.getName());
        values.addValues(toMysqlValue(a.getValue()));
      }
      insertDomainObj.INSERT(domainAttrs.first.getName(), columns, values);
      mysql.Query(insertDomainObj.toString());

      // foreach super class parse his attributes
      for (Pair<Class, List<Attr>> p : attrs) {
        setObjectTable(mysql, p.first);

        // delete a previous value
        QueryBuilder deletePrevObj = new QueryBuilder();
        where = new WhereClause().compareWithColumn(OID, "=",
                String.valueOf(oid));
        deletePrevObj.
                DELETE(true).
                FROM(p.first.getName()).
                WHERE(where);
        mysql.Query(deletePrevObj.toString());

        // insert the object
        QueryBuilder insertObj = new QueryBuilder();
        columns = new InsertClauseColumns().addColumns(OID);
        values = new InsertClauseValues().addValues(String.valueOf(oid));
        for (Attr a : p.second) {
          columns.addColumns(a.getName());
          values.addValues(toMysqlValue(a.getValue()));
        }
        insertObj.INSERT(p.first.getName(), columns, values);
        mysql.Query(insertObj.toString());
      }
    } catch (SimpleMySQLException e) {
      Logger.getLogger(MysqlParser.class.getName()).log(Level.SEVERE, null, e);
    }
  }

  @Override
  public void setRoot(Stack<Class> type, long oid) {
    try {
      // tries to create the root table
      createRootTable(mysql);

      QueryBuilder findDomainObject = new QueryBuilder();
      WhereClause where = new WhereClause();
      where.compareWithValue(OID, "=", "" + oid);
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
        deletePrevRoot.DELETE(true).FROM(ROOT_TABLE_NAME);
        mysql.Query(deletePrevRoot.toString());

        QueryBuilder insertObject = new QueryBuilder();
        InsertClauseColumns columns = new InsertClauseColumns();
        InsertClauseValues values = new InsertClauseValues();

        columns.addColumns(OID, TABLE_NAME);
        values.addValues(fetch.get(OID),
                fetch.get(OBJECT_NAME));

        insertObject.INSERT(ROOT_TABLE_NAME, columns, values);
        mysql.Query(insertObject.toString());
      } else {
        // object not found
      }
    } catch (SimpleMySQLException | ResultException ex) {
      Logger.getLogger(MysqlParser.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  @Override
  public DomainObject loadObj() {
    try {
      createRootTable(mysql);

      QueryBuilder findDomainObject = new QueryBuilder();
      findDomainObject.SELECT(true).
              FROM(ROOT_TABLE_NAME);
      SimpleMySQLResult search;
      search = mysql.Query(findDomainObject.toString());

      if (search.getNumRows() != 0) {
        // object found insert in rootObject
        Map<String, String> fetch = search.FetchAssoc();
        return loadObj(Long.parseLong(fetch.get(OID)));
      } else {
        // object not found
      }

    } catch (SimpleMySQLException | ResultException ex) {
      Logger.getLogger(MysqlParser.class.getName()).log(Level.SEVERE, null, ex);
    }
    return null;
  }

  @Override
  public DomainObject loadObj(long oid) {
    try {
      Stack<Class> type = getClassChain(mysql, oid);

      Class domainObject = type.pop();
      setObjectTable(mysql, domainObject);
      QueryBuilder query = new QueryBuilder();
      WhereClause where = new WhereClause();
      where.compareWithValue(OID, "=", toMysqlValue(oid));
      query.SELECT(true).FROM(DomainObject.class.getName()).WHERE(where);
      SimpleMySQLResult mysqlObj;
      mysqlObj = mysql.Query(query.toString());
      if (mysqlObj.getNumRows() != 0) {
        Class lastType = type.get(type.size() - 1);
        try {
          DomainObject retObj = (DomainObject) lastType.newInstance();
          loadedObjs.put(oid, retObj);
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
            fetchedObj.remove(OID);
            for (String k : fetchedObj.keySet()) {
              Field f = c.getDeclaredField(k);
              f.setAccessible(true);
              if (AttrCheck.isPrimitive(f.getType())) {
                f.set(retObj, valueOf(f.getType().getName(), fetchedObj.get(k)));
              } else if (AttrCheck.isDomainObject(f.getType())) {
                if (loadedObjs.containsKey(oid)) {
                  // FIXME
                  f.set(retObj, loadedObjs.get(oid));
                } else {
                  f.set(retObj, loadObj(Long.valueOf(fetchedObj.get(k))));
                }
              }
            }
          }
          return retObj;
        } catch (InstantiationException | IllegalAccessException |
                NoSuchFieldException | SecurityException | ResultException ex) {
          Logger.getLogger(MysqlParser.class.getName()).
                  log(Level.SEVERE, null, ex);
        }
      }
    } catch (SimpleMySQLException ex) {
      Logger.getLogger(MysqlParser.class.getName()).log(Level.SEVERE, null, ex);
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
    try {
      if (args.length != 4) {
        // cannot initialize
        return false;
      }
      // receives host, user, pass then database
      return mysql.connect(args[0], args[2], args[3], args[1]);
    } catch (SimpleMySQLException ex) {
      Logger.getLogger(MysqlParser.class.getName()).log(Level.SEVERE, null, ex);
    }
    return false;
  }

  @Override
  public void transactionBegin() {
    mysql.transactionBegin();
  }

  @Override
  public void transactionCommit() {
    try {
      mysql.transactionCommit();
    } catch (SimpleMySQLException ex) {
      Logger.getLogger(MysqlParser.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  @Override
  public void transactionRollback() {
    try {
      mysql.transactionRollback();
    } catch (SimpleMySQLException ex) {
      Logger.getLogger(MysqlParser.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  @Override
  public boolean deleteObj() {
    // TODO: drop database; create database;
    //To change body of generated methods, choose Tools | Templates.
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public boolean deleteObj(Stack<Class> type, long oid) {
    WhereClause where = new WhereClause();
    where.compareWithColumn(OID, "=", "" + oid);
    QueryBuilder query = new QueryBuilder();
    for (Class c : type) {
      query.DELETE(true).FROM(c.getName()).WHERE(where);
    }
    return true;
  }

  @Override
  public long getOid() {
    long retOid;
    synchronized (monitor) {
      if (lastOid == 0) {
        lastOid = OidTableUtility.loadStateFromIDSTable(mysql);
        retOid = lastOid;
        lastOid += 1;
        OidTableUtility.setIDSTable(toMysqlValue(lastOid),
                mysql);
      } else {
        retOid = lastOid;
        lastOid += 1;
        OidTableUtility.setIDSTable(toMysqlValue(lastOid),
                mysql);
      }
    }
    return retOid;
  }

  @Override
  public boolean addObjToMultObj(long oid, long multObjOid) {
    try {
      setMultipleObjectTable(mysql);

      QueryBuilder query = new QueryBuilder();
      InsertClauseColumns columns = new InsertClauseColumns();
      InsertClauseValues values = new InsertClauseValues();

      columns.addColumns(MULTIPLE_OID, OID);
      values.addValues(toMysqlValue(multObjOid), toMysqlValue(oid));

      query.INSERT(MULTIPLE_OBJECT, columns, values);
      mysql.Query(query.toString());

    } catch (SimpleMySQLException ex) {
      Logger.getLogger(MysqlParser.class.getName()).log(Level.SEVERE, null, ex);
    }
    return true;
  }

  @Override
  public boolean removeObjFromMultObj(long oid, long multObjOid) {
    try {
      setMultipleObjectTable(mysql);

      QueryBuilder query = new QueryBuilder();
      WhereClause where = new WhereClause();
      where.compareWithValue(OID, "=", "" + oid).
              AND().
              compareWithColumn(MULTIPLE_OID, "=", "" + multObjOid);

      query.DELETE(true).FROM(MULTIPLE_OBJECT).WHERE(where);
      mysql.Query(query.toString());

    } catch (SimpleMySQLException ex) {
      Logger.getLogger(MysqlParser.class.getName()).log(Level.SEVERE, null, ex);
    }
    return true;
  }

  @Override
  public List<DomainObject> searchObjsInMultObj(String param, Object value,
          long multObjOid) {
    try {
      List<DomainObject> ret = new ArrayList<>();

      QueryBuilder query = new QueryBuilder();
      FromClause from;
      from = new FromClause(MULTIPLE_OBJECT, "m");
      from.CARTESIAN_PRODUCT_AS(DomainObject.class.getName(), "d");
      WhereClause where = new WhereClause();
      where.compareWithColumn("m", OID, "=", "d", OID);
      where.AND();
      where.compareWithValue("m", MULTIPLE_OID, "=", "" + multObjOid);
      query.SELECT(true, OBJECT_NAME).FROM(from).WHERE(where);
      SimpleMySQLResult objsInMultObj;
      objsInMultObj = mysql.Query(query.toString());

      if (objsInMultObj.getNumRows() == 0) {
        return null;
      }

      while (objsInMultObj.next()) {
        Map<String, String> fetch = objsInMultObj.FetchAssoc();

        Class fetchedClass;
        try {
          fetchedClass = Class.forName(fetch.get(OBJECT_NAME));
        } catch (ClassNotFoundException ex) {
          Logger.getLogger(MysqlParser.class.getName()).
                  log(Level.SEVERE, null, ex);
          return null;
        }

        from = null;
        Stack<Class> objectChain = AttrUtility.objectChain(fetchedClass);
        for (Class c : objectChain) {
          if (from == null) {
            from = new FromClause(c.getName());
          } else {
            from.NATURAL_JOIN(c.getName());
          }
        }
        where = new WhereClause();
        where.compareWithColumn(param, "=", toMysqlValue(value));
        query = new QueryBuilder().SELECT(true).FROM(from).WHERE(where);
        SimpleMySQLResult searchResult;
        searchResult = mysql.Query(query.toString());
        if (searchResult.getNumRows() != 0) {
          while (searchResult.next()) {
            Map<String, String> searchFetch = searchResult.FetchAssoc();
            ret.add(loadObj(Long.valueOf(searchFetch.get(OID))));
          }
        }
      }

      return ret;
    } catch (SimpleMySQLException | ResultException ex) {
      Logger.getLogger(MysqlParser.class.getName()).log(Level.SEVERE, null, ex);
    }
    return null;
  }

  @Override
  public List<DomainObject> getAllObjsInMultObj() {
    //To change body of generated methods, choose Tools | Templates.
    throw new UnsupportedOperationException("Not supported yet.");
  }
}
