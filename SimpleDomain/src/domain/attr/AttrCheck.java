/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package domain.attr;

import domain.DomainObject;
import domain.MultipleObject;
import java.util.Arrays;

/**
 *
 * @author Daniel
 */
public class AttrCheck {

    // TODO: add DATE, DATETIME & TIMESTAMP to primitives
    public static final String[] primitiveTypes = {
        byte.class.getName(), Byte.class.getName(),
        short.class.getName(), Short.class.getName(),
        int.class.getName(), Integer.class.getName(),
        long.class.getName(), Long.class.getName(),
        float.class.getName(), Float.class.getName(),
        double.class.getName(), Double.class.getName(),
        boolean.class.getName(), Boolean.class.getName(),
        char.class.getName(), Character.class.getName(),
        String.class.getName(),};

    public static final String oidType = String.class.getName();
    
    static {
        Arrays.sort(primitiveTypes);
    }

    public static boolean isPrimitive(Class type) {
        return Arrays.binarySearch(primitiveTypes, type.getName()) >= 0;
    }

    public static boolean isDomainObject(Class type) {
        Class domainObject = DomainObject.class;
        return domainObject.isAssignableFrom(type);
    }
    
    public static boolean isMultipleObject(Class type) {
        Class multipleObject = MultipleObject.class;
        return multipleObject.isAssignableFrom(type);
    }
    
    public static DomainType attributeType(Class type) {
        if(isPrimitive(type)) {
            return DomainType.PRIMITIVE;
        } else if(isMultipleObject(type)) {
            return DomainType.MULTIPLE_OBJECT;
        } else if(isDomainObject(type)) {
            return DomainType.DOMAIN_OBJECT;
        } else {
            return DomainType.OTHER;
        }
    }
}
