/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.intro.impl.model;

import org.eclipse.ui.internal.intro.impl.model.util.BundleUtil;
import org.osgi.framework.Bundle;
import org.w3c.dom.Element;

/**
 * An intro image element.
 */
public class IntroImage extends AbstractBaseIntroElement {

    protected static final String TAG_IMAGE = "img"; //$NON-NLS-1$

    private static final String ATT_SRC = "src"; //$NON-NLS-1$
    private static final String ATT_ALT = "alt"; //$NON-NLS-1$

    private Element element;
    private String src;
    private String base;

    IntroImage(Element element, Bundle bundle, String base) {
        super(element, bundle);
        this.element = element;
        this.base = base;
    }
    
    protected void loadFromParent() {
    	// Resolve src.
        src = BundleUtil.getResolvedResourceLocation(base, getSrcAsIs(), getBundle());
    }

    /**
     * @return Returns the alt.
     */
    public String getAlt() {
        return getAttribute(element, ATT_ALT);
    }


    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.internal.intro.impl.model.IntroElement#getType()
     */
    public int getType() {
        return AbstractIntroElement.IMAGE;
    }

    /**
     * @return Returns the src value, already resolved as a local url.
     */
    public String getSrc() {
        return src;
    }

    /**
     * @return Returns the src value, as is, without resolving it as a local
     *         url. The src value is relative to the parent plugin.
     */
    public String getSrcAsIs() {
        return getAttribute(element, ATT_SRC);
    }
}
