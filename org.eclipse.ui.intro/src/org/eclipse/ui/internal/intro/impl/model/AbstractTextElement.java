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

import org.eclipse.ui.internal.intro.impl.util.*;
import org.osgi.framework.*;
import org.w3c.dom.*;

/**
 * An intro config component that can have a single Text element as a child. In
 * case there is more than one text child, the text is retrieved from the first
 * text child element.
 */
public abstract class AbstractTextElement extends AbstractBaseIntroElement {

    private IntroText introText;

    AbstractTextElement(Element element, Bundle bundle) {
        super(element, bundle);
        // description will be null if there is no description element.
        introText = getTextElement(element);
    }

    /**
     * Retruns the intro text element embedded in this element.
     */
    private IntroText getTextElement(Element element) {
        try {
            // There should only be one text element.
            // Since elements where obtained by name, no point validating name.
            NodeList textElements = element
                    .getElementsByTagName(IntroText.TAG_TEXT);
            if (textElements.getLength() == 0)
                // no contributions. done.
                return null;
            IntroText text = new IntroText((Element) textElements.item(0),
                    getBundle());
            text.setParent(this);
            return text;
        } catch (Exception e) {
            Util.handleException(e.getMessage(), e);
            return null;
        }
    }

    /**
     * @return Returns the text of the child text of this element.
     */
    public String getText() {
        // intro text may be null if there is no child Text element.
        if (introText != null)
            return introText.getText();
        else
            return null;
    }

    /**
     * Returns the intro text representing the child text of this element. May
     * return null if there is no text child.
     * 
     * @return Returns the introText.
     */
    public IntroText getIntroText() {
        return introText;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.internal.intro.impl.model.IntroElement#getType()
     */
    public int getType() {
        return AbstractIntroElement.ABSTRACT_TEXT;
    }

    /**
     * Deep copy since class has mutable objects.
     */
    public Object clone() throws CloneNotSupportedException {
        AbstractTextElement clone = (AbstractTextElement) super.clone();
        if (introText != null) {
            IntroText cloneIntroText = (IntroText) introText.clone();
            cloneIntroText.setParent(clone);
            clone.introText = cloneIntroText;
        }
        return clone;
    }

}