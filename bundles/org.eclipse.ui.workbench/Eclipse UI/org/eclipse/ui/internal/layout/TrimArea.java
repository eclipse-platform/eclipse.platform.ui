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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.internal.WindowTrimProxy;

/**
 * Represents one Trim Area.
 * 
 * @since 3.2
 */
public class TrimArea {
	// this is no longer necessary, since every piece of window trim defined
	// itself as the trim layout data.
//	private static final TrimLayoutData defaultData = new TrimLayoutData();
	private static final IWindowTrim defaultData = new WindowTrimProxy(null, null, null, 0, true);

	/**
	 * This method separates resizable controls from non-resizable controls.
	 * 
	 * @param input
	 *            the list of {@link SizeCache} to filter
	 * @param resizable
	 *            will contain resizable controls from the input list
	 * @param nonResizable
	 *            will contain non-resizable controls from the input list
	 * @param width
	 *            if true, we're interested in horizontally-resizable controls.
	 *            Else we're interested in vertically resizable controls
	 */
	static void filterResizable(List input, List resizable, List nonResizable,
			boolean width) {
		Iterator iter = input.iterator();
		while (iter.hasNext()) {
			SizeCache next = (SizeCache) iter.next();

			if (next.getControl().isVisible()) {
				if (isResizable(next.getControl(), width)) {
					resizable.add(next);
				} else {
					nonResizable.add(next);
				}
			}
		}
	}

	/**
	 * Helper function to check for resizeable controls.
	 * @param control the control to check
	 * @param horizontally the direction of resizeability.
	 * @return <code>true</code> if the control is resizeable.
	 */
	static boolean isResizable(Control control, boolean horizontally) {
		IWindowTrim data = getData(control);

		if (!data.isResizeable()) {
			return false;
		}

		if (horizontally) {
			return data.getWidthHint() == SWT.DEFAULT;
		}

		return data.getHeightHint() == SWT.DEFAULT;
	}

	private static IWindowTrim getData(Control control) {
		IWindowTrim data = (IWindowTrim) control.getLayoutData();
		if (data == null) {
			data = defaultData;
		}

		return data;
	}

	/**
	 * Cause the SizeCache to compute it's size.
	 * @param toCompute the cache to compute
	 * @param widthHint in pixels
	 * @param heightHint in pixels
	 * @return a point
	 */
	private static Point computeSize(SizeCache toCompute, int widthHint,
			int heightHint) {
		IWindowTrim data = getData(toCompute.getControl());

		if (widthHint == SWT.DEFAULT) {
			widthHint = data.getWidthHint();
		}

		if (heightHint == SWT.DEFAULT) {
			heightHint = data.getHeightHint();
		}

		if (widthHint == SWT.DEFAULT || heightHint == SWT.DEFAULT) {
			return toCompute.computeSize(widthHint, heightHint);
		}

		return new Point(widthHint, heightHint);
	}

	static int getSize(SizeCache toCompute, int hint, boolean width) {
		if (width) {
			return computeSize(toCompute, SWT.DEFAULT, hint).x;
		}

		return computeSize(toCompute, hint, SWT.DEFAULT).y;
	}

	/**
	 * Computes the maximum dimensions of controls in the given list
	 * 
	 * @param caches
	 *            a list of {@link SizeCache}
	 * @param hint a size hint in pixels
	 * @param width are we interested in height or width
	 * @return pixel width
	 */
	private static int maxDimension(List caches, int hint, boolean width) {

		if (hint == SWT.DEFAULT) {
			int result = 0;
			Iterator iter = caches.iterator();

			while (iter.hasNext()) {
				SizeCache next = (SizeCache) iter.next();

				result = Math.max(getSize(next, SWT.DEFAULT, width), result);
			}

			return result;
		}

		List resizable = new ArrayList(caches.size());
		List nonResizable = new ArrayList(caches.size());

		filterResizable(caches, resizable, nonResizable, width);

		int result = 0;
		int usedHeight = 0;

		Iterator iter = nonResizable.iterator();

		while (iter.hasNext()) {
			SizeCache next = (SizeCache) iter.next();

			Point nextSize = computeSize(next, SWT.DEFAULT, SWT.DEFAULT);

			if (width) {
				result = Math.max(result, nextSize.x);
				usedHeight += nextSize.y;
			} else {
				result = Math.max(result, nextSize.y);
				usedHeight += nextSize.x;
			}
		}

		if (resizable.size() > 0) {
			int individualHint = (hint - usedHeight) / resizable.size();

			iter = resizable.iterator();

			while (iter.hasNext()) {
				SizeCache next = (SizeCache) iter.next();

				result = Math.max(result, getSize(next, individualHint, width));
			}
		}

		return result;
	}

	/**
	 * Our area ID.
	 */
	private int fId;

	/**
	 * An NLS display name.
	 */
	private String fDisplayName;

	/**
	 * Each trimArea is an ordered list of TrimDescriptors.
	 */
	private ArrayList fTrim;

	/**
	 * Relevant modifiers for a piece of trim to create it's control if it's
	 * interested in this area, like SWT.TOP, SWT.LEFT, etc.
	 */
	private int fControlModifiers;

	/**
	 * A default size for this trim area.
	 */
	private int fTrimSize;

