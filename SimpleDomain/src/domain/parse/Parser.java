/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package domain.parse;

import domain.DomainObject;
import domain.attr.Attr;
import domain.attr.Pair;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 *
 * @author Daniel
 */
public interface Parser {

    /**
     * Saves a DomainObject in a persistent manner.
     *
     * @param attrs a data structure of the object to parse, first element to
     * pop must be a DomainObject
     */
    void parseAttrs(Stack<Pair<Class, List<Attr>>> attrs);

    /**
     * Sets a saved object as root.
     *
     * @param type
     * @param oid the saved object oid
     */
    void setRoot(Stack<Class> type, long oid);

    /**
     * Loads the root object.
     *
     * @return the root object
     */
    DomainObject loadObj();

    /**
     * Loads a object with a specific oid.
     *
     * @param type
     * @param oid the object oid
     * @return the required object
     */
    DomainObject loadObj(Stack<Class> type, long oid);

    boolean deleteObj(Stack<Class> type);
    
    boolean deleteObj(Stack<Class> type, long oid);
    
    boolean addObjToMultObj(long oid, long multObjOid);
    
    boolean removeObjFromMultObj(long oid, long multObjOid);
    
    List<DomainObject> searchObjsInMultObj(String param, Object value,
            long multObjOid);
    
    List<DomainObject> getAllObjsInMultObj();
    
    void transactionBegin();
    
    void transactionCommit();
    
    void transactionRollback();
    
    long getOid();
    
    /**
     * Initializes the parser.
     *
     * @param args
     * @return true if successfully initialized, false otherwise
     */
    boolean initialize(String... args);
}
