/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;

import org.eclipse.jface.resource.JFaceResources;


/**
 * Extension interface for {@link org.eclipse.jface.text.IInformationControl}.
 * Adds API
 * <ul>
 * <li>to test the visibility of the control,</li>
 * <li>to test whether another control is a child of the information control,</li>
 * <li>to compute size constraints based on the information control's main font and</li>
 * <li>to allow the mouse to move into this information control.</li>
 * </ul>
 * 
 * @see org.eclipse.jface.text.IInformationControl
 * @since 3.4
 */
public interface IInformationControlExtension5 {

	/**
	 * Tests whether the given control is this information control
	 * or a child of this information control.
	 * 
	 * @param control the control to test
	 * @return <code>true</code> iff the given control is this information control
	 * or a child of this information control
	 */
	public boolean containsControl(Control control);
	
	/**
	 * @return <code>true</code> iff the information control is currently visible
	 */
	public abstract boolean isVisible();
	
	/**
	 * Returns whether the mouse is allowed to move into this information control.
	 * Note that this feature only works if this information control also implements
	 * {@link IInformationControlExtension3}.
	 * 
	 * @return <code>true</code> to allow the mouse to move into this information control,
	 * <code>false</code> to close the information control when the mouse is moved into it
	 */
	public boolean allowMoveIntoControl();

	/**
	 * Computes the width- and height constraints of the information control in
	 * pixels, based on the given width and height in characters. Implementors
	 * should use the main font of the information control to do the
	 * characters-to-pixels conversion. This is typically the
	 * {@link JFaceResources#getDialogFont() dialog font}.
	 * 
	 * @param widthInChars the width constraint in number of characters
	 * @param heightInChars the height constraint in number of characters
	 * @return a point with width and height in pixels, or <code>null</code>
	 *         to use the subject control's font to calculate the size
	 */
	public Point computeSizeConstraints(int widthInChars, int heightInChars);
	
}