	/**
	 * Create the trim area with its ID.
	 * 
	 * @param id
	 * @param displayName
	 *            the NLS display name
	 */
	public TrimArea(int id, String displayName) {
		fTrim = new ArrayList();
		fId = id;
		fDisplayName = displayName;
		fControlModifiers = SWT.HORIZONTAL;
	}

	/**
	 * return true of the trim area is empty
	 * 
	 * @return <code>true</code>
	 */
	public boolean isEmpty() {
		return fTrim.isEmpty();
	}

	/**
	 * Return the ordered list of trim for this area.
	 * 
	 * @return a List containing IWindowTrim
	 */
	public List getTrims() {
		List trim = new ArrayList(fTrim.size());
		Iterator d = fTrim.iterator();

		while (d.hasNext()) {
			TrimDescriptor desc = (TrimDescriptor) d.next();
			trim.add(desc.getTrim());
		}
		return trim;
	}

	/**
	 * Return the ordered list of trim descriptors for this area.
	 * 
	 * @return a List containing TrimDescriptor
	 */
	public List getDescriptors() {
		return (List) fTrim.clone();
	}

	/**
	 * Set the trim size for this area.
	 * 
	 * @param size
	 *            the size in pixels.
	 */
	public void setTrimSize(int size) {
		fTrimSize = size;
	}

	/**
	 * Return the trim size for this area.
	 * 
	 * @return the size in pixels
	 */
	public int getTrimSize() {
		return fTrimSize;
	}

	/**
	 * Caculate a max dimension for this trim area. It uses a different hint
	 * depending on its orientation.
	 * 
	 * @param wHint
	 *            a width hint in pixels
	 * @param hHint
	 *            a height hint in pixels
	 * @return the size in pixels
	 */
	public int calculateTrimSize(int wHint, int hHint) {
		int size = 0;
		if (!fTrim.isEmpty()) {
			size = getTrimSize();
		}

		if (size == SWT.DEFAULT) {
			int hint = isVertical() ? hHint : wHint;
			size = maxDimension(getCaches(), hint, isVertical());
		}
		return size;
	}

	/**
	 * return true if this area orientation is vertical.
	 * 
	 * @return <code>true</code>
	 */
	public boolean isVertical() {
		return (fControlModifiers & SWT.VERTICAL) == SWT.VERTICAL
				|| fControlModifiers == SWT.LEFT
				|| fControlModifiers == SWT.RIGHT;
	}

	/**
	 * The ID for this area.
	 * 
	 * @return the ID.
	 */
	public int getId() {
		return fId;
	}

	/**
	 * The NLS display name for this area.
	 * 
	 * @return the String display name.
	 */
	public String getDisplayName() {
		return fDisplayName;
	}

	/**
	 * Add the descriptor representing a piece of trim to this trim area.
	 * 
	 * @param desc
	 *            the trim descriptor
	 */
	public void addTrim(TrimDescriptor desc) {
		fTrim.add(desc);
	}

	/**
	 * Insert this desc before the other desc.  If beforeMe is not
	 * part of this area it just defaults to an add.
	 * 
	 * @param desc
	 *            the window trim
	 * @param beforeMe
	 *            before this trim
	 */
	public void addTrim(TrimDescriptor desc, TrimDescriptor beforeMe) {
		int idx = fTrim.indexOf(beforeMe);
		if (idx == -1) {
			fTrim.add(desc);
		} else {
			ListIterator i = fTrim.listIterator(idx);
			i.add(desc);
		}
	}

	/**
	 * Remove the descriptor representing a piece of trim from this trim area.
	 * 
	 * @param desc
	 *            the trim descriptor
	 */
	public void removeTrim(TrimDescriptor desc) {
		fTrim.remove(desc);
	}

	/**
	 * Does this area contain a piece of trim.
	 * 
	 * @param desc
	 *            the trim
	 * @return <code>true</code> if we contain the trim.
	 */
	public boolean contains(TrimDescriptor desc) {
		return fTrim.contains(desc);
	}

	/**
	 * Takes the trim area and turns it into an List of {@link SizeCache}.
	 * There can be more items in the return list than there are trim
	 * descriptors in the area.
	 * 
	 * @return a list of {@link SizeCache}
	 */
	public List getCaches() {
		ArrayList result = new ArrayList(fTrim.size());
		Iterator d = fTrim.iterator();
		while (d.hasNext()) {
			TrimDescriptor desc = (TrimDescriptor) d.next();
			if (desc.getDockingCache() != null) {
				result.add(desc.getDockingCache());
			}
			result.add(desc.getCache());
		}
		return result;
	}

	/**
	 * The bitwise SWT modifiers that this trim area suggests, like SWT.TOP
	 * or SWT.LEFT. The control modifiers determine the orientation,
	 * amongst other things.
	 * 
	 * @return the bitwise OR of the SWT constants.
	 */
	public int getControlModifiers() {
		return fControlModifiers;
	}

	/**
	 * The bitwise SWT modifiers that this trim area suggests, like SWT.TOP
	 * or SWT.LEFT.
	 * 
	 * @param mod
	 *            the bitwise OR of the SWT constants.
	 */
	public void setControlModifiers(int mod) {
		fControlModifiers = mod;
	}
}
