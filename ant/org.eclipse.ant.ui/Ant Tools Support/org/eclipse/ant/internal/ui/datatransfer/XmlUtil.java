/*******************************************************************************
 * Copyright (c) 2004, 2005 Richard Hoefter and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Richard Hoefter (richard.hoefter@web.de) - initial API and implementation
 *     IBM Corporation - nlsing and incorporating into Eclipse
 *******************************************************************************/

package org.eclipse.ant.internal.ui.datatransfer;

import java.io.StringWriter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

/**
 * XML Utilities.
 */
public class XmlUtil
{
    private XmlUtil()
    {
    }

    /**
     * Convert document to formatted XML string.
     */
    public static String toString(Document doc) throws TransformerConfigurationException, TransformerFactoryConfigurationError, TransformerException
    {
        StringWriter writer = new StringWriter();
        Source source = new DOMSource(doc);
        Result result = new StreamResult(writer);
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4"); //$NON-NLS-1$ //$NON-NLS-2$
        transformer.transform(source, result);
        return writer.toString();
    }

    /**
     * Include a file into an XML document by adding an entity reference.
     * @param doc     XML document
     * @param name    name of the entity reference to create
     * @param file    name of file to include 
     * @return        XML document with entity reference
     */
    public static String addEntity(Document doc, String name, String file) throws TransformerConfigurationException, TransformerFactoryConfigurationError, TransformerException
    {
        String xml = XmlUtil.toString(doc);
        return addEntity(xml, name, file);
    }
    
    /**
     * Include a file into an XML document by adding an entity reference.
     * @param xml     XML document
     * @param name    name of the entity reference to create
     * @param file    name of file to include 
     * @return        XML document with entity reference
     */
    public static String addEntity(String xml, String name, String file)
    {
        // NOTE: It is not possible to write a DOCTYPE with an internal DTD using transformer.
        //       It is also not possible to write an entity reference with JAXP.
        StringBuffer xmlBuffer = new StringBuffer(xml);
        int index = xmlBuffer.indexOf(StringUtil.NEWLINE) != -1 ? xmlBuffer.indexOf(StringUtil.NEWLINE) : 0;
        StringBuffer entity= new StringBuffer();
        entity.append(StringUtil.NEWLINE);
        entity.append("<!DOCTYPE project [<!ENTITY "); //$NON-NLS-1$
        entity.append(name);
        entity.append(" SYSTEM \"file:"); //$NON-NLS-1$
        entity.append(file);
        entity.append("\">]>"); //$NON-NLS-1$
        xmlBuffer.insert(index, entity.toString());
        index = xmlBuffer.indexOf("basedir") != -1 ? xmlBuffer.indexOf("basedir") : 0; //$NON-NLS-1$ //$NON-NLS-2$
        index = xmlBuffer.indexOf(StringUtil.NEWLINE, index);
        if (index != -1)
        {
            xmlBuffer.insert(index, StringUtil.NEWLINE + "    &" + name + ';'); //$NON-NLS-1$
        }
        return xmlBuffer.toString();
    }
}
