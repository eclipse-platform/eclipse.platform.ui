/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.layout;

import org.eclipse.swt.SWT;

/**
 * TrimLayoutData can be attached to a control in a TrimLayout to configure how
 * TrimLayout will arrange the control. TrimLayoutData can override
 * the control's preferred size. This is useful for attaching trim objects with
 * badly-behaved computeSize implementations. TrimLayoutData can also specify whether 
 * the control should be resized with the layout.
 * <p>
 * To create a fixed-size control based on its preferred size, use:
 * <code>
 * new TrimLayoutData(false, SWT.DEFAULT, SWT.DEFAULT)
 * </code> 
 * </p>
 * 
 * <p> 
 * To create a resizable control that will be resized according to the available
 * space in the layout, use:
 * <code>
 * new TrimLayoutData();
 * </code>
 * </p>
 * 
 * <p>
 * To create a control with a predetermined fixed size (that overrides the preferred size
 * of the control, use:
 * <code>
 * new TrimLayoutData(false, someFixedWidthInPixels, someFixedHeightInPixels);
 * </code> 
 * </p>
 *
 * @since 3.0  
 */
public class TrimLayoutData {
    /**
     * Width of the control (or SWT.DEFAULT if the control's preferred width should be used)
     */
    int widthHint = SWT.DEFAULT;

    /**
     * Height of the control (or SWT.DEFAULT if the control's preferred height should be used)
     */
    int heightHint = SWT.DEFAULT;

    /**
     * Flag indicating whether the control should resize with the window. Note that 
     * available space is always divided equally among all resizable controls on the 
     * same side of the layout, regardless of their preferred size.
     */
    public boolean resizable = true;

    /**
     * Creates a default TrimLayoutData. The default trim layout data is resizable.
     */
    public TrimLayoutData() {
    }

    /**
     * Creates a TrimLayoutData with user-specified parameters.
     * 
     * @param resizable if true, the control will be resized with the layout. If there
     * is more than one resizable control on the same side of the layout, the available
     * space will be divided equally among all the controls.
     * 
     * @param widthHint overrides the preferred width of the control (pixels). If SWT.DEFAULT,
     * then the control's preferred width will be used. This has no effect for 
     * horizontally resizable controls.
     *  
     * @param heightHint overrides the preferred height of the control (pixels). If SWT.DEFAULT,
     * then the control's preferred height will be used. This has no effect for 
     * vertically resizable controls.
     */
    public TrimLayoutData(boolean resizable, int widthHint, int heightHint) {
        this.widthHint = widthHint;
        this.heightHint = heightHint;
        this.resizable = resizable;
    }

}
