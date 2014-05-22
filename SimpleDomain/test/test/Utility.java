/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test;

import domain.DomainObject;
import domain.MultipleObject;
import java.util.Map;
import simplemysql.query.QueryBuilder;
import simplemysql.SimpleMySQL;
import simplemysql.SimpleMySQLResult;

/**
 *
 * @author Daniel
 */
class Utility {

  public static final String server = "localhost:3306";
  public static final String database = "test";
  public static final String user = "root";
  public static final String pass = "";

  public static class A extends DomainObject {

    char c1 = 'a';
    Character c2 = 'b';
    byte b1 = 3;
    Byte b2 = 4;
    short s1 = 5;
    Short s2 = 6;
    int i1 = 7;
    Integer i2 = 8;
    long l1 = 9;
    Long l2 = 10l;
    float f1 = 11;
    Float f2 = 12f;
    double d1 = 13;
    Double d2 = 13d;
    String str = "olá  '\"`;DROP DATABASE; aaa";

    public A() {
    }
  }

  public static class B extends DomainObject {
    MultipleObject<C> mult = new MultipleObject<>();
    
    public B() {
    }
  }
  
  public static class C extends DomainObject {
    private int i;
    
    public C(int i) {
      this.i = i;
    }

    public int getI() {
      return i;
    }

    public void setI(int i) {
      this.i = i;
    }
  }
  
  public static Map<String, String> selectFromDatabase(String tableName) {
    SimpleMySQL mysql = new SimpleMySQL();
    mysql.connect(server, user, pass, database);
    QueryBuilder query = new QueryBuilder().SELECT(false).FROM(tableName);
    SimpleMySQLResult result = mysql.Query(query.toString());
    Map<String, String> row = result.FetchAssoc();
    mysql.close();
    return row;
  }
}
