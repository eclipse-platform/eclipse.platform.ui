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
package org.eclipse.ui.intro.internal.model.loader;

import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.ui.intro.internal.util.*;
import org.w3c.dom.*;


/**
 * Utilities class for help with loading the intro model from the Platform
 * runtime model and from the DOM of content files.
 */
public class ModelLoaderUtil {

    /**
     * Utility method to validate an elements name.
     * 
     * @param element
     * @param validName
     * @return
     */
    public static boolean isValidElementName(IConfigurationElement element,
            String validName) {

        if (element.getName().equals(validName))
            return true;
        else
            // bad element name.
            return false;
    }

    /**
     * Utility method to verify that there is only a single configElement in the
     * passed array of elements. If the array is empty, null is returned. If
     * there is more than one element in the array, the first one is picked, but
     * this fact is logged. Attribute passed is used for logging.
     * 
     * @param configElements
     * @return the first configElement in the array, or null if the array is
     *         empty.
     */
    public static IConfigurationElement validateSingleContribution(
            IConfigurationElement[] configElements, String logAttribute) {

        int arraySize = configElements.length;
        if (arraySize == 0)
            // No one contributed to extension. return null.
            return null;

        // we should only have one, so use first one.
        IConfigurationElement configElement = configElements[0];
        Logger.logInfo("Loaded " + configElement.getName() + " from "
                + getLogString(configElement, logAttribute));

        if (arraySize != 1) {
            // we have more than one, warn in the log.
            for (int i = 1; i < arraySize; i++)
                // log each extra extension.
                Logger.logWarning(getLogString(configElements[i], logAttribute)
                        + " ignored due to multiple contributions");
        }
        return configElement;
    }

    /**
     * Utility method to return a string to display in .log. If logAttribute is
     * not null, its value is also printed.
     */
    public static String getLogString(IConfigurationElement element,
            String logAttribute) {
        StringBuffer buffer = new StringBuffer("Plugin:");
        buffer.append(element.getDeclaringExtension()
                .getDeclaringPluginDescriptor().getUniqueIdentifier());
        buffer.append("  Extension:");
        buffer.append(element.getDeclaringExtension()
                .getExtensionPointUniqueIdentifier());
        buffer.append("  element:");
        buffer.append(element.getName());
        if (logAttribute != null) {
            buffer.append("  ");
            buffer.append(logAttribute);
            buffer.append(":");
            buffer.append(element.getAttribute(logAttribute));
        }
        return buffer.toString();
    }

    /**
     * Utility method to verify that there is only a single Element in the
     * passed array of elements. If the list is empty, null is returned. If
     * there is more than one element in the array, the first one is picked, but
     * this fact is logged. Attribute passed is used for logging.
     * 
     * @param Elements
     * 
     * @return the first Element in the array, or null if the array is empty.
     */
    public static Element validateSingleContribution(Element[] elements,
            String logAttribute) {

        int arraySize = elements.length;
        if (arraySize == 0)
            // element list in empty. return null.
            return null;

        // we should only have one, so use first one.
        Element element = (Element) elements[0];
        Logger.logInfo("Loaded " + element.getNodeName() + " from "
                + getLogString(element, logAttribute));

        if (arraySize != 1) {
            // we have more than one, warn in the log.
            for (int i = 1; i < arraySize; i++)
                // log each extra extension.
                Logger.logWarning(getLogString(element, logAttribute)
                        + " ignored due to multiple contributions");
        }
        return element;
    }

    /**
     * Utility method to return a string to display in .log. If logAttribute is
     * not null, its value is also printed.
     */
    public static String getLogString(Element element, String logAttribute) {
        StringBuffer buffer = new StringBuffer("XML document:");
        buffer.append(element.getOwnerDocument().toString());
        buffer.append("  Parent:");
        buffer.append(element.getParentNode().getNodeName());
        buffer.append("  element:");
        buffer.append(element.getNodeName());
        if (logAttribute != null) {
            buffer.append("  ");
            buffer.append(logAttribute);
            buffer.append(":");
            buffer.append(element.getAttribute(logAttribute));
        }
        return buffer.toString();
    }



    /**
     * Returns an Element array of all first level descendant Elements with a
     * given tag name, in the order in which they are encountered in the DOM.
     * Unlike the JAXP apis, which returns preorder traversal of this Element
     * tree, this method filters out children deeper than first level child
     * nodes.
     */
    public static Element[] getElementsByTagName(Element parent, String tagName) {
        NodeList allChildElements = parent.getElementsByTagName(tagName);
        Vector vector = new Vector();
        for (int i = 0; i < allChildElements.getLength(); i++) {
            // we know that the nodelist is of elements.
            Element aElement = (Element) allChildElements.item(i);
            if (aElement.getParentNode().equals(parent))
                // first level child element. add it.
                vector.add(aElement);
        }
        Element[] filteredElements = new Element[vector.size()];
        vector.copyInto(filteredElements);
        return filteredElements;
    }

    /**
     * @see getElementsByTagName(Element parent, String tagName)
     */
    public static Element[] getElementsByTagName(Document dom, String tagName) {
        NodeList allChildElements = dom.getElementsByTagName(tagName);
        Vector vector = new Vector();
        for (int i = 0; i < allChildElements.getLength(); i++) {
            // we know that the nodelist is of elements.
            Element aElement = (Element) allChildElements.item(i);
            if (aElement.getParentNode().equals(dom.getDocumentElement()))
                // first level child element. add it. Cant use getParent here.
                vector.add(aElement);
        }
        Element[] filteredElements = new Element[vector.size()];
        vector.copyInto(filteredElements);
        return filteredElements;
    }


}