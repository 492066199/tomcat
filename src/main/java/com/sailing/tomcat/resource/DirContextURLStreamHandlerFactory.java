package com.sailing.tomcat.resource;

import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

/**
 * Factory for Stream handlers to a JNDI directory context.
 * 
 * @author <a href="mailto:remm@apache.org">Remy Maucherat</a>
 * @version $Revision: 1.4 $
 */
public class DirContextURLStreamHandlerFactory 
    implements URLStreamHandlerFactory {
    
    
    // ----------------------------------------------------------- Constructors
    
    
    public DirContextURLStreamHandlerFactory() {
    }
    
    
    // ----------------------------------------------------- Instance Variables
    
    
    // ------------------------------------------------------------- Properties
    
    
    // ---------------------------------------- URLStreamHandlerFactory Methods
    
    
    /**
     * Creates a new URLStreamHandler instance with the specified protocol.
     * Will return null if the protocol is not <code>jndi</code>.
     * 
     * @param protocol the protocol (must be "jndi" here)
     * @return a URLStreamHandler for the jndi protocol, or null if the 
     * protocol is not JNDI
     */
    public URLStreamHandler createURLStreamHandler(String protocol) {
        if (protocol.equals("jndi")) {
            return new DirContextURLStreamHandler();
        } else {
            return null;
        }
    }
    
    
}
