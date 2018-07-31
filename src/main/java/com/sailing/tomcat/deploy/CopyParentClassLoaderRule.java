/*
 * Copyright 1999-2001,2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.sailing.tomcat.deploy;


import com.sailing.tomcat.container.Container;
import org.apache.commons.digester.Digester;
import org.apache.commons.digester.Rule;
import org.xml.sax.Attributes;

import java.lang.reflect.Method;


/**
 * <p>Rule that copies the <code>parentClassLoader</code> property from the
 * next-to-top item on the stack (which must be a <code>Container</code>)
 * to the top item on the stack (which must also be a
 * <code>Container</code>).</p>
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.3 $ $Date: 2004/08/26 21:41:12 $
 */

public class CopyParentClassLoaderRule extends Rule {


    // ----------------------------------------------------------- Constructors


    /**
     * Construct a new instance of this Rule.
     *
     * @param digester Digester we are associated with
     */
    public CopyParentClassLoaderRule(Digester digester) {

        super(digester);

    }


    // --------------------------------------------------------- Public Methods


    /**
     * Handle the beginning of an XML element.
     *
     * @param attributes The attributes of this element
     *
     * @exception Exception if a processing error occurs
     */
    public void begin(Attributes attributes) throws Exception {

        if (digester.getDebug() >= 1)
            digester.log("Copying parent class loader");
        Container child = (Container) digester.peek(0);
        Object parent = digester.peek(1);
        Method method =
            parent.getClass().getMethod("getParentClassLoader", new Class[0]);
        ClassLoader classLoader =
            (ClassLoader) method.invoke(parent, new Object[0]);
        child.setParentClassLoader(classLoader);

    }


}
