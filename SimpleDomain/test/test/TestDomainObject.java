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
import static test.Utility.database;
import static test.Utility.pass;
import static test.Utility.server;
import static test.Utility.user;

/**
 *
 * @author Daniel
 */
public class TestDomainObject {

    private final static DomainManager manager = DomainManager.getInstance();

    public TestDomainObject() {
    }

    public static class A extends DomainObject {

        private C c;

        public A() {
            c = new C();
            B b = new B();
            D d = new D();

            b.setAb(d);
            d.setC(c);
            c.setB(b);
        }

        public static class D extends DomainObject {

            private C c;

            public void setC(C c) {
                this.c = c;
            }
        }
    }

    public static class B extends DomainObject {

        private A.D ab;

        public void setAb(A.D ab) {
            this.ab = ab;
        }
    }

    public static class C extends DomainObject {

        private B b;

        private void setB(B b) {
            this.b = b;
        }
    }

    public static class E extends DomainObject {

        private F f;

        public void setF(F f) {
            this.f = f;
        }
    }

    public static class F extends E {
    }

    @BeforeClass
    public static void setUpClass() {
        manager.initializeMySQL(server, database, user, pass);

    }

    @AfterClass
    public static void tearDownClass() {

    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    @Test
    public void testCommitBoundObjects() {
        A a = new A();

        manager.transactionBegin();
        manager.commitAsRoot(a);
        manager.transactionCommit();

        manager.transactionBegin();
        A obtainedA = (A) manager.getRoot();
        manager.transactionCommit();

        assertNotNull(obtainedA.c.b.ab.c.b.ab.c);

        manager.transactionBegin();
        manager.delete(a.c.b.ab);
        manager.delete(a.c.b);
        manager.delete(a.c);
        manager.delete(a);
        manager.transactionCommit();
    }

    @Test
    public void testCommitInheritedObjects() {
        E e = new E();
        F f = new F();
        e.setF(f);

        manager.transactionBegin();
        manager.commitAsRoot(e);
        manager.transactionCommit();

        manager.transactionBegin();
        E obtainedE = (E) manager.getRoot();
        manager.transactionCommit();

        assertNotNull(obtainedE.f);

        manager.transactionBegin();
        manager.delete(e.f);
        manager.delete(e);
        manager.transactionCommit();
    }
}
