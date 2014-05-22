/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package domain.attr;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class Attr {

    private final String name;
    private final DomainType domainType;
    private final Class type;
    private final Object value;
    private final Class container;
    
    /**
     * Initializes a Attribute.
     *
     * @param name the name of the attribute
     * @param type the class of the attribute
     * @param escapedValue the attribute value in string format
     * @param value the actual value of the attribute casted to Object
     */
    Attr(String name, Class type, Object value, Class container) {
        this.name = name;
        this.domainType = AttrCheck.attributeType(type);
        this.type = type;
        this.value = value;
        this.container = container;
    }

    /**
     * 
     * @return 
     */
    public String getName() {
        return name;
    }

    /**
     * 
     * @return 
     */
    public DomainType getDomainType() {
        return domainType;
    }

    /**
     * 
     * @return 
     */
    public Object getValue() {
        return value;
    }

    /**
     * 
     * @return 
     */
    public Class getType() {
        return type;
    }
    
    /**
     * 
     * @return 
     */
    public Class getContainer() {
        return container;
    }
}
