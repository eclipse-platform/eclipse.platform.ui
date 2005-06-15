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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.util.Geometry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;

/**
 * Lays out the children of a Composite. One control occupies the center of the composite,
 * and any number of controls may be attached to the top, bottom, and sides. This is a
 * common layout for application windows, which typically have a central work area
 * and a number of small widgets (toolbars, status lines, etc.) attached to the edge.  
 * 
 * <p>Unlike most other SWT layouts, this layout does not require layout data to be attached
 * to each child control. Instead, member functions on the Layout are used to control
 * the positioning of controls within the layout.</p>
 * 
 * <p>The interface to this layout is intended to easily support drag-and-drop. Trim widgets
 * can be added, removed, or inserted between other existing widgets and the layout will
 * adjust accordingly. If one side of the layout contains no trim widgets, the central 
 * area will expand to reclaim the unused space.</p> 
 *
 * <p>This layout must be told about every widget that it is supposed to arrange. If the
 * composite contains additional widgets, they will not be moved by the layout and may
 * be arranged through other means.</p>
 *  
 * @since 3.0
 */
public class TrimLayout extends Layout implements ICachingLayout {

    private static final TrimLayoutData defaultData = new TrimLayoutData();

    private SizeCache centerArea = new SizeCache();

    private List[] controls;

    private int[] trimSizes;

    private int marginWidth;

    private int marginHeight;

    private int topSpacing;

    private int bottomSpacing;

    private int leftSpacing;

    private int rightSpacing;

    private Map mapPartOntoTrimData = new HashMap();

    // Position constants -- correspond to indices in the controls array, above.
    private static final int TOP = 0;

    private static final int BOTTOM = 1;

    private static final int LEFT = 2;

    private static final int RIGHT = 3;

    private static final int NONTRIM = 4;

	private int spacing = 3;

    private class TrimData {
        int controlsIndex;

        SizeCache cache;

        TrimData(int index, SizeCache cache) {
            this.cache = cache;
            this.controlsIndex = index;
        }
    }

    /**
     * Creates a new (initially empty) trim layout.
     */
    public TrimLayout() {
        controls = new List[4];
        trimSizes = new int[controls.length];

        for (int idx = 0; idx < controls.length; idx++) {
            controls[idx] = new LinkedList();
            trimSizes[idx] = SWT.DEFAULT;
        }
    }

    /**
     * Sets the empty space surrounding the center area. This whitespace is
     * located between the trim and the central widget.
     */
    public void setSpacing(int left, int right, int top, int bottom) {
        leftSpacing = left;
        rightSpacing = right;
        topSpacing = top;
        bottomSpacing = bottom;
    }

    /**
     * Sets the empty space around the outside of the layout. This whitespace
     * is located outside the trim widgets.
     * 
     * @param marginWidth
     * @param marginHeight
     */
    public void setMargins(int marginWidth, int marginHeight) {
        this.marginWidth = marginWidth;
        this.marginHeight = marginHeight;
    }

    /**
     * Converts an SWT position constant into an index in the controls array
     *  
     * @param positionConstant one of SWT.LEFT, SWT.RIGHT, SWT.TOP, or SWT.BOTTOM
     * @return an index into the controls array
     */
    private int convertSwtConstantToIndex(int positionConstant) {
        switch (positionConstant) {
        case SWT.TOP:
            return TOP;
        case SWT.BOTTOM:
            return BOTTOM;
        case SWT.LEFT:
            return LEFT;
        case SWT.RIGHT:
            return RIGHT;
        }

        return 0;
    }

    /**
     * Converts an index into the controls array into the corresponding SWT constants.
     * 
     * @param index
     * @return
     */
    private int convertIndexToSwtConstant(int index) {
        switch (index) {
        case TOP:
            return SWT.TOP;
        case BOTTOM:
            return SWT.BOTTOM;
        case LEFT:
            return SWT.LEFT;
        case RIGHT:
            return SWT.RIGHT;
        case NONTRIM:
            return SWT.DEFAULT;
        }

        return 0;
    }

