/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
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

import org.eclipse.jface.util.Geometry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.ui.internal.IWindowTrim;

/**
 * Lays out the children of a Composite. One control occupies the center of the
 * composite, and any number of controls may be attached to the top, bottom, and
 * sides. This is a common layout for application windows, which typically have
 * a central work area and a number of small widgets (toolbars, status lines,
 * etc.) attached to the edge.
 * 
 * <p>
 * Unlike most other SWT layouts, this layout does not require layout data to be
 * attached to each child control. Instead, member functions on the Layout are
 * used to control the positioning of controls within the layout.
 * </p>
 * 
 * <p>
 * The interface to this layout is intended to easily support drag-and-drop.
 * Trim widgets can be added, removed, or inserted between other existing
 * widgets and the layout will adjust accordingly. If one side of the layout
 * contains no trim widgets, the central area will expand to reclaim the unused
 * space.
 * </p>
 * 
 * <p>
 * This layout must be told about every widget that it is supposed to arrange.
 * If the composite contains additional widgets, they will not be moved by the
 * layout and may be arranged through other means.
 * </p>
 * 
 * @since 3.0
 */
public class TrimLayout extends Layout implements ICachingLayout {

	private static final TrimLayoutData defaultData = new TrimLayoutData();

	private SizeCache centerArea = new SizeCache();

	/**
	 * Each trimArea is a list of TrimDescriptors.
	 */
	private List[] fTrimArea;

	private int[] trimSizes;

	private int marginWidth;

	private int marginHeight;

	private int topSpacing;

	private int bottomSpacing;

	private int leftSpacing;

	private int rightSpacing;

	// private Map mapControlOntoData = new HashMap();
	//
	// private Map mapControlOntoTrim = new HashMap();

	// Position constants -- correspond to indices in the controls array, above.
	private static final int TOP = 0;

	private static final int BOTTOM = 1;

	private static final int LEFT = 2;

	private static final int RIGHT = 3;

	private static final int NONTRIM = 4;

	private int spacing = 3;

	/**
	 * Creates a new (initially empty) trim layout.
	 */
	public TrimLayout() {
		fTrimArea = new List[4];
		trimSizes = new int[fTrimArea.length];

		for (int idx = 0; idx < fTrimArea.length; idx++) {
			fTrimArea[idx] = new ArrayList();
			trimSizes[idx] = SWT.DEFAULT;
		}
	}

	/**
	 * Sets the empty space surrounding the center area. This whitespace is
	 * located between the trim and the central widget.
	 * 
	 * @param left
	 *            pixel width
	 * @param right
	 *            pixel width
	 * @param top
	 *            pixel width
	 * @param bottom
	 *            pixel width
	 */
	public void setSpacing(int left, int right, int top, int bottom) {
		leftSpacing = left;
		rightSpacing = right;
		topSpacing = top;
		bottomSpacing = bottom;
	}

	/**
	 * Sets the empty space around the outside of the layout. This whitespace is
	 * located outside the trim widgets.
	 * 
	 * @param marginWidth
	 * @param marginHeight
	 */
	public void setMargins(int marginWidth, int marginHeight) {
		this.marginWidth = marginWidth;
		this.marginHeight = marginHeight;
	}

	/**
	 * Converts an SWT position constant into the trim area id.
	 * 
	 * @param positionConstant
	 *            one of SWT.LEFT, SWT.RIGHT, SWT.TOP, or SWT.BOTTOM
	 * @return the ID for the trim area
	 */
	private int convertSwtConstantToAreaId(int positionConstant) {
		switch (positionConstant) {
		case SWT.TOP:
			return TrimLayout.TOP;
		case SWT.BOTTOM:
			return TrimLayout.BOTTOM;
		case SWT.LEFT:
			return TrimLayout.LEFT;
		case SWT.RIGHT:
			return TrimLayout.RIGHT;
		}

		return 0;
	}

