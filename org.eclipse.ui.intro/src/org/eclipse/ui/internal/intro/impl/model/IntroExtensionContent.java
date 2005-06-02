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

import java.util.Hashtable;
import java.util.Vector;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.internal.intro.impl.model.loader.IntroContentParser;
import org.eclipse.ui.internal.intro.impl.model.util.BundleUtil;
import org.eclipse.ui.internal.intro.impl.model.util.ModelUtil;
import org.osgi.framework.Bundle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * An intro container extension. If the content attribute is defined, then it is
 * assumed that we have XHTML content in an external file. Load content from
 * external DOM. No need to worry about caching here because this is a transient
 * model class. It is used and then disregarded from the model.<br>
 * Just like in a page, the styles and altStyles strings can be a comma
 * separated list of styles. Handle this by storing styles just like pages.
 */
public class IntroExtensionContent extends AbstractIntroElement {

    protected static final String TAG_CONTAINER_EXTENSION = "extensionContent"; //$NON-NLS-1$

    protected static final String ATT_PATH = "path"; //$NON-NLS-1$
    private static final String ATT_STYLE = "style"; //$NON-NLS-1$
    private static final String ATT_ALT_STYLE = "alt-style"; //$NON-NLS-1$
    private static final String ATT_CONTENT = "content"; //$NON-NLS-1$

    private String path;
    private String content;

    private Element element;
    private String base;

    private Vector styles = new Vector();
    private Hashtable altStyles = new Hashtable();

    IntroExtensionContent(Element element, Bundle bundle, String base) {
        super(element, bundle);
        path = getAttribute(element, ATT_PATH);
        content = getAttribute(element, ATT_CONTENT);
        this.element = element;
        this.base = base;

        // load and resolve styles, first.
        init(element, bundle, base);

        // if content is not null we have XHTML extension.
        if (content != null) {
            // BASE: since content is being loaded from another XHTML file and
            // not this xml file, point the base of this page to be relative to
            // the new xml file location.
            IPath subBase = ModelUtil.getParentFolderPath(content);
            String newBase = new Path(base).append(subBase).toString();
            content = BundleUtil.getResolvedResourceLocation(base, content,
                bundle);
            this.base = newBase;
        }

    }


    /**
     * Initialize styles. Take first style in style attribute and make it the
     * page style. Then put other styles in styles vectors. Make sure to resolve
     * each style.
     * 
     * @param element
     * @param bundle
     */
    private void init(Element element, Bundle bundle, String base) {
        String[] styleValues = getAttributeList(element, ATT_STYLE);
        if (styleValues != null && styleValues.length > 0) {
            for (int i = 0; i < styleValues.length; i++) {
                String style = styleValues[i];
                style = BundleUtil.getResolvedResourceLocation(base, style,
                    bundle);
                addStyle(style);
            }
        }

        String[] altStyleValues = getAttributeList(element, ATT_ALT_STYLE);
        if (altStyleValues != null && altStyleValues.length > 0) {
            for (int i = 0; i < altStyleValues.length; i++) {
                String style = altStyleValues[i];
                style = BundleUtil.getResolvedResourceLocation(base, style,
                    bundle);
                addAltStyle(style, bundle);
            }
        }
    }

    /**
     * Adds the given style to the list. Style is not added if it already exists
     * in the list.
     * 
     * @param style
     */
    protected void addStyle(String style) {
        if (styles.contains(style))
            return;
        styles.add(style);
    }


    /**
     * Adds the given style to the list.Style is not added if it already exists
     * in the list.
     * 
     * @param altStyle
     */
    protected void addAltStyle(String altStyle, Bundle bundle) {
        if (altStyles.containsKey(altStyle))
            return;
        altStyles.put(altStyle, bundle);
    }



    /**
     * @return Returns the path.
     */
    public String getPath() {
        return path;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.internal.intro.impl.model.IntroElement#getType()
     */
    public int getType() {
        return AbstractIntroElement.CONTAINER_EXTENSION;
    }

    protected Element[] getChildren() {
        NodeList nodeList = element.getChildNodes();
        Vector vector = new Vector();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE)
                vector.add(node);
        }
        Element[] filteredElements = new Element[vector.size()];
        vector.copyInto(filteredElements);
        // free DOM model for memory performance.
        this.element = null;
        return filteredElements;
    }

    public boolean isXHTMLContent() {
        return content != null ? true : false;
    }

    public Document getDocument() {
        if (isXHTMLContent()) {
            IntroContentParser parser = new IntroContentParser(content);
            Document dom = parser.getDocument();
            if (dom == null)
                // bad xml. Parser would have logged fact.
                return null;
            // parser content should be XHTML because defining content here
            // means that we want XHTML extension.
            if (parser.hasXHTMLContent())
                return dom;

        }
        return null;
    }

    /**
     * @return Returns the altStyle.
     */
    protected Hashtable getAltStyles() {
        return altStyles;
    }

    /**
     * @return Returns the style.
     */
    protected String[] getStyles() {
        String[] stylesArray = new String[styles.size()];
        styles.copyInto(stylesArray);
        return stylesArray;
    }

    /**
     * @return Returns the content.
     */
    public String getContent() {
        return content;
    }

    public String getBase() {
        return base;
    }
}
