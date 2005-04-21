/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.intro.impl.model;

import java.net.URL;
import java.util.Vector;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.internal.intro.impl.util.Log;
import org.osgi.framework.Bundle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * Util class for model. Has methods for resolving model attributes, and methods
 * for manipulating XHTML DOM.
 */
public class ModelUtil {

    private static String TAG_BODY = "body"; //$NON-NLS-1$
    private static String TAG_HEAD = "head"; //$NON-NLS-1$
    private static String TAG_BASE = "base"; //$NON-NLS-1$
    private static String TAG_PARAM = "param"; //$NON-NLS-1$
    private static String ATT_SRC = "src"; //$NON-NLS-1$
    private static String ATT_HREF = "href"; //$NON-NLS-1$
    private static String ATT_CITE = "cite"; //$NON-NLS-1$
    private static String ATT_LONGDESC = "longdesc"; //$NON-NLS-1$
    private static String ATT_DATA = "data"; //$NON-NLS-1$
    private static String ATT_CODEBASE = "codebase"; //$NON-NLS-1$
    private static String ATT_VALUE = "value"; //$NON-NLS-1$
    private static String ATT_VALUE_TYPE = "valuetype"; //$NON-NLS-1$


    /*
     * ********* Model util methods ************************************
     */

    /**
     * Checks to see if the passed string is a valid URL (has a protocol), if
     * yes, returns it as is. If no, treats it as a resource relative to the
     * declaring plugin. Return the plugin relative location, fully qualified.
     * Retruns null if the passed string itself is null.
     * 
     * @param resource
     * @param pluginDesc
     * @return returns the URL as is if it had a protocol.
     */
    protected static String resolveURL(String url, String pluginId) {
        Bundle bundle = null;
        if (pluginId != null)
            // if pluginId is not null, use it.
            bundle = Platform.getBundle(pluginId);
        return resolveURL(url, bundle);
    }



    /**
     * Checks to see if the passed string is a valid URL (has a protocol), if
     * yes, returns it as is. If no, treats it as a resource relative to the
     * declaring plugin. Return the plugin relative location, fully qualified.
     * Retruns null if the passed string itself is null.
     * 
     * @param resource
     * @param pluginDesc
     * @return returns the URL as is if it had a protocol.
     */
    protected static String resolveURL(String url, IConfigurationElement element) {
        Bundle bundle = BundleUtil.getBundleFromConfigurationElement(element);
        return resolveURL(url, bundle);
    }



    /**
     * @see resolveURL(String url, IConfigurationElement element)
     */
    protected static String resolveURL(String url, Bundle bundle) {
        // quick exit
        if (url == null)
            return null;
        IntroURLParser parser = new IntroURLParser(url);
        if (parser.hasProtocol())
            return url;
        else
            // make plugin relative url. Only now we need the bundle.
            return BundleUtil.getResolvedResourceLocation(url, bundle);
    }



    /**
     * Util method to support jarring. Used to extract parent folder of intro
     * xml content files. And to extract parent folder of invalidPage.xhtml
     * 
     * @param resource
     */
    protected static void extractParentFolder(Bundle bundle, String contentFile) {
        try {
            IPath parentFolder = ModelUtil.getParentFolder(contentFile);
            URL parentFolderURL = Platform.find(bundle, parentFolder);
            URL url = Platform.asLocalURL(parentFolderURL);
        } catch (Exception e) {
            if (contentFile != null)
                Log.error("Failed to extract Intro content folder for: " //$NON-NLS-1$
                        + contentFile, e);
        }
    }




    /*
     * 
     * ******** XHTML DOM util methods *********************************
     */

    /**
     * Returns the path to the parent folder containing the passed content xml
     * file. It is assumed that the path is a local url representing a content
     * file.
     */
    public static String getParentFolderPath(String contentFilePath) {
        IPath path = getParentFolder(contentFilePath);
        return path.toOSString();
    }

    /**
     * Returns the parent folder of the given path.
     */
    public static IPath getParentFolder(String contentFilePath) {
        IPath path = new Path(contentFilePath);
        path = path.removeLastSegments(1).addTrailingSeparator();
        return path;
    }




    public static void insertBase(Document dom, String baseURL) {
        // there should only be one head and one base element dom.
        NodeList headList = dom.getElementsByTagName(TAG_HEAD);
        Element head = (Element) headList.item(0);
        NodeList baseList = head.getElementsByTagName(TAG_BASE);
        if (baseList.getLength() == 0) {
            // insert a base element, since one is not defined already.
            Element base = dom.createElement(TAG_BASE);
            base.setAttribute(ATT_HREF, baseURL);
            head.insertBefore(base, head.getFirstChild());
        }
    }


