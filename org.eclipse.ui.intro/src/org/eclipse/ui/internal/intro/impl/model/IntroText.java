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
 * An intro text element.
 */
public class IntroText extends AbstractBaseIntroElement {

    protected static final String TAG_TEXT = "text";

    private String text;

    IntroText(Element element, IPluginDescriptor pd) {
        super(element, pd);
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