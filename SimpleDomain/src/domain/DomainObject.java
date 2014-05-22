package domain;

/**
 *
 * @author Daniel
 */
public abstract class DomainObject {

    /**
     * Object unique id.
     *
     * oid's are unique to all DomainObject instances.
     */
    long oid = DomainManager.getInstance().getAnUniqueID();
    String name = this.getClass().getName();

    /**
     * Initializates a DomainObject.
     *
     * All extended DomainObjects must have a public constructor that receives
     * no arguments.
     */
    public DomainObject() {
    }

    /**
     * Gets the object id.
     *
     * @return this instance unique id
     * @see #oid
     */
    public long getOID() {
        return oid;
    }

    /**
     * Sets the object id
     *
     * @param oid a unique id to this instance
     * @see #oid
     */
    public void setOID(long oid) {
        this.oid = oid;
    }
}