	/**
	 * Converts the trim area id into the corresponding SWT constants.
	 * 
	 * @param id
	 *            the area id
	 * @return the SWT constant
	 */
	private int convertAreaIdToSwtConstant(int id) {
		switch (id) {
		case TrimLayout.TOP:
			return SWT.TOP;
		case TrimLayout.BOTTOM:
			return SWT.BOTTOM;
		case TrimLayout.LEFT:
			return SWT.LEFT;
		case TrimLayout.RIGHT:
			return SWT.RIGHT;
		case TrimLayout.NONTRIM:
			return SWT.DEFAULT;
		}

		return 0;
	}

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
	private static void filterResizable(List input, List resizable,
			List nonResizable, boolean width) {
		Iterator iter = input.iterator();
		while (iter.hasNext()) {
			SizeCache next = (SizeCache) iter.next();

			if (isResizable(next.getControl(), width)) {
				resizable.add(next);
			} else {
				nonResizable.add(next);
			}
		}
	}

	private static boolean isResizable(Control control, boolean horizontally) {
		TrimLayoutData data = getData(control);

		if (!data.resizable) {
			return false;
		}

		if (horizontally) {
			return data.widthHint == SWT.DEFAULT;
		}

		return data.heightHint == SWT.DEFAULT;
	}

	private static TrimLayoutData getData(Control control) {
		TrimLayoutData data = (TrimLayoutData) control.getLayoutData();
		if (data == null) {
			data = defaultData;
		}

		return data;
	}

	private static Point computeSize(SizeCache toCompute, int widthHint,
			int heightHint) {
		TrimLayoutData data = getData(toCompute.getControl());

		if (widthHint == SWT.DEFAULT) {
			widthHint = data.widthHint;
		}

		if (heightHint == SWT.DEFAULT) {
			heightHint = data.heightHint;
		}

		if (widthHint == SWT.DEFAULT || heightHint == SWT.DEFAULT) {
			return toCompute.computeSize(widthHint, heightHint);
		}

		return new Point(widthHint, heightHint);
	}

