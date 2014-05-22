/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package domain.attr;

import domain.DomainObject;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Daniel
 */
public class AttrUtility {

    public static Stack<Pair<Class, List<Attr>>> objectChain(Class type,
            Object instance) {
        Stack<Pair<Class, List<Attr>>> chain = new Stack<>();
        for (Class c = type; c != null; c = c.getSuperclass()) {
            chain.push(Pair.of(c, getAttributes(c, instance)));
            if (c.equals(DomainObject.class)) {
                break;
            }
        }
        return chain;
    }
    
    public static Stack<Class> objectChain(Class type) {
        Stack<Class> chain = new Stack<>();
        for (Class c = type; c != null; c = c.getSuperclass()) {
            chain.push(c);
            if (c.equals(DomainObject.class)) {
                break;
            }
        }
        return chain;
    }

    public static List<Attr> getAttributes(Class type, Object instance) {
        List<Attr> attributes = new ArrayList<>();
        for (Field f : type.getDeclaredFields()) {
            f.setAccessible(true);
            try {
                attributes.add(new Attr(f.getName(), f.getType(),
                        f.get(instance), instance.getClass()));
            } catch (IllegalArgumentException | IllegalAccessException ex) {
                Logger.getLogger(AttrUtility.class.getName()).
                        log(Level.SEVERE, null, ex);
            }
        }
        return attributes;
    }

    public static void setAttributes(Class type, Object instance,
            List<Attr> attributes) {
        for (Attr a : attributes) {
            Field f;
            try {
                f = type.getDeclaredField(a.getName());
                f.setAccessible(true);
                f.set(instance, a.getValue());
            } catch (NoSuchFieldException | SecurityException |
                    IllegalArgumentException | IllegalAccessException ex) {
                Logger.getLogger(AttrUtility.class.getName()).
                        log(Level.SEVERE, null, ex);
            }
        }
    }
}
