/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.views.framelist;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.common.EventManager;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

/**
 * Supports a web-browser style of navigation by maintaining a list
 * of frames.  Each frame holds a snapshot of a view at some point 
 * in time.
 * <p>
 * The frame list obtains a snapshot of the current frame from a frame source
 * on creation, and whenever switching to a different frame.
 * </p>
 * <p>
 * A property change notification is sent whenever the current page changes.
 * </p>
 */
public class FrameList extends EventManager {

    /** Property name constant for the current frame. */
    public static final String P_CURRENT_FRAME = "currentFrame"; //$NON-NLS-1$

    private IFrameSource source;

    private List frames;

    private int current;

    /**
     * Creates a new frame list with the given source.
     *
     * @param source the frame source
     */
    public FrameList(IFrameSource source) {
        this.source = source;
        Frame frame = source.getFrame(IFrameSource.CURRENT_FRAME, 0);
        frame.setParent(this);
        frame.setIndex(0);
        frames = new ArrayList();
        frames.add(frame);
        current = 0;
    }

    /**
     * Adds a property change listener.
     * Has no effect if an identical listener is already registered.
     *
     * @param listener a property change listener
     */
    public void addPropertyChangeListener(IPropertyChangeListener listener) {
    	addListenerObject(listener);
    }

    /**
     * Moves the frame pointer back by one.
     * Has no effect if there is no frame before the current one.
     * Fires a <code>P_CURRENT_FRAME</code> property change event.
     */
    public void back() {
        if (current > 0) {
            setCurrent(current - 1);
        }
    }

    /**
     * Notifies any property change listeners that a property has changed.
     * Only listeners registered at the time this method is called are notified.
     *
     * @param event the property change event
     *
     * @see IPropertyChangeListener#propertyChange
     */
    protected void firePropertyChange(PropertyChangeEvent event) {
        Object[] listeners = getListeners();
        for (int i = 0; i < listeners.length; ++i) {
            ((IPropertyChangeListener) listeners[i]).propertyChange(event);
        }
    }

    /**
     * Moves the frame pointer forward by one.
     * Has no effect if there is no frame after the current one.
     * Fires a <code>P_CURRENT_FRAME</code> property change event.
     */
    public void forward() {
        if (current < frames.size() - 1) {
            setCurrent(current + 1);
        }
    }

    /**
     * Returns the current frame.
     * Returns <code>null</code> if there is no current frame.
     *
     * @return the current frame, or <code>null</code>
     */
    public Frame getCurrentFrame() {
        return getFrame(current);
    }

    /**
     * Returns the index of the current frame.
     *
     * @return the index of the current frame
     */
    public int getCurrentIndex() {
        return current;
    }

    /**
     * Returns the frame at the given index, or <code>null</code>
     * if the index is &le; 0 or &ge; <code>size()</code>.
     *
     * @param index the index of the requested frame
     * @return the frame at the given index or <code>null</code>
     */
    public Frame getFrame(int index) {
        if (index < 0 || index >= frames.size()) {
			return null;
		}
        return (Frame) frames.get(index);
    }

    /**
     * Returns the frame source.
     */
    public IFrameSource getSource() {
        return source;
    }

    /**
     * Adds the given frame after the current frame,
     * and advances the pointer to the new frame.
     * Before doing so, updates the current frame, and removes any frames following the current frame.
     * Fires a <code>P_CURRENT_FRAME</code> property change event.
     *
     * @param frame the frame to add
     */
    public void gotoFrame(Frame frame) {
        for (int i = frames.size(); --i > current;) {
            frames.remove(i);
        }
        frame.setParent(this);
        int index = frames.size();
        frame.setIndex(index);
        frames.add(frame);
        setCurrent(index);
    }

    /**
     * Removes a property change listener.
     * Has no effect if an identical listener is not registered.
     *
     * @param listener a property change listener
     */
    public void removePropertyChangeListener(IPropertyChangeListener listener) {
        removeListenerObject(listener);
    }

    /**
     * Sets the current frame to the one with the given index.
     * Updates the old current frame, and fires a <code>P_CURRENT_FRAME</code> property change event
     * if the current frame changes.
     *
     * @param newCurrent the index of the frame
     */
    void setCurrent(int newCurrent) {
        Assert.isTrue(newCurrent >= 0 && newCurrent < frames.size());
        int oldCurrent = this.current;
        if (oldCurrent != newCurrent) {
            updateCurrentFrame();
            this.current = newCurrent;
            firePropertyChange(new PropertyChangeEvent(this, P_CURRENT_FRAME,
                    getFrame(oldCurrent), getFrame(newCurrent)));
        }
    }

    /**
     * Sets the current frame to the frame with the given index.
     * Fires a <code>P_CURRENT_FRAME</code> property change event
     * if the current frame changes.
     */
    public void setCurrentIndex(int index) {
        if (index != -1 && index != current) {
			setCurrent(index);
		}
    }

    /**
     * Returns the number of frames in the frame list.
     */
    public int size() {
        return frames.size();
    }

    /**
     * Replaces the current frame in this list with the current frame 
     * from the frame source.  No event is fired.
     */
    public void updateCurrentFrame() {
        Assert.isTrue(current >= 0);
        Frame frame = source.getFrame(IFrameSource.CURRENT_FRAME,
                IFrameSource.FULL_CONTEXT);
        frame.setParent(this);
        frame.setIndex(current);
        frames.set(current, frame);
    }
}
