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
 * An intro text element.
 */
public class IntroText extends AbstractBaseIntroElement {

    protected static final String TAG_TEXT = "text"; //$NON-NLS-1$

    private String text;

    IntroText(Element element, Bundle bundle) {
        super(element, bundle);
        Node textNode = element.getFirstChild();
        if (textNode.getNodeType() == Node.TEXT_NODE)
            text = textNode.getNodeValue();
    }

    /**
     * @return Returns the text description.
     */
    public String getText() {
        return text;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.internal.intro.impl.model.IntroElement#getType()
     */
    public int getType() {
        return AbstractIntroElement.TEXT;
    }


    /**
     * @return Returns the class id.
     */
    public String getClassId() {
        return super.class_id;
    }
}