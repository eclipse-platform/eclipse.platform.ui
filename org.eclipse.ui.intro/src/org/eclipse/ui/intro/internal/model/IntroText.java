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
 * An intro text element.
 */
public class IntroText extends AbstractCommonIntroElement {

    protected static final String TAG_TEXT = "text";

    private String text;

    IntroText(IConfigurationElement element) {
        super(element);
        text = element.getValue();
    }

    /**
     * @return Returns the description.
     */
    public String getText() {
        return text;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.intro.internal.model.IntroElement#getType()
     */
    public int getType() {
        return AbstractIntroElement.TEXT;
    }

}