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

import java.util.List;

import org.eclipse.swt.SWT;

/**
 * Allow programmatic access to the workbench window trim areas.
 * 
 * <p>
 * <b>Note:</b> This is highly experimental and will change between M4 and M5.
 * For example, the current trim area IDs will be changes to Strings, amongst
 * other things.
 * </p>
 * 
 * @since 3.2
 */
public interface ITrimManager {

	/**
	 * Trim area location.
	 */
	public static final int TOP = SWT.TOP;

	/**
	 * Trim area location.
	 */
	public static final int BOTTOM = SWT.BOTTOM;

	/**
	 * Trim area location.
	 */
	public static final int LEFT = SWT.LEFT;

	/**
	 * Trim area location.
	 */
	public static final int RIGHT = SWT.RIGHT;

	/**
	 * Trim area location.
	 */
	public static final int NONTRIM = SWT.DEFAULT;

	/**
	 * Adds the given control to the layout's trim. The same as calling
	 * addTrim(areaId, trim, null);
	 * 
	 * @param trim
	 *            new window trim to be added
	 * @param areaId
	 *            the area ID
	 * @see #getAreaIds()
	 * @see #addTrim(int, IWindowTrim, IWindowTrim)
	 */
	public void addTrim(int areaId, IWindowTrim trim);

	/**
	 * Adds the given control to the layout's trim. Note that this must be
	 * called for every trim control. If the given widget is already a trim
	 * widget, it will be moved to the new position. Specifying a position
	 * allows a new widget to be inserted between existing trim widgets.
	 * 
	 * <p>
	 * For example, this method allows the caller to say "insert this new
	 * control as trim along the bottom of the layout, to the left of this
	 * existing control".
	 * </p>
	 * 
	 * @param trim
	 *            new window trim to be added
	 * @param areaId
	 *            the area ID
	 * @param beforeMe
	 *            trim to insert before, <code>null</code> to insert at the
	 *            end
	 * @see #getAreaIds()
	 */
	public void addTrim(int areaId, IWindowTrim trim, IWindowTrim beforeMe);

	/**
	 * Removes the given window trim. Note that this has no effect if window
	 * trim is not our window trim.
	 * 
	 * @param toRemove
	 *            a piece of trim.
	 */
	public void removeTrim(IWindowTrim toRemove);

	/**
	 * Return the window trim for a given id.
	 * 
	 * @param id
	 *            the id
	 * @return the window trim, or <code>null</code> if not found.
	 */
	public IWindowTrim getTrim(String id);

	/**
	 * Return all of the IDs for the currently supported trim areas. This is
	 * <b>experimental</b> and will be changing.
	 * 
	 * @return the list of IDs that can be used with area descriptions. We
	 *         currently support SWT.TOP, SWT.BOTTOM, SWT.LEFT, and SWT.RIGHT.
	 * @since 3.2
	 */
	public int[] getAreaIds();

	/**
	 * Return a copy of the IWindowTrim in an ordered array. This will not
	 * return <code>null</code>. This array can be used to shuffle items
	 * around in {@link #updateAreaTrim(int, List, boolean) }.
	 * 
	 * @param areaId
	 *            the trim area id
	 * @return the IWindowTrim array
	 * @since 3.2
	 * @see #getAreaIds()
	 */
	public List getAreaTrim(int areaId);

	/**
	 * Update ID's area description with the new window trim ordering. This
	 * applies the IWindowTrim contains in the array to the trim area named
	 * "ID".
	 * 
	 * @param id
	 *            the trim area ID
	 * @param trim
	 *            the trim array must not be <code>null</code>.
	 * @param removeExtra
	 *            if <code>true</code> the any trim in the specified trim area
	 *            that's not contained in the List is removed from the window
	 *            trim (but not disposed()). If <code>false</code> then the
	 *            extra trim is shuffled to the beginning of the trim area.
	 * @since 3.2
	 * @see #getAreaIds()
	 */
	public void updateAreaTrim(int id, List trim, boolean removeExtra);

	/**
	 * This method returns an aggregate array of all trim items known to this
	 * TrimLayout.
	 * 
	 * @return The List of all IWindowTrim elements
	 * @since 3.2
	 */
	public List getAllTrim();

	/**
	 * Update the visibility of the trim controls. It updates any docking
	 * handles as well. It has no effect on visiblity if the window trim doesn't
	 * belong to this TrimLayout.
	 * 
	 * @param trim
	 *            the trim to update
	 * @param visible
	 *            visible or not
	 * @since 3.2
	 */
	public void setTrimVisible(IWindowTrim trim, boolean visible);
	
	/**
	 * Force the trim areas to layout to pick up changes
	 * 
	 * @since 3.2
	 */
	public void forceLayout();
}
