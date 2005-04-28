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

import java.util.ArrayList;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.ui.internal.intro.impl.util.ImageUtil;

/**
 * An Intro Config component that has captures launch bar information.
 * 
 * @since 3.1
 */
public class LaunchBarElement extends AbstractIntroElement {
    private static final String LOC_LEFT = "left"; //$NON-NLS-1$
    private static final String LOC_BOTTOM = "bottom"; //$NON-NLS-1$
   // private static final String LOC_RIGHT = "right"; //$NON-NLS-1$

    private static final String ATT_LOCATION = "location"; //$NON-NLS-1$
    private static final String ATT_BG = "bg"; //$NON-NLS-1$
    private static final String ATT_FG = "fg"; //$NON-NLS-1$
    private static final String ATT_CLOSE = "close"; //$NON-NLS-1$
    private static final String ATT_IMAGE = "image"; //$NON-NLS-1$

    private static final String TAG_SHORTCUT = "shortcut"; //$NON-NLS-1$
    private static final String TAG_HANDLE = "handle"; //$NON-NLS-1$

    private ArrayList shortcuts;

    public LaunchBarElement(IConfigurationElement element) {
        super(element);
    }


    /**
     * Returns LAUNCH_BAR.
     */
    public int getType() {
        return LAUNCH_BAR;
    }

    /**
     * Returns the desired launch bar orientation that results from the desired
     * location. Valid values are <code>SWT.VERTICAL</code> and
     * <code>SWT.HORIZONTAL</code>.
     * 
     * @return
     */
    public int getOrientation() {
        int location = getLocation();
        return (location == SWT.RIGHT || location == SWT.LEFT) ? SWT.VERTICAL
                : SWT.HORIZONTAL;
    }

    /**
     * Returns the location of the launch bar in the workbench window. Valid
     * values are <code>SWT.RIGHT</code>,<code>SWT.LEFT</code> and
     * <code>SWT.BOTTOM</code>.
     * 
     * @return
     */
    public int getLocation() {
        String location = getCfgElement().getAttribute(ATT_LOCATION);
        int loc = SWT.RIGHT;
        if (location != null) {
            if (location.equals(LOC_LEFT))
                return SWT.LEFT;
            if (location.equals(LOC_BOTTOM))
                return SWT.BOTTOM;
        }
        return loc;
    }

    public String getBackground() {
        return getCfgElement().getAttribute(ATT_BG);
    }

    public String getForeground() {
        return getCfgElement().getAttribute(ATT_FG);
    }

    public boolean getCreateHandle() {
        return getHandleElement() != null;
    }

    public boolean getClose() {
        IConfigurationElement handle = getHandleElement();
        if (handle != null) {
            String value = handle.getAttribute(ATT_CLOSE);
            return value == null || value.equals("true"); //$NON-NLS-1$
        }
        return true;
    }

    /**
     * Returns the relative icon path of the handle image.
     * 
     * @return
     */
    private String getHandleImage() {
        IConfigurationElement handle = getHandleElement();
        if (handle == null)
            return null;
        return handle.getAttribute(ATT_IMAGE);
    }

    /**
     * Returns the icon image of the handle, or <code>null</code> if not
     * defined or found.
     * 
     * @return
     */
    public ImageDescriptor getHandleImageDescriptor() {
        String path = getHandleImage();
        if (path == null)
            return null;
        return ImageUtil.createImageDescriptor(getBundle(), path);
    }

    private IConfigurationElement getHandleElement() {
        IConfigurationElement[] children = getCfgElement().getChildren(
            TAG_HANDLE);
        if (children.length > 0)
            return children[0];
        return null;
    }

    /**
     * Returns an array of shorcut elements.
     * 
     * @return
     */
    public LaunchBarShortcutElement[] getShortcuts() {
        if (shortcuts == null) {
            createShortcuts();
        }
        return (LaunchBarShortcutElement[]) shortcuts
            .toArray(new LaunchBarShortcutElement[shortcuts.size()]);
    }

    /**
     * Creates an array of shortcut elements
     * 
     */
    private void createShortcuts() {
        shortcuts = new ArrayList();
        IConfigurationElement[] children = getCfgElement().getChildren(
            TAG_SHORTCUT);
        for (int i = 0; i < children.length; i++) {
            IConfigurationElement child = children[i];
            LaunchBarShortcutElement shortcut = new LaunchBarShortcutElement(
                child);
            shortcuts.add(shortcut);
        }
    }
}