    /**
     * This method separates resizable controls from non-resizable controls.
     * 
     * @param input the list of SizeCache to filter
     * @param resizable will contain resizable controls from the input list
     * @param nonResizable will contain non-resizable controls from the input list
     * @param width if true, we're interested in horizontally-resizable controls. Else we're interested in
     * vertically resizable controls
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
        } else {
            return data.heightHint == SWT.DEFAULT;
        }
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
        } else {
            return computeSize(toCompute, hint, SWT.DEFAULT).y;
        }
    }

    /**
     * Computes the maximum dimensions of controls in the given list
     * 
     * @param controls a list of SizeCaches
     * @return
     */
    private static int maxDimension(List controls, int hint, boolean width) {

        if (hint == SWT.DEFAULT) {
            int result = 0;
            Iterator iter = controls.iterator();

            while (iter.hasNext()) {
                SizeCache next = (SizeCache) iter.next();

                result = Math.max(getSize(next, SWT.DEFAULT, width), result);
            }

            return result;
        }

        List resizable = new ArrayList(controls.size());
        List nonResizable = new ArrayList(controls.size());

        filterResizable(controls, resizable, nonResizable, width);

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
     * Sets the trimSize (pixels) for the given side of the layout. If SWT.DEFAULT, then
     * the trim size will be computed from child controls.
     * 
     * @param position one of SWT.TOP, SWT.BOTTOM, SWT.LEFT, SWT.RIGHT
     * @param size
     */
    public void setTrimSize(int position, int size) {
        int idx = convertSwtConstantToIndex(position);

        trimSizes[idx] = size;
    }

    /**
     * Returns the location of the given trim control. For example, returns SWT.LEFT
     * if the control is docked on the left, SWT.RIGHT if docked on the right, etc.
     * Returns SWT.DEFAULT if the given control is not a trim control.
     * 
     * @param trimControl control to query
     * @return one of SWT.LEFT, SWT.RIGHT, SWT.TOP, SWT.BOTTOM, or SWT.DEFAULT
     */
    public int getTrimLocation(Control trimControl) {
        return convertIndexToSwtConstant(getIndex(trimControl));
    }

    /**
     * Adds the given control to the layout's trim at the given location.
     * This is equivalent to <code>addTrim(control, location, null)</code>.
     * 
     * @param control new trim widget to be added
     * @param location one of SWT.TOP, SWT.BOTTOM, SWT.LEFT, SWT.RIGHT
     */
    public void addTrim(Control control, int location) {
        addTrim(control, location, null);
    }

    /**
     * Adds the given control to the layout's trim. Note that this must be called
     * for every trim control. If the given widget is already a trim
     * widget, it will be moved to the new position. Specifying a position allows
     * a new widget to be inserted between existing trim widgets. 
     * 
     * <p>For example, this method allows the caller to say "insert this new control
     * as trim along the bottom of the layout, to the left of this existing control".</p>
     * 
     * @param control new trim widget to be added
     * @param location one of SWT.TOP, SWT.BOTTOM, SWT.LEFT, SWT.RIGHT
     * @param position if null, the control will be inserted as the last trim widget on
     * this side of the layout. Otherwise, the control will be inserted before the given
     * widget.
     */
    public void addTrim(Control control, int location, Control position) {
        removeTrim(control);

        int index = convertSwtConstantToIndex(location);
        List list = controls[index];

        SizeCache cache = new SizeCache(control);

        insertBefore(list, cache, position);

        mapPartOntoTrimData.put(control, new TrimData(index, cache));
    }

    /**
     * Inserts the given object into the list before the specified position.
     * If the given position is null, the object is inserted at the end of the list.
     * 
     * @param list a list of SizeCache
     */
    private static void insertBefore(List list, SizeCache cache,
            Control position) {

        if (position != null) {
            int insertionPoint = 0;

            Iterator iter = list.iterator();
            while (iter.hasNext()) {
                SizeCache next = (SizeCache) iter.next();

                if (next.getControl() == position) {
                    break;
                }

                insertionPoint++;
            }

            list.add(insertionPoint, cache);
        } else {
            list.add(cache);
        }
    }

    private static void remove(List list, Control toRemove) {
        SizeCache target = null;

        Iterator iter = list.iterator();
        while (iter.hasNext()) {
            SizeCache next = (SizeCache) iter.next();

            if (next.getControl() == toRemove) {
                target = next;
                break;
            }
        }

        list.remove(target);
    }

    /**
     * Removes the given trim widget. Note that this has no effect if the
     * widget is not a trim widget.
     * 
     * @param toRemove
     */
    public void removeTrim(Control toRemove) {

        int idx = getIndex(toRemove);

        // If this isn't a trim widget.
        if (idx == NONTRIM) {
            return;
        }

        remove(controls[idx], toRemove);
        mapPartOntoTrimData.remove(toRemove);
    }

    /**
     * Returns an index into the controls array, above, indicating the position
     * where this trim control is located.
     * 
     * @param toQuery
     * @return
     */
    private int getIndex(Control toQuery) {
        TrimData data = (TrimData) mapPartOntoTrimData.get(toQuery);

        if (data == null) {
            return NONTRIM;
        }

        return data.controlsIndex;
    }

    /**
     * Removes any disposed widgets from this layout
     */
    private void removeDisposed() {
        for (int idx = 0; idx < controls.length; idx++) {
            List ctrl = controls[idx];

            if (ctrl != null) {
                Iterator iter = ctrl.iterator();

                while (iter.hasNext()) {
                    SizeCache next = (SizeCache) iter.next();

                    Control nextControl = next.getControl();

                    if (nextControl.isDisposed()
                            || getIndex(nextControl) != idx) {
                        iter.remove();
                    }
                }
            }
        }
    }

    /**
     * Returns the size of the trim on each side of the layout
     * 
     * @return an array of trim sizes (pixels). See the index constants,
     * above, for the meaning of the indices. 
     */
    private int[] getTrimSizes(int widthHint, int heightHint) {
        int[] trimSize = new int[controls.length];

        for (int idx = 0; idx < trimSizes.length; idx++) {
            if (controls[idx].isEmpty()) {
                trimSize[idx] = 0;
            } else {
                trimSize[idx] = trimSizes[idx];
            }
        }

        if (trimSize[TOP] == SWT.DEFAULT) {
            trimSize[TOP] = maxDimension(controls[TOP], widthHint, false);
        }
        if (trimSize[BOTTOM] == SWT.DEFAULT) {
            trimSize[BOTTOM] = maxDimension(controls[BOTTOM], widthHint, false);
        }
        if (trimSize[LEFT] == SWT.DEFAULT) {
            trimSize[LEFT] = maxDimension(controls[LEFT], heightHint, true);
        }
        if (trimSize[RIGHT] == SWT.DEFAULT) {
            trimSize[RIGHT] = maxDimension(controls[RIGHT], heightHint, true);
        }
        return trimSize;
    }

    /* (non-Javadoc)
     * @see org.eclipse.swt.widgets.Layout#computeSize(org.eclipse.swt.widgets.Composite, int, int, boolean)
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

    /* (non-Javadoc)
     * @see org.eclipse.swt.widgets.Layout#layout(org.eclipse.swt.widgets.Composite, boolean)
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
                trimSize[TOP]), controls[TOP], true, spacing);
        arrange(new Rectangle(leftOfCenterPane, bottomOfCenterPane,
                widthOfCenterPane, trimSize[BOTTOM]), controls[BOTTOM], true, spacing);
        arrange(new Rectangle(leftOfLayout, topOfCenterPane, trimSize[LEFT],
                clientArea.height - trimSize[TOP]), controls[LEFT], false, spacing);
        arrange(new Rectangle(rightOfCenterPane, topOfCenterPane,
                trimSize[RIGHT], clientArea.height - trimSize[TOP]),
                controls[RIGHT], false, spacing);

        if (centerArea.getControl() != null) {
            centerArea.getControl().setBounds(leftOfCenterPane,
                    topOfCenterPane, widthOfCenterPane, heightOfCenterPane);
        }
    }

    private void flushCaches() {
        for (int idx = 0; idx < controls.length; idx++) {
            List ctrl = controls[idx];

            if (ctrl != null) {
                Iterator iter = ctrl.iterator();

                while (iter.hasNext()) {
                    SizeCache next = (SizeCache) iter.next();

                    next.flush();
                }
            }
        }

        centerArea.flush();
    }

    /**
     * Arranges all the given controls in a horizontal row that fills the given rectangle.
     * 
     * @param area area to be filled by the controls
     * @param controls a list of SizeCaches for controls that will span the rectangle
     */
    private static void arrange(Rectangle area, List controls,
            boolean horizontally, int spacing) {
        Point currentPosition = new Point(area.x, area.y);

        List resizable = new ArrayList(controls.size());
        List nonResizable = new ArrayList(controls.size());

        filterResizable(controls, resizable, nonResizable, horizontally);

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

        int available = Geometry.getDimension(area, horizontally) - used - spacing  * (controls.size() - 1);
        idx = 0;
        int remainingResizable = resizable.size();

        iter = controls.iterator();

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
     * Sets the widget that will occupy the central area of the layout. Typically,
     * this will be a composite that contains the main widgetry of the application.
     * 
     * @param composite control that will occupy the center of the layout, or null if none
     */
    public void setCenterControl(Control center) {
        centerArea.setControl(center);
    }

    /**
     * Returns the control in the center of this layout
     * 
     * @return
     */
    public Control getCenterControl() {
        return centerArea.getControl();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.layout.ICachingLayout#flush(org.eclipse.swt.widgets.Control)
     */
    public void flush(Control dirtyControl) {
        TrimData data = (TrimData) mapPartOntoTrimData.get(dirtyControl);

        if (data == null) {
            if (dirtyControl == centerArea.getControl()) {
                centerArea.flush();
            }
        } else {
            data.cache.flush();
        }
    }
}
