/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.layout;

import org.eclipse.swt.widgets.Control;

/**
 * Interface for trim controls that can be docked to the edge of a Workbench
 * window using drag-and-drop.
 * 
 * <p>
 * <b>Note:</b> This interface is highly experimental, and will probably change
 * between M4 and M5. For example, it will support a "lifecycle" that allows the
 * {@link ITrimManager} to update its modifiers (like SWT.TOP or SWT.LEFT) so the
 * IWindowTrim can dispose and re-create its control. This will likely effect
 * methods like {@link #dock(int) }, {@link #getControl() },
 * {@link #getValidSides() }, etc.
 * </p>
 * 
 * @since 3.2
 */
public interface IWindowTrim {
	/**
	 * Returns the control representing this trim widget, or null if it has not
	 * yet been created.
	 * 
	 * @return the control for the trim widget.
	 */
	Control getControl();

	/**
	 * Returns the set of sides that this trim can be docked onto.
	 * 
	 * @return bitwise OR of one or more of SWT.TOP, SWT.BOTTOM, SWT.LEFT, and
	 *         SWT.RIGHT
	 */
	int getValidSides();

	/**
	 * Called to notify the trim object that it has been docked on the given
	 * side of the layout
	 * 
	 * @param dropSide
	 *            the trim drop area
	 * 
	 */
	void dock(int dropSide);

	/**
	 * Each piece of window trim must have a unique ID to participate fully as
	 * trim.
	 * 
	 * @return The unique id
	 * @since 3.2
	 */
	public String getId();

	/**
	 * Returns the (localized) display name for this trim. This is used, for
	 * example, to construct menu items...
	 * 
	 * @return The display name for this trim
	 * 
	 * @since 3.2
	 */
	public String getDisplayName();

	/**
	 * Determines whether a particular trim can be 'closed' using the common
	 * Trim UI.
	 * 
	 * @return true if the UI should profer the close affordance; false
	 *         otherwise
	 * 
	 * @since 3.2
	 */
	public boolean isCloseable();

	/**
	 * This method is called when the trim UI has closed (hidden) the trim. The
	 * controls associated with the trim will have already been removed from the
	 * trim layout. The implementor should take any necessary clean up actions
	 * here.
	 * 
	 * @since 3.2
	 */
	public void handleClose();

	/**
	 * Overrides the preferred width of the control (pixels). If SWT.DEFAULT,
	 * then the control's preferred width will be used. This has no effect for
	 * horizontally resizable controls.
	 * 
	 * @return pixels, or SWT.DEFAULT
	 * @since 3.2
	 */
	public int getWidthHint();

	/**
	 * Overrides the preferred height of the control (pixels). If SWT.DEFAULT,
	 * then the control's preferred height will be used. This has no effect for
	 * vertically resizable controls.
	 * 
	 * @return pixels, or SWT.DEFAULT
	 * @since 3.2
	 */
	public int getHeightHint();

	/**
	 * If true, the control will be resized with the layout. If there is more
	 * than one resizable control on the same side of the layout, the available
	 * space will be divided equally among all the resizeable controls.
	 * 
	 * @return <code>true</code> or <code>false</code>.
	 * @since 3.2
	 */
	public boolean isResizeable();
}
