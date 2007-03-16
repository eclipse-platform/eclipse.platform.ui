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
 * A frame source is the source of frames which appear in a frame list.
 * The frame list asks for the current frame whenever it switches
 * to another frame, so that the context can be restored when the
 * frame becomes current again.
 *
 * @see FrameList
 */
public interface IFrameSource {

    /**
     * Frame constant indicating the current frame.
     */
    public static final int CURRENT_FRAME = 0x0001;

    /**
     * Frame constant indicating the frame for the selection.
     */
    public static final int SELECTION_FRAME = 0x0002;

    /**
     * Frame constant indicating the parent frame.
     */
    public static final int PARENT_FRAME = 0x0003;

    /**
     * Flag constant indicating that the full context should be captured.
     */
    public static final int FULL_CONTEXT = 0x0001;

    /**
     * Returns a new frame describing the state of the source.
     * If the <code>FULL_CONTEXT</code> flag is specified, then the full
     * context of the source should be captured by the frame.
     * Otherwise, only the visible aspects of the frame, such as the name and tool tip text,
     * will be used.
     *
     * @param whichFrame one of the frame constants defined in this interface
     * @param flags a bit-wise OR of the flag constants defined in this interface
     * @return a new frame describing the current state of the source
     */
    public Frame getFrame(int whichFrame, int flags);
}