    /**
     * Returns a reference to the body of the DOM.
     * 
     * @param dom
     * @return
     */
    public static Element getBodyElement(Document dom) {
        // there should only be one body element dom.
        NodeList bodyList = dom.getElementsByTagName(TAG_BODY);
        Element body = (Element) bodyList.item(0);
        return body;
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
     * Same as getElementsByTagName(Element parent, String tagName) but the
     * parent element is assumed to be the root of the document.
     * 
     * @see getElementsByTagName(Element parent, String tagName)
     */
    public static Element[] getElementsByTagName(Document dom, String tagName) {
        NodeList allChildElements = dom.getElementsByTagName(tagName);
        Vector vector = new Vector();
        for (int i = 0; i < allChildElements.getLength(); i++) {
            // we know that the nodelist is of elements.
            Element aElement = (Element) allChildElements.item(i);
            if (aElement.getParentNode().equals(dom.getDocumentElement()))
                // first level child element. add it. Cant use getParent
                // here.
                vector.add(aElement);
        }
        Element[] filteredElements = new Element[vector.size()];
        vector.copyInto(filteredElements);
        return filteredElements;
    }



    /**
     * Updates all the resource attributes of the passed element to point to a
     * local resolved url.
     * 
     * @param element
     * @param extensionContent
     */
    public static void updateResourceAttributes(Element element,
            String localContentFilePath) {
        String folderLocalPath = getParentFolderPath(localContentFilePath);
        doUpdateResourceAttributes(element, folderLocalPath);
        NodeList children = element.getElementsByTagName("*"); //$NON-NLS-1$
        for (int i = 0; i < children.getLength(); i++) {
            Element child = (Element) children.item(i);
            doUpdateResourceAttributes(child, folderLocalPath);
        }
    }

    private static void doUpdateResourceAttributes(Element element,
            String folderLocalPath) {
        qualifyAttribute(element, ATT_SRC, folderLocalPath);
        qualifyAttribute(element, ATT_HREF, folderLocalPath);
        qualifyAttribute(element, ATT_CITE, folderLocalPath);
        qualifyAttribute(element, ATT_LONGDESC, folderLocalPath);
        qualifyAttribute(element, ATT_CODEBASE, folderLocalPath);
        qualifyAttribute(element, ATT_DATA, folderLocalPath);
        qualifyValueAttribute(element, folderLocalPath);
    }

    private static void qualifyAttribute(Element element, String attributeName,
            String folderLocalPath) {
        if (element.hasAttribute(attributeName)) {
            String attributeValue = element.getAttribute(attributeName);
            if (new IntroURLParser(attributeValue).hasProtocol())
                return;
            IPath localSrcPath = new Path(folderLocalPath)
                .append(attributeValue);
            element.setAttribute(attributeName, localSrcPath.toOSString());
        }
    }

    private static void qualifyValueAttribute(Element element,
            String folderLocalPath) {
        if (element.hasAttribute(ATT_VALUE)
                && element.hasAttribute(ATT_VALUE_TYPE)
                && element.getAttribute(ATT_VALUE_TYPE).equals("ref") //$NON-NLS-1$
                && element.getLocalName().equals(TAG_PARAM)) {
            String value = element.getAttribute(ATT_VALUE);
            if (new IntroURLParser(value).hasProtocol())
                return;
            IPath localSrcPath = new Path(folderLocalPath).append(value);
            element.setAttribute(ATT_VALUE, localSrcPath.toOSString());
        }
    }


    /**
     * Returns an array version of the passed NodeList. Used to work around DOM
     * design issues.
     */
    public static Node[] getArray(NodeList nodeList) {
        Node[] nodes = new Node[nodeList.getLength()];
        for (int i = 0; i < nodeList.getLength(); i++)
            nodes[i] = nodeList.item(i);
        return nodes;
    }


    /**
     * Remove all anchors from this page.
     * 
     */
    public static void removeElement(Document dom, String elementLocalName) {
        // get all elements in DOM and remove them.
        NodeList anchors = dom.getElementsByTagNameNS("*", //$NON-NLS-1$
            elementLocalName);
        // get the array version of the nodelist to work around DOM api design.
        Node[] anchorArray = ModelUtil.getArray(anchors);
        for (int i = 0; i < anchorArray.length; i++) {
            Node anchor = anchorArray[i];
            anchor.getParentNode().removeChild(anchor);
        }

    }



}
