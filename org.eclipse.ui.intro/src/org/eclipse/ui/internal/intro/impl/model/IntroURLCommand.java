/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
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
 * An intro url command registration. This model class does not appear as a
 * child under any of the other model classes. It is returned by the
 * SharedConfigExtensionsManager when asked for commands.
 */
public class IntroURLCommand extends AbstractIntroElement {

    public static final String TAG_COMMAND = "command"; //$NON-NLS-1$

    private static final String ATT_NAME = "name"; //$NON-NLS-1$
    private static final String ATT_RESOLVED_VALUE = "resolvedValue"; //$NON-NLS-1$

    private String name;
    private String resolvedValue;


    /**
     * Note: model class with public constructor because it is not instantiated
     * by the model root.
     */
    public IntroURLCommand(IConfigurationElement element) {
        super(element);
        name = element.getAttribute(ATT_NAME);
        resolvedValue = element.getAttribute(ATT_RESOLVED_VALUE);
    }

    /**
     * @return Returns the className.
     */
    public String getName() {
        return name;
    }

    /**
     * @return Returns the pluginId.
     */
    public String getResolvedValue() {
        return resolvedValue;
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