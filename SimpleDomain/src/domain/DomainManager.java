/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package domain;

import domain.parse.Parser;
import domain.parse.ParserInitializer;
import domain.attr.AttrUtility;
import domain.parse.mysql.MysqlParser;

/**
 *
 * @author Daniel
 */
public final class DomainManager {

  private static final DomainManager domainManagerInstance
          = new DomainManager();

  /**
   * Gets the instance of DomainManager.
   *
   * @return the DomainManager instance
   */
  public static DomainManager getInstance() {
    return domainManagerInstance;
  }

  private Parser presistentParser;

  /**
   * Initializes a new DomainManager. This constructor is intentionally private,
   * this way is not possible to achieve two instances from outside this class
   * (Singleton pattern).
   */
  private DomainManager() {
  }

  public DomainObject getRoot() {
    return presistentParser.loadObj();
  }

  public <T extends DomainObject> void commit(T obj) {
    //...
    presistentParser.parseAttrs(
            AttrUtility.objectChain(obj.getClass(), obj));
    // if multiple objects
    //presistentParser.parseMultipleObject(null, objId, mulObjId);
  }

  public <T extends DomainObject> void delete(T obj) {
    presistentParser.deleteObj(null, obj.getOID());
  }

  public <T extends DomainObject> void commitAsRoot(T obj) {
    commit(obj);
    presistentParser.setRoot(AttrUtility.objectChain(obj.getClass()),
            obj.getOID());
  }

  /**
   * Initializes the DomainManager with database parameters. This method shall
   * be called before any other utility method from this class.
   *
   * @param databaseServer in the form &lt;host&gt;:&lt;port&gt;
   * @param databaseName name of the database to use
   * @param databaseUser user with privileges to create and modify tables from
   * the selected database, as well as insert, select, update, etc. entries from
   * that database tables
   * @param databasePass user password
   */
  public void initializeMySQL(String databaseServer,
          String databaseName, String databaseUser, String databasePass) {
    presistentParser = MysqlParser.getInstance();
    ParserInitializer.initializeMySQL(presistentParser,
            databaseServer, databaseName, databaseUser, databasePass);
  }

  public void transactionBegin() {
    presistentParser.transactionBegin();
  }

  public void transactionCommit() {
    presistentParser.transactionCommit();
  }

  public void transactionRollback() {
    presistentParser.transactionRollback();
  }

  /**
   *
   * @return
   */
  long getAnUniqueID() {
    return presistentParser.getOid();
  }

  Parser getPresistentParser() {
    return presistentParser;
  }
}
