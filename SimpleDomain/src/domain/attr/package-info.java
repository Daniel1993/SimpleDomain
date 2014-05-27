/**
 * Handles DomainObjects fields.
 *
 * A class field is represented by an <code>Attr</code>, an <code>Attr</code>
 * has a <code>name</code>, <code>type</code>, <code>value</code>,
 * <code>container</code> and <code>DomainType</code>. The
 * <code>container</code> is the class type that have this <code>Attr</code>.
 * The <code>DomainType</code> is used to differ the field when saving it to a
 * database or file, there are 4 <code>DomainType</code>s:
 * <code>PRIMITIVE</code>, <code>DOMAIN_OBJECT</code>,
 * <code>MULTIPLE_OBJECT</code> and <code>OTHER</code>.
 *
 */
package domain.attr;
