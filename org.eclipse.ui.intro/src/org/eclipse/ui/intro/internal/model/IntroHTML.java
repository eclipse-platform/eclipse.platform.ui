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
import org.eclipse.ui.intro.internal.util.*;
import org.w3c.dom.*;

/**
 * An intro HTML element. Can have text and image as fall back. "type" attribute
 * in markup determines if it is inlined or not. if inlined, value of 'src' will
 * be treated as a snippet of HTML to emit 'in-place'. If 'embed', a valid
 * (full) HTML document will be embedded using HTML 'OBJECT' tag.
 */
public class IntroHTML extends AbstractTextElement {

    protected static final String TAG_HTML = "html";

    private static final String ATT_SRC = "src";
    /**
     * type must be "inline" or "embed".
     */
    private static final String ATT_TYPE = "type";

    private String src;
    private String html_type;
    private IntroImage introImage;

    IntroHTML(Element element, IPluginDescriptor pd) {
        super(element, pd);
        src = getAttribute(element, ATT_SRC);
        html_type = getAttribute(element, ATT_TYPE);
        if (html_type != null && !html_type.equalsIgnoreCase("inline")
                && !html_type.equalsIgnoreCase("embed"))
            // if type is not correct, null it.
            html_type = null;

        // description will be null if there is no description element.
        introImage = getIntroImage(element);

        // Resolve.
        src = IntroModelRoot.getPluginLocation(src, pd);
    }

    /**
     * Retruns the intro image element embedded in this element.
     */
    private IntroImage getIntroImage(Element element) {
        try {
            // There should only be one text element. Since elements where
            // obtained by name, no point validating name.
            NodeList imageElements = element
                    .getElementsByTagName(IntroImage.TAG_IMAGE);
            if (imageElements.getLength() == 0)
                // no contributions. done.
                return null;
            IntroImage image = new IntroImage((Element) imageElements.item(0),
                    getPluginDesc());
            image.setParent(this);
            return image;
        } catch (Exception e) {
            Util.handleException(e.getMessage(), e);
            return null;
        }
    }

    /**
     * Returns the html type. Will be either "inline" or "embed". If not, null
     * will be returned as if the attibute was nto defined.
     * 
     * @return Returns the html type value.
     */
    public boolean isInlined() {
        return (html_type != null && html_type.equalsIgnoreCase("inline")) ? true
                : false;
    }

    /**
     * @return Returns the src.
     */
    public String getSrc() {
        return src;
    }

    /**
     * Returns the intro image used as a replacement if this HTML element fails.
     * May return null if there is no image child.
     * 
     * @return Returns the introImage.
     */
    public IntroImage getIntroImage() {
        return introImage;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.intro.internal.model.IntroElement#getType()
     */
    public int getType() {
        return AbstractIntroElement.HTML;
    }

}