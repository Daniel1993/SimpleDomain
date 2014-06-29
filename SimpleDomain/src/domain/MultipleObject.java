/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package domain;

import domain.attr.AttrUtility;
import java.util.List;

/**
 *
 * @author Daniel
 * @param <T>
 */
public final class MultipleObject<T extends DomainObject> extends DomainObject {

  /**
   * Initializes MultipleObject.
   *
   * @see DomainObject#DomainObject(boolean)
   */
  public MultipleObject() {
    super();
  }

  public boolean add(T obj) {
    DomainManager.getInstance().getPresistentParser().saveObj(AttrUtility.
            objectChain(obj.getClass(), obj));
    return DomainManager.getInstance().getPresistentParser().addObjToMultObj(
            obj.getOID(), oid);
  }

  public boolean remove(T obj) {
    return DomainManager.getInstance().getPresistentParser().
            removeObjFromMultObj(obj.getOID(), oid);
  }

  public List<DomainObject> search(String attr, Object value) {
    return DomainManager.getInstance().getPresistentParser().
            searchObjsInMultObj(attr, value, oid);
  }

  public List<DomainObject> getAll() {
    return DomainManager.getInstance().getPresistentParser().
            getAllObjsInMultObj();
  }
}
