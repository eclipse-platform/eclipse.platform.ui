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

package org.eclipse.ui.internal.intro.impl.model;

import org.eclipse.core.runtime.*;
import org.w3c.dom.*;

/**
 * An intro image element.
 */
public class IntroInclude extends AbstractIntroElement {

    protected static final String TAG_INCLUDE = "include";

    private static final String ATT_CONFIG_ID = "configId";
    private static final String ATT_PATH = "path";
    /**
     * boolean attribute, default is false.
     */
    private static final String ATT_MERGE_STYLE = "merge-style";

    private String configId;
    private String path;
    private boolean mergeStyle = false;

    IntroInclude(Element element, IPluginDescriptor pd) {
        super(element, pd);
        configId = getAttribute(element, ATT_CONFIG_ID);
        path = getAttribute(element, ATT_PATH);
        String mergeStyleString = getAttribute(element, ATT_MERGE_STYLE);
        mergeStyle = (mergeStyleString != null && mergeStyleString
                .equalsIgnoreCase("true")) ? true : false;
    }

    /**
     * @return Returns the configId.
     */
    public String getConfigId() {
        return configId;
    }

    /**
     * @return Returns the mergeStyle.
     */
    public boolean getMergeStyle() {
        return mergeStyle;
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
        return AbstractIntroElement.INCLUDE;
    }

}