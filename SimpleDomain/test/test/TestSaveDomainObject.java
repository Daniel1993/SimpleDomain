/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test;

import domain.DomainManager;
import domain.DomainObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

import static test.Utility.*;

/**
 *
 * @author Daniel
 */
public class TestSaveDomainObject {

    private final static DomainManager manager = DomainManager.getInstance();
    private static A a;

    public TestSaveDomainObject() {
    }

    @BeforeClass
    public static void setUpClass() {
        manager.initializeMySQL(server, database, user, pass);
        manager.transactionBegin();
        A aLocal = new A();
        TestSaveDomainObject.a = aLocal;
        manager.commit(aLocal);
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
        assertEquals("a", selectFromDatabase(a.getClass().getName()).get("c1"));
    }

    @Test
    public void testCharacter() {
        assertEquals("b", selectFromDatabase(a.getClass().getName()).get("c2"));
    }

    @Test
    public void testbyte() {
        assertEquals("3", selectFromDatabase(a.getClass().getName()).get("b1"));
    }

    @Test
    public void testByte() {
        assertEquals("4", selectFromDatabase(a.getClass().getName()).get("b2"));
    }

    @Test
    public void testshort() {
        assertEquals("5", selectFromDatabase(a.getClass().getName()).get("s1"));
    }

    @Test
    public void testShort() {
        assertEquals("6", selectFromDatabase(a.getClass().getName()).get("s2"));
    }

    @Test
    public void testint() {
        assertEquals("7", selectFromDatabase(a.getClass().getName()).get("i1"));
    }

    @Test
    public void testInteger() {
        assertEquals("8", selectFromDatabase(a.getClass().getName()).get("i2"));
    }

    @Test
    public void testlong() {
        assertEquals("9", selectFromDatabase(a.getClass().getName()).get("l1"));
    }

    @Test
    public void testLong() {
        assertEquals("10", selectFromDatabase(a.getClass().getName()).get("l2"));
    }

    @Test
    public void testfloat() {
        assertEquals(11, Float.parseFloat(selectFromDatabase(a.getClass().
                getName()).get("f1")), 0.01);
    }

    @Test
    public void testFloat() {
        assertEquals(12, Float.parseFloat(selectFromDatabase(a.getClass().
                getName()).get("f2")), 0.01);
    }

    @Test
    public void testdouble() {
        assertEquals(13, Double.parseDouble(selectFromDatabase(a.getClass().
                getName()).get("d1")), 0.01);
    }

    @Test
    public void testDouble() {
        assertEquals(13, Double.parseDouble(selectFromDatabase(a.getClass().
                getName()).get("d2")), 0.01);
    }

    @Test
    public void testString() {
        assertEquals("olá  \\'\"`;DROP DATABASE; aaa", selectFromDatabase(a.
                getClass().getName()).get("str"));
    }

}
