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

import org.osgi.framework.*;
import org.w3c.dom.*;

/**
 * An intro Link. This model class is responsible for parsing and creating an
 * IntroURL class instance if the URL happens to be a valid intro url.
 */
public class IntroLink extends AbstractTextElement {

    protected static final String TAG_LINK = "link";

    private static final String ATT_LABEL = "label";
    private static final String ATT_URL = "url";

    private String label;
    private String url;
    private IntroURL introURL;

    /**
     * @param element
     */
    IntroLink(Element element, Bundle bundle) {
        super(element, bundle);
        url = getAttribute(element, ATT_URL);
        label = getAttribute(element, ATT_LABEL);

        url = IntroModelRoot.resolveURL(url, bundle);
        if (url != null) {
            // check the URL.
            IntroURLParser parser = new IntroURLParser(url);
            if (parser.hasIntroUrl())
                introURL = parser.getIntroURL();
        }
    }

    /**
     * @return Returns the label.
     */
    public String getLabel() {
        return label;
    }

    /**
     * @return Returns the url.
     */
    public String getUrl() {
        return url;
    }

    /**
     * Retruns an IntroURL instance if link has a valid intro url. Returns null
     * otherwise.
     * 
     * @return Returns the introURL.
     */
    public IntroURL getIntroURL() {
        return introURL;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.internal.intro.impl.model.IntroElement#getType()
     */
    public int getType() {
        return AbstractIntroElement.LINK;
    }

}