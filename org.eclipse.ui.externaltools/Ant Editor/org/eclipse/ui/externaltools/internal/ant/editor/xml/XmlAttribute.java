/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
/**********************************************************************
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/
//
// Copyright:
// GEBIT Gesellschaft fuer EDV-Beratung
// und Informatik-Technologien mbH, 
// Berlin, Duesseldorf, Frankfurt (Germany) 2002
// All rights reserved.
//
package org.eclipse.ui.externaltools.internal.ant.editor.xml;


/**
 * General representation of an xml attribute.
 * <P>
 * Here an xml attribute is refered to as what is specified using like
 * '<... attribute="some value">' in an xml file.
 * 
 * @version 12.10.2002
 * @author Alf Schiefelbein
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
