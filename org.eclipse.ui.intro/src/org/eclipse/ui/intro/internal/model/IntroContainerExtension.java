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

import org.eclipse.core.runtime.*;

/**
 * An intro container extension.
 */
public class IntroContainerExtension extends AbstractCommonIntroElement {

    protected static final String CONTAINER_EXTENSION_ELEMENT = "extensionContent";

    protected static final String ATT_PATH = "path";
    private static final String ATT_STYLE = "style";
    private static final String ATT_ALT_STYLE = "alt-style";

    private String path;
    private String style;
    private String altStyle;

    IntroContainerExtension(IConfigurationElement element) {
        super(element);
        path = element.getAttribute(ATT_PATH);
        style = element.getAttribute(ATT_STYLE);
        altStyle = element.getAttribute(ATT_ALT_STYLE);

        // Resolve.
        style = IntroModelRoot.getPluginLocation(style, element);
        altStyle = IntroModelRoot.getPluginLocation(altStyle, element);
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

    protected IConfigurationElement[] getChildren() {
        return getConfigurationElement().getChildren();
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