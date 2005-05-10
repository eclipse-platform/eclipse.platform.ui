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

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.internal.intro.impl.util.ImageUtil;

/**
 * An Intro Config component that captures launch bar shortcut information.
 * 
 * @since 3.1
 */
public class IntroLaunchBarShortcut extends AbstractIntroElement {

    protected static final String TAG_SHORTCUT = "shortcut"; //$NON-NLS-1$

    private static final String ATT_TOOLTIP = "tooltip"; //$NON-NLS-1$
    private static final String ATT_ICON = "icon"; //$NON-NLS-1$
    private static final String ATT_URL = "url"; //$NON-NLS-1$

    IntroLaunchBarShortcut(IConfigurationElement element) {
        super(element);
    }


    public int getType() {
        return AbstractIntroElement.LAUNCH_BAR_SHORTCUT;
    }

    /**
     * Returns the URL of this shortcut.
     * 
     * @return
     */
    public String getURL() {
        return getCfgElement().getAttribute(ATT_URL);
    }

    /**
     * Returns the tooltip of this shortcut.
     * 
     * @return
     */
    public String getToolTip() {
        return getCfgElement().getAttribute(ATT_TOOLTIP);
    }

    /**
     * Returns the relative icon path of this shortcut.
     * 
     * @return
     */
    private String getIcon() {
        return getCfgElement().getAttribute(ATT_ICON);
    }

    /**
     * Returns the icon image of this shortcut, or <code>null</code> if not
     * found.
     * 
     * @return
     */
    public ImageDescriptor getImageDescriptor() {
        return ImageUtil.createImageDescriptor(getBundle(), getIcon());
    }
}