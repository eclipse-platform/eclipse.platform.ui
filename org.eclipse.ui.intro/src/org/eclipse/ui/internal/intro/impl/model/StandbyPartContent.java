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

/**
 * An intro standby part registration. This model class does not appear as a
 * child under any of the other model classes. It is returned by the
 * ExtensionPointManager when asked for registration parts.
 */
public class StandbyPartContent extends AbstractBaseIntroElement {

    public static final String STANDBY_PART_ELEMENT = "standbyPart"; //$NON-NLS-1$

    private static final String PLUGIN_ID_ATTRIBUTE = "pluginId"; //$NON-NLS-1$
    private static final String CLASS_ATTRIBUTE = "class"; //$NON-NLS-1$

    private String pluginId;
    private String className;

    /**
     * Note: the only model class with public constructor because it is not
     * instantiated by the model root.
     * 
     * @param element
     */
    public StandbyPartContent(IConfigurationElement element) {
        super(element);
        pluginId = element.getAttribute(PLUGIN_ID_ATTRIBUTE);
        className = element.getAttribute(CLASS_ATTRIBUTE);
    }


    /**
     * @return Returns the className.
     */
    public String getClassName() {
        return className;
    }

    /**
     * @return Returns the pluginId.
     */
    public String getPluginId() {
        return pluginId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.internal.intro.impl.model.IntroElement#getType()
     */
    public int getType() {
        // this model class does not need a type so far.
        return 0;
    }
}