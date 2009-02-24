/*******************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others.
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
 * An intro anchor. An anchor is the only valid target for a config extension.
 */
public class IntroAnchor extends AbstractIntroIdElement {

    protected static final String TAG_ANCHOR = "anchor"; //$NON-NLS-1$

    /**
     * @param element
     */
    IntroAnchor(Element element, Bundle bundle) {
        super(element, bundle);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.internal.intro.impl.model.IntroElement#getType()
     */
    public int getType() {
        return AbstractIntroElement.ANCHOR;
    }


}
