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

package org.eclipse.ui.intro.internal.model;

import java.util.*;

import org.eclipse.core.runtime.*;
import org.w3c.dom.*;

/**
 * An intro container extension.
 */
public class IntroExtensionContent extends AbstractBaseIntroElement {

    protected static final String TAG_CONTAINER_EXTENSION = "extensionContent";

    protected static final String ATT_PATH = "path";
    private static final String ATT_STYLE = "style";
    private static final String ATT_ALT_STYLE = "alt-style";

    private String path;
    private String style;
    private String altStyle;

    private Element element;

    IntroExtensionContent(Element element, IPluginDescriptor pd) {
        super(element, pd);
        path = getAttribute(element, ATT_PATH);
        style = getAttribute(element, ATT_STYLE);
        altStyle = getAttribute(element, ATT_ALT_STYLE);
        this.element = element;

        // Resolve.
        style = IntroModelRoot.getPluginLocation(style, pd);
        altStyle = IntroModelRoot.getPluginLocation(altStyle, pd);
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
     * @see org.eclipse.ui.intro.internal.model.IntroElement#getType()
     */
    public int getType() {
        return AbstractIntroElement.CONTAINER_EXTENSION;
    }

    protected Element[] getChildren() {
        Vector children = new Vector();
        NodeList nodeList = element.getChildNodes();
        Vector vector = new Vector();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE)
                vector.add(node);
        }
        Element[] filteredElements = new Element[vector.size()];
        vector.copyInto(filteredElements);
        return filteredElements;
    }

    /**
     * @return Returns the altStyle.
     */
    protected String getAltStyle() {
        return altStyle;
    }

    /**
     * @return Returns the style.
     */
    protected String getStyle() {
        return style;
    }

}