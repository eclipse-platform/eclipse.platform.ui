/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.views.framelist;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

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
