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

import org.eclipse.core.runtime.*;
import org.osgi.framework.*;
import org.w3c.dom.*;

/**
 * An intro Head element. Head elements are only interpreted for HTML case. They
 * are always inlined. Ignored in UI Forms case.
 */
public class IntroHead extends AbstractIntroElement {

    protected static final String TAG_HEAD = "head"; //$NON-NLS-1$

    private static final String ATT_SRC = "src"; //$NON-NLS-1$
    private static final String ATT_ENCODING = "encoding"; //$NON-NLS-1$

    private String src;
    private String encoding;

    IntroHead(IConfigurationElement element) {
        super(element);
        src = element.getAttribute(ATT_SRC);
        encoding = element.getAttribute(ATT_ENCODING);

        // Resolve.
        src = IntroModelRoot.getPluginLocation(src, element);
    }

    IntroHead(Element element, Bundle bundle) {
        super(element, bundle);
        src = getAttribute(element, ATT_SRC);
        encoding = getAttribute(element, ATT_ENCODING);

        // Resolve.
        src = IntroModelRoot.getPluginLocation(src, bundle);
    }

    /**
     * @return Returns the src.
     */
    public String getSrc() {
        return src;
    }

    /**
     * @return Returns the encoding of the inlined file. This is not needed for
     *         embedded files.
     */
    public String getInlineEncoding() {
        return encoding;
    }


    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.internal.intro.impl.model.IntroElement#getType()
     */
    public int getType() {
        return AbstractIntroElement.HEAD;
    }

}