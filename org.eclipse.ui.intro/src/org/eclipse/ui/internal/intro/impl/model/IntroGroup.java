/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.intro.impl.model;

import org.osgi.framework.Bundle;
import org.w3c.dom.Element;

/**
 * An intro div.
 */
public class IntroGroup extends AbstractIntroContainer {

    protected static final String TAG_GROUP = "group"; //$NON-NLS-1$
    private static final String ATT_LABEL = "label"; //$NON-NLS-1$

    private String label;

    /**
     * @param element
     */
    IntroGroup(Element element, Bundle bundle, String base) {
        super(element, bundle, base);
        label = getAttribute(element, ATT_LABEL);
    }

    /**
     * @return Returns the label.
     */
    public String getLabel() {
        return label;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.internal.intro.impl.model.IntroElement#getType()
     */
    public int getType() {
        return AbstractIntroElement.GROUP;
    }

}
