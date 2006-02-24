/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.internal;

/**
 * This iterface is used to identify and call back to trim elements that can be
 * repositioned to edges other than the one that they're defined as being in by
 * default.
 * <p>
 * NOTE: This class is a part of a 'work in progress' and should not be used
 * without consulting the Platform UI group. No guarantees are made as to the
 * stability of the API (except that the javadoc will get better...;-).
 * </p>
 * 
 * @since 3.2
 * 
 */
public interface ITrimAreaChangeListener {
	/**
	 * If a control's TrimLayoutData has a listener defined then this method
	 * will be called whenever the trim changes the orientation (i.e. side) that
	 * it's on.
	 * <p>
	 * The implementation should perform any adjustments to its control
	 * necessary to support it in its 'newOrientation' (i.e. changing a ToolBar
	 * from horizontal to vertical if it's been redocked to an area on the left
	 * or right side)
	 * </p>
	 * 
	 * @param oldOrientation
	 *            The SWT side that the trim was previously on.
	 * @param newOrientation
	 *            The SWT side that the trim has been repositioned to
	 */
	public void areaChanged(int oldOrientation, int newOrientation);
}
