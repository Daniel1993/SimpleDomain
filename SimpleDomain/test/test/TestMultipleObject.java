/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test;

import domain.DomainManager;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static test.Utility.*;

/**
 *
 * @author Daniel
 */
public class TestMultipleObject {

  private final static DomainManager manager = DomainManager.getInstance();
  private static Utility.B b;

  @BeforeClass
  public static void setUpClass() {
    manager.initializeMySQL(server, database, user, pass);
    manager.transactionBegin();
    Utility.B bLocal = new Utility.B();
    for(int i = 0; i < 100; i++) {
      Utility.C c = new C(i);
      bLocal.mult.add(c);
    }
    manager.commitAsRoot(bLocal);
    manager.transactionCommit();

    manager.transactionBegin();
    TestMultipleObject.b = (Utility.B) manager.getRoot();
    manager.transactionCommit();
  }

  @AfterClass
  public static void tearDownClass() {
    manager.transactionBegin();
    manager.delete(b);
    manager.transactionCommit();
  }

  @Before
  public void setUp() {
  }

  @After
  public void tearDown() {
  }

  @Test
  public void testB() {
    assertNotNull(b);
  }
  
  @Test
  public void testMult() {
    assertNotNull(b.mult);
  }
  
  @Test
  public void testSearchC34() {
    Utility.C c34 = (Utility.C) b.mult.search("i", 34).get(0);
    assertNotNull(c34);
    c34.setI(35);
    manager.commit(c34);
    assertEquals(2, b.mult.search("i", 34).size());
  }
}
