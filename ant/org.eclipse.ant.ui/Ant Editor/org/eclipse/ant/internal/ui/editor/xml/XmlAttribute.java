/*******************************************************************************
 * Copyright (c) 2002, 2003 GEBIT Gesellschaft fuer EDV-Beratung
 * und Informatik-Technologien mbH, 
 * Berlin, Duesseldorf, Frankfurt (Germany) and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     GEBIT Gesellschaft fuer EDV-Beratung und Informatik-Technologien mbH - initial API and implementation
 * 	   IBM Corporation - bug 29148
 *******************************************************************************/

package org.eclipse.ant.internal.ui.editor.xml;


/**
 * General representation of an xml attribute.
 * <P>
 * Here an xml attribute is refered to as what is specified using like
 * '<... attribute="some value">' in an xml file.
 * 
 */
public class XmlAttribute {

    /**
     * The attribute name.
     */
    protected String name;
    
    /**
     * The attribute value.
     */
    protected String value;
    
    /**
     * Creates an instance with the specified name and value.
     */
    public XmlAttribute(String aName, String aValue) {
        name = aName;
        value = aValue;
    }

    /**
     * Returns the name.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the value.
     */
    public String getValue() {
        return value;
    }
}
