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

import org.osgi.framework.*;
import org.w3c.dom.*;

/**
 * An intro div.
 */
public class IntroDiv extends AbstractIntroContainer {

    protected static final String TAG_DIV = "div"; //$NON-NLS-1$
    private static final String ATT_LABEL = "label"; //$NON-NLS-1$

    private String label;

    /**
     * @param element
     */
    IntroDiv(Element element, Bundle bundle) {
        super(element, bundle);
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
        return AbstractIntroElement.DIV;
    }

}

