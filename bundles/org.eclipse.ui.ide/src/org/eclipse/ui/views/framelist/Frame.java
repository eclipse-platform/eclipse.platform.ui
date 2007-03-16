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

/**
 * Generic frame, which captures the state for one frame in the frame list.
 * Clients may subclass this frame to add their own state.
 */
public class Frame {

    private int index = -1;

    private FrameList parent;

    private String name = ""; //$NON-NLS-1$

    private String toolTipText;

    /**
     * Constructs a new frame. <p>
     * 
     * This implementation does nothing.
     */
    public Frame() {
    }

    /**
     * Returns the index of the frame in the frame list.
     * Only valid once the frame has been added to the frame list.
     * 
     * @return the index of the frame in the frame list.
     */
    public int getIndex() {
        return index;
    }

    /**
     * Returns the displayable name for the frame.
     *
     * @return the displayable name for the frame.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the frame list.
     * 
     * @return the frame list
     */
    public FrameList getParent() {
        return parent;
    }

    /**
     * Returns the tool tip text to show for the frame.
     * This can form part of the tool tip for actions like the back and forward
     * actions.
     * 
     * @return the tool tip text to show for the frame
     */
    public String getToolTipText() {
        return toolTipText;
    }

    /**
     * Sets the index of the frame in the frame list.
     * Should only be called by the frame list.
     * 
     * @param index the index of the frame in the frame list
     */
    public void setIndex(int index) {
        this.index = index;
    }

    /**
     * Sets the displayable name for the frame.
     * 
     * @param name the displayable name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the frame list.
     * 
     * @param parent the frame list
     */
    public void setParent(FrameList parent) {
        this.parent = parent;
    }

    /**
     * Sets the tool tip text to show for the frame.
     * This can form part of the tool tip for actions like the back and forward
     * actions.
     * 
     * @param toolTipText the tool tip text to show for the frame.
     */
    public void setToolTipText(String toolTipText) {
        this.toolTipText = toolTipText;
    }
}
