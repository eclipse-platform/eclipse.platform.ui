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
package org.eclipse.ui.views.framelist;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * Abstract superclass for actions dealing with frames or a frame list.
 * This listens for changes to the frame list and updates itself
 * accordingly.
 */
public abstract class FrameAction extends Action {
    private FrameList frameList;

    private IPropertyChangeListener propertyChangeListener = new IPropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent event) {
            FrameAction.this.handlePropertyChange(event);
        }
    };

    /**
     * Constructs a new action for the specified frame list.
     * and adds a property change listener on it.
     * 
     * @param frameList the frame list
     */
    protected FrameAction(FrameList frameList) {
        this.frameList = frameList;
        frameList.addPropertyChangeListener(propertyChangeListener);
    }

    /**
     * Disposes this frame action.
     * This implementation removes the property change listener from the frame list.
     */
    public void dispose() {
        frameList.removePropertyChangeListener(propertyChangeListener);
    }

    /**
     * Returns the frame list.
     */
    public FrameList getFrameList() {
        return frameList;
    }

    /**
     * Returns the image descriptor with the given relative path.
     */
    static ImageDescriptor getImageDescriptor(String relativePath) {
        String iconPath = "icons/full/"; //$NON-NLS-1$
        try {
            AbstractUIPlugin plugin = (AbstractUIPlugin) Platform
                    .getPlugin(PlatformUI.PLUGIN_ID);
            URL installURL = plugin.getDescriptor().getInstallURL();
            URL url = new URL(installURL, iconPath + relativePath);
            return ImageDescriptor.createFromURL(url);
        } catch (MalformedURLException e) {
            // should not happen
            return ImageDescriptor.getMissingImageDescriptor();
        }
    }

    /**
     * Handles a property change event from the frame list.
     * This implementation calls <code>update()</code>.
     */
    protected void handlePropertyChange(PropertyChangeEvent event) {
        update();
    }

    /**
     * Updates this action.  This implementation does nothing.
     * Most implementations will override this method.
     */
    public void update() {
    }

}