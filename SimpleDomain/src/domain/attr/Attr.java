/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package domain.attr;

/**
 *
 * @author Daniel Castro
 */
public class Attr {

    /**
     * The field name.
     */
    private final String name;
    
    /**
     * The field DomainType.
     * 
     * @see DomainType
     */
    private final DomainType domainType;
    
    /**
     * This field class type.
     * 
     * Allowed types are: boolean, int
     */
    private final Class type;
    
    /**
     * This field value.
     * 
     * The value may be a primitive java type (int, float, ...), a DomainObject
     * or a MultipleObject other types will be discarted.
     */
    private final Object value;
    
    /**
     * Class type of the Object that contains the field.
     */
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
     * @return the field name
     * @see #name
     */
    public String getName() {
        return name;
    }

    /**
     * 
     * @return the field DomainType classification
     * @see #domainType
     */
    public DomainType getDomainType() {
        return domainType;
    }

    /**
     * 
     * @return the field value
     * @see #value
     */
    public Object getValue() {
        return value;
    }

    /**
     * 
     * @return 
     * @see #type
     */
    public Class getType() {
        return type;
    }
    
    /**
     * 
     * @return the container class type
     * @see #container
     */
    public Class getContainer() {
        return container;
    }
}
