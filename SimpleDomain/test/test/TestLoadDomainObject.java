/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test;

import domain.DomainManager;
import static org.hamcrest.core.Is.is;
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
public class TestLoadDomainObject {

  private final static DomainManager manager = DomainManager.getInstance();
  private static Utility.A a;

  public TestLoadDomainObject() {
  }

  @BeforeClass
  public static void setUpClass() {
    manager.initializeMySQL(server, database, user, pass);
    manager.transactionBegin();
    Utility.A aLocal = new Utility.A();
    manager.commitAsRoot(aLocal);
    manager.transactionCommit();

    manager.transactionBegin();
    TestLoadDomainObject.a = (A) manager.getRoot();
    manager.transactionCommit();
  }

  @AfterClass
  public static void tearDownClass() {
    manager.transactionBegin();
    manager.delete(a);
    manager.transactionCommit();
  }

  @Before
  public void setUp() {
  }

  @After
  public void tearDown() {
  }

  @Test
  public void testChar() {
    assertEquals('a', a.c1);
  }

  @Test
  public void testCharacter() {
    assertEquals("b", String.valueOf(a.c2));
  }

  @Test
  public void testbyte() {
    assertThat("3", is(String.valueOf(a.b1)));
  }

  @Test
  public void testByte() {
    assertThat("4", is(String.valueOf(a.b2)));
  }

  @Test
  public void testshort() {
    assertEquals(5, a.s1);
  }

  @Test
  public void testShort() {
    assertThat((short)6, is(a.s2));
  }

  @Test
  public void testint() {
    assertEquals(7, a.i1);
  }

  @Test
  public void testInteger() {
    assertEquals("8", String.valueOf(a.i2));
  }

  @Test
  public void testlong() {
    assertEquals("9", String.valueOf(a.l1));
  }

  @Test
  public void testLong() {
    assertEquals("10", String.valueOf(a.l2));
  }

  @Test
  public void testfloat() {
    assertEquals(11, a.f1, 0.01);
  }

  @Test
  public void testFloat() {
    assertEquals(12, a.f2, 0.01);
  }

  @Test
  public void testdouble() {
    assertEquals(13, a.d1, 0.01);
  }

  @Test
  public void testDouble() {
    assertEquals(13, a.d2, 0.01);
  }

  @Test
  public void testString() {
    assertEquals("olá  \\'\"`;DROP DATABASE; aaa", a.str);
  }
}