	private static int getSize(SizeCache toCompute, int hint, boolean width) {
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
	 * Sets the trimSize (pixels) for the given side of the layout. If
	 * SWT.DEFAULT, then the trim size will be computed from child controls.
	 * 
	 * @param position
	 *            one of SWT.TOP, SWT.BOTTOM, SWT.LEFT, SWT.RIGHT
	 * @param size
	 */
	public void setTrimSize(int position, int size) {
		int id = convertSwtConstantToAreaId(position);

		trimSizes[id] = size;
	}

	/**
	 * Returns the location of the given trim control. For example, returns
	 * SWT.LEFT if the control is docked on the left, SWT.RIGHT if docked on the
	 * right, etc. Returns SWT.DEFAULT if the given control is not a trim
	 * control.
	 * 
	 * @param trimControl
	 *            control to query
	 * @return one of SWT.LEFT, SWT.RIGHT, SWT.TOP, SWT.BOTTOM, or SWT.DEFAULT
	 */
	public int getTrimLocation(Control trimControl) {
		return convertAreaIdToSwtConstant(getAreaId(trimControl));
	}

	/**
	 * Adds the given control to the layout's trim at the given location. This
	 * is equivalent to <code>addTrim(control, location, null)</code>.
	 * 
	 * @param control
	 *            new window trim to be added
	 * @param location
	 *            one of SWT.TOP, SWT.BOTTOM, SWT.LEFT, SWT.RIGHT
	 */
	public void addTrim(IWindowTrim control, int location) {
		addTrim(control, location, null);
	}

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
	 * @param location
	 *            one of SWT.TOP, SWT.BOTTOM, SWT.LEFT, SWT.RIGHT
	 * @param position
	 *            if null, the control will be inserted as the last trim widget
	 *            on this side of the layout. Otherwise, the control will be
	 *            inserted before the given widget.
	 */
	public void addTrim(IWindowTrim trim, int location, IWindowTrim position) {
		removeTrim(trim);

		int id = convertSwtConstantToAreaId(location);
		List list = fTrimArea[id];

		SizeCache cache = new SizeCache(trim.getControl());
		TrimDescriptor desc = new TrimDescriptor(trim, cache, id);

		insertBefore(list, desc, position);
	}

	/**
	 * Inserts the given object into the list before the specified position. If
	 * the given position is null, the object is inserted at the end of the
	 * list.
	 * 
	 * @param list
	 *            a list of {@link TrimDescriptor}
	 */
	private static void insertBefore(List list, TrimDescriptor desc,
			IWindowTrim position) {

		if (position != null) {
			int insertionPoint = 0;

			Iterator iter = list.iterator();
			while (iter.hasNext()) {
				TrimDescriptor next = (TrimDescriptor) iter.next();

				if (next.getTrim() == position) {
					break;
				}

				insertionPoint++;
			}

			list.add(insertionPoint, desc);
		} else {
			list.add(desc);
		}
	}

	/**
	 * Remove window trim from list.
	 * 
	 * @param list
	 *            a list of {@link TrimDescriptor}
	 * @param toRemove
	 *            the trim to remove.
	 */
	private static void remove(List list, IWindowTrim toRemove) {
		TrimDescriptor target = null;

		Iterator iter = list.iterator();
		while (iter.hasNext()) {
			TrimDescriptor next = (TrimDescriptor) iter.next();

			if (next.getTrim() == toRemove) {
				target = next;
				break;
			}
		}

		list.remove(target);
	}

	/**
	 * Removes the given window trim. Note that this has no effect if window
	 * trim is not our window trim.
	 * 
	 * @param toRemove
	 */
	public void removeTrim(IWindowTrim toRemove) {

		int id = getAreaId(toRemove);

		// If this isn't a trim widget.
		if (id == NONTRIM) {
			return;
		}

		remove(fTrimArea[id], toRemove);
	}

	// private int getAreaId(TrimDescriptor desc) {
	// return desc.getAreaId();
	// }

	private int getAreaId(IWindowTrim trim) {
		return getAreaId(trim.getControl());
	}

	/**
	 * Returns the area id indicating the position where this trim control is
	 * located.
	 * 
	 * @param toQuery
	 *            the control
	 * @return the area id
	 */
	private int getAreaId(Control toQuery) {
		TrimDescriptor desc = getTrimDescription(toQuery);
		if (desc == null) {
			return TrimLayout.NONTRIM;
		}
		return desc.getAreaId();
	}

	// private TrimDescriptor getTrimDescription(IWindowTrim trim) {
	// return getTrimDescription(trim.getControl());
	// }

	private TrimDescriptor getTrimDescription(Control toQuery) {
		for (int i = 0; i < fTrimArea.length; i++) {
			List area = fTrimArea[i];
			Iterator d = area.iterator();
			while (d.hasNext()) {
				TrimDescriptor desc = (TrimDescriptor) d.next();
				if (desc.getTrim().getControl() == toQuery) {
					return desc;
				}
			}
		}
		return null;
	}

	/**
	 * Removes any disposed widgets from this layout
	 */
	private void removeDisposed() {
		for (int idx = 0; idx < fTrimArea.length; idx++) {
			List ctrl = fTrimArea[idx];

			if (ctrl != null) {
				Iterator iter = ctrl.iterator();

				while (iter.hasNext()) {
					TrimDescriptor next = (TrimDescriptor) iter.next();

					Control nextControl = next.getTrim().getControl();

					if (nextControl == null || nextControl.isDisposed()
							|| next.getAreaId() != idx) {
						iter.remove();
					}
				}
			}
		}
	}

	/**
	 * Returns the size of the trim on each side of the layout
	 * 
	 * @return an array of trim sizes (pixels). See the index constants, above,
	 *         for the meaning of the indices.
	 */
	private int[] getTrimSizes(int widthHint, int heightHint) {
		int[] trimSize = new int[fTrimArea.length];

		for (int idx = 0; idx < trimSizes.length; idx++) {
			if (fTrimArea[idx].isEmpty()) {
				trimSize[idx] = 0;
			} else {
				trimSize[idx] = trimSizes[idx];
			}
		}

		if (trimSize[TOP] == SWT.DEFAULT) {
			trimSize[TOP] = maxDimension(cacheList(fTrimArea[TOP]), widthHint,
					false);
		}
		if (trimSize[BOTTOM] == SWT.DEFAULT) {
			trimSize[BOTTOM] = maxDimension(cacheList(fTrimArea[BOTTOM]),
					widthHint, false);
		}
		if (trimSize[LEFT] == SWT.DEFAULT) {
			trimSize[LEFT] = maxDimension(cacheList(fTrimArea[LEFT]),
					heightHint, true);
		}
		if (trimSize[RIGHT] == SWT.DEFAULT) {
			trimSize[RIGHT] = maxDimension(cacheList(fTrimArea[RIGHT]),
					heightHint, true);
		}
		return trimSize;
	}

	/**
	 * Takes the trim area and turns it back into an array of {@link SizeCache}.
	 * There can be more items in the return list than in the parameter list.
	 * 
	 * @param list
	 *            a list of {@link TrimDescriptor}
	 * @return a list of {@link SizeCache}
	 */
	private List cacheList(List list) {
		ArrayList result = new ArrayList(list.size());
		Iterator d = list.iterator();
		while (d.hasNext()) {
			TrimDescriptor desc = (TrimDescriptor) d.next();
			result.add(desc.getCache());
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.widgets.Layout#computeSize(org.eclipse.swt.widgets.Composite,
	 *      int, int, boolean)
	 */
	protected Point computeSize(Composite composite, int wHint, int hHint,
			boolean flushCache) {
		Point result = new Point(wHint, hHint);

		int[] trimSize = getTrimSizes(wHint, hHint);
		int horizontalTrim = trimSize[LEFT] + trimSize[RIGHT]
				+ (2 * marginWidth) + leftSpacing + rightSpacing;
		int verticalTrim = trimSize[TOP] + trimSize[BOTTOM]
				+ (2 * marginHeight) + topSpacing + bottomSpacing;

		Point innerSize = centerArea.computeSize(wHint == SWT.DEFAULT ? wHint
				: wHint - horizontalTrim, hHint == SWT.DEFAULT ? hHint : hHint
				- verticalTrim);

		if (wHint == SWT.DEFAULT) {
			result.x = innerSize.x + horizontalTrim;
		} else if (hHint == SWT.DEFAULT) {
			result.y = innerSize.y + verticalTrim;
		}

		return new Point(0, 0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.widgets.Layout#layout(org.eclipse.swt.widgets.Composite,
	 *      boolean)
	 */
	protected void layout(Composite composite, boolean flushCache) {
		removeDisposed();

		Rectangle clientArea = composite.getClientArea();

		clientArea.x += marginWidth;
		clientArea.width -= 2 * marginWidth;
		clientArea.y += marginHeight;
		clientArea.height -= 2 * marginHeight;

		int[] trimSize = getTrimSizes(clientArea.width, clientArea.height);

		int leftOfLayout = clientArea.x;
		int leftOfCenterPane = leftOfLayout + trimSize[LEFT] + leftSpacing;
		int widthOfCenterPane = clientArea.width - trimSize[LEFT]
				- trimSize[RIGHT] - leftSpacing - rightSpacing;
		int rightOfCenterPane = clientArea.x + clientArea.width
				- trimSize[RIGHT];

		int topOfLayout = clientArea.y;
		int topOfCenterPane = topOfLayout + trimSize[TOP] + topSpacing;
		int heightOfCenterPane = clientArea.height - trimSize[TOP]
				- trimSize[BOTTOM] - topSpacing - bottomSpacing;
		int bottomOfCenterPane = clientArea.y + clientArea.height
				- trimSize[BOTTOM];

		arrange(new Rectangle(leftOfLayout, topOfLayout, clientArea.width,
				trimSize[TOP]), cacheList(fTrimArea[TOP]), true, spacing);
		arrange(new Rectangle(leftOfCenterPane, bottomOfCenterPane,
				widthOfCenterPane, trimSize[BOTTOM]),
				cacheList(fTrimArea[BOTTOM]), true, spacing);
		arrange(new Rectangle(leftOfLayout, topOfCenterPane, trimSize[LEFT],
				clientArea.height - trimSize[TOP]), cacheList(fTrimArea[LEFT]),
				false, spacing);
		arrange(new Rectangle(rightOfCenterPane, topOfCenterPane,
				trimSize[RIGHT], clientArea.height - trimSize[TOP]),
				cacheList(fTrimArea[RIGHT]), false, spacing);

		if (centerArea.getControl() != null) {
			centerArea.getControl().setBounds(leftOfCenterPane,
					topOfCenterPane, widthOfCenterPane, heightOfCenterPane);
		}
	}

	/**
	 * Arranges all the given controls in a horizontal row that fills the given
	 * rectangle.
	 * 
	 * @param area
	 *            area to be filled by the controls
	 * @param caches
	 *            a list of SizeCaches for controls that will span the rectangle
	 */
	private static void arrange(Rectangle area, List caches,
			boolean horizontally, int spacing) {
		Point currentPosition = new Point(area.x, area.y);

		List resizable = new ArrayList(caches.size());
		List nonResizable = new ArrayList(caches.size());

		filterResizable(caches, resizable, nonResizable, horizontally);

		int[] sizes = new int[nonResizable.size()];

		int idx = 0;
		int used = 0;
		int hint = Geometry.getDimension(area, !horizontally);

		// Compute the sizes of non-resizable controls
		Iterator iter = nonResizable.iterator();

		while (iter.hasNext()) {
			SizeCache next = (SizeCache) iter.next();

			sizes[idx] = getSize(next, hint, horizontally);
			used += sizes[idx];
			idx++;
		}

		int available = Geometry.getDimension(area, horizontally) - used
				- spacing * (caches.size() - 1);
		idx = 0;
		int remainingResizable = resizable.size();

		iter = caches.iterator();

		while (iter.hasNext()) {
			SizeCache next = (SizeCache) iter.next();

			int thisSize;
			if (isResizable(next.getControl(), horizontally)) {
				thisSize = available / remainingResizable;
				available -= thisSize;
				remainingResizable--;
			} else {
				thisSize = sizes[idx];
				idx++;
			}

			if (horizontally) {
				next.getControl().setBounds(currentPosition.x,
						currentPosition.y, thisSize, hint);
				currentPosition.x += thisSize + spacing;
			} else {
				next.getControl().setBounds(currentPosition.x,
						currentPosition.y, hint, thisSize);
				currentPosition.y += thisSize + spacing;
			}
		}
	}

	/**
	 * Sets the widget that will occupy the central area of the layout.
	 * Typically, this will be a composite that contains the main widgetry of
	 * the application.
	 * 
	 * @param center
	 *            control that will occupy the center of the layout, or null if
	 *            none
	 */
	public void setCenterControl(Control center) {
		centerArea.setControl(center);
	}

	/**
	 * Returns the control in the center of this layout
	 * 
	 * @return the center area control.
	 */
	public Control getCenterControl() {
		return centerArea.getControl();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.layout.ICachingLayout#flush(org.eclipse.swt.widgets.Control)
	 */
	public void flush(Control dirtyControl) {
		if (dirtyControl == centerArea.getControl()) {
			centerArea.flush();
		} else {
			TrimDescriptor desc = getTrimDescription(dirtyControl);
			if (desc != null) {
				desc.flush();
			}
		}
	}
}
