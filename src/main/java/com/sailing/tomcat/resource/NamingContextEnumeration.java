package com.sailing.tomcat.resource;

import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import java.util.Enumeration;
import java.util.Vector;

/**
 * Naming enumeration implementation.
 *
 * @author Remy Maucherat
 * @version $Revision: 1.2 $ $Date: 2004/08/26 21:46:17 $
 */

public class NamingContextEnumeration implements NamingEnumeration {


    // ----------------------------------------------------------- Constructors


    public NamingContextEnumeration(Vector entries) {
        enum1 = entries.elements();
    }


    public NamingContextEnumeration(Enumeration enum1) {
        this.enum1 = enum1;
    }

    /**
     * Underlying enumeration.
     */
    protected Enumeration enum1;

    // -------------------------------------------------------------- Variables




    // --------------------------------------------------------- Public Methods


    /**
     * Retrieves the next element in the enumeration.
     */
    public Object next()
        throws NamingException {
        return nextElement();
    }


    /**
     * Determines whether there are any more elements in the enumeration.
     */
    public boolean hasMore()
        throws NamingException {
        return enum1.hasMoreElements();
    }


    /**
     * Closes this enumeration.
     */
    public void close()
        throws NamingException {
    }


    public boolean hasMoreElements() {
        return enum1.hasMoreElements();
    }


    public Object nextElement() {
        NamingEntry entry = (NamingEntry) enum1.nextElement();
        return new NameClassPair(entry.name, entry.value.getClass().getName());
    }
}

