/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package domain;

import domain.attr.DomainType;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Daniel
 */
public class Utility {

    /**
     * Checks if a class attribute is a MultipleObject.
     *
     * @param field
     * @return
     */
    public static boolean checkMultipleObject(Field field) {
        Class multipleObject = MultipleObject.class;
        Class type = field.getType();
        return multipleObject.isAssignableFrom(type);
    }

    public static List getInheritedFields(Class<?> type) {
        List<Field> fields = new ArrayList<>();
        for (Class<?> c = type; c != null;
                c = c.getSuperclass()) {
            for (Field f : c.getDeclaredFields()) {
                if (c != type && f.getModifiers() != Modifier.PRIVATE) {
                    fields.add(f);
                }
            }
        }
        return fields;
    }

//    static DomainType attributeType(Class fieldType) {
//        boolean isDomainObject;
//        boolean isMultipleObject;
//        Class multipleObject = MultipleObject.class;
//        Class domainObject = DomainObject.class;
//        isDomainObject = domainObject.isAssignableFrom(fieldType);
//        isMultipleObject = multipleObject.isAssignableFrom(fieldType);
//        if (!domain.mysql.Utility.typeToMySQL(fieldType.getName()).equals("")) {
//            return DomainType.PRIMITIVE;
//        } else if (isMultipleObject) {
//            return DomainType.MULTIPLE_OBJECT;
//        } else if (isDomainObject) {
//            return DomainType.DOMAIN_OBJECT;
//        } else {
//            return DomainType.OTHER;
//        }
//    }
    
}
