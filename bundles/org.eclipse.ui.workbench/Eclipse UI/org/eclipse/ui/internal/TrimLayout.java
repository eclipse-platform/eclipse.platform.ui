/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

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
 * area will expand to reclaim the unused space</p> 
 *
 * <p>This layout must be told about every widget that it is supposed to arrange. If the
 * composite contains additional widgets, they will not be moved by the layout and may
 * be arranged through other means.</p>
 *  
 */
class TrimLayout extends Layout {

	private Control centerArea;
	
	private List[] controls;
	private int[] trimSizes;
	
	private int marginWidth;
	private int marginHeight;
	
	private int topSpacing;
	private int bottomSpacing;
	private int leftSpacing;
	private int rightSpacing;
	
	// Position constants -- correspond to indices in the controls array, above.
	private static final int TOP = 0;
	private static final int BOTTOM = 1;
	private static final int LEFT = 2;
	private static final int RIGHT = 3;
	private static final int NONTRIM = 4;
	
	private class TrimLayoutData {
		TrimLayoutData(int idx, int widthHint, int heightHint) {
			positionIndex = idx;
			this.widthHint = widthHint;
			this.heightHint = heightHint;
		}
		
		/**
		 * See the position constants, above. Corresponds to an index in the
		 * controls array.
		 */
		int positionIndex;
		
		int widthHint = SWT.DEFAULT;
		int heightHint = SWT.DEFAULT;
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
	 * 
	 * @param horizontalSpacing
	 * @param verticalSpacing
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
		switch(positionConstant) {
		case SWT.TOP: return TOP;
		case SWT.BOTTOM: return BOTTOM;
		case SWT.LEFT: return LEFT;
		case SWT.RIGHT: return RIGHT;
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
		switch(index) {
			case TOP: return SWT.TOP;
			case BOTTOM: return SWT.BOTTOM;
			case LEFT: return SWT.LEFT;
			case RIGHT: return SWT.RIGHT;
			case NONTRIM: return SWT.DEFAULT;
		}
		
		return 0;
	}
	
	/**
	 * Computes the maximum dimensions of controls in the given list
	 * 
	 * @param controls
	 * @return
	 */
	private static Point maxDimensions(List controls, int widthHint, int heightHint) {
		
		int individualWidthHint = (widthHint == SWT.DEFAULT) ? SWT.DEFAULT : widthHint / controls.size();
		int individualHeightHint = (heightHint == SWT.DEFAULT) ? SWT.DEFAULT : heightHint / controls.size();
		
		Point result = new Point(0,0);
		Iterator iter = controls.iterator();
		
		while (iter.hasNext()) {
			Control next = (Control)iter.next();
			
			Point dimension = next.computeSize(individualWidthHint, individualHeightHint);
			
			result.x = Math.max(result.x, dimension.x);
			result.y = Math.max(result.y, dimension.y);
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
		addTrim(control, location, position, SWT.DEFAULT, SWT.DEFAULT);
	}
	
	/**
	 * Adds the given control to the layout's trim. The width and height hints may be used to
	 * override the control's preferred size in one or both directions.
	 * 
	 * @param control new trim widget to be added
	 * @param location one of SWT.TOP, SWT.BOTTOM, SWT.LEFT, SWT.RIGHT
	 * @param position if null, the control will be inserted as the last trim widget on
	 * this side of the layout. Otherwise, the control will be inserted before the given
	 * widget.
	 * @param widthHint specifies the width of the control or SWT.DEFAULT to ask the control
	 * for its preferred size.
	 * @param heightHint specifies the height of the control or SWT.DEFAULT to ask the control
	 * for its preferred size.
	 */
    public void addTrim(Control control, int location, Control position, int widthHint, int heightHint) {
		removeTrim(control);
		
		int index = convertSwtConstantToIndex(location);
		List list = controls[index];
		
		insertBefore(list, control, position);
		
		control.setLayoutData(new TrimLayoutData(index, widthHint, heightHint));
	}
	
	/**
	 * Inserts the given object into the list before the specified position.
	 * If the given position is null, the object is inserted at the end of the list.
	 */
	private static void insertBefore(List list, Object toInsert, Object position) {
		int insertIndex = -1;
		if (position != null) {
			insertIndex = list.indexOf(position);
		}
		if (insertIndex != -1) {
			list.add(insertIndex, toInsert);
		} else {
			list.add(toInsert);
		}
	}
	
	/**
	 * Removes the given trim widget. Note that this has no effect if the
	 * widget is not a trim widget.
	 * 
	 * @param toRemove
	 */
	public void removeTrim(Control toRemove) {
		// If this isn't a trim widget.
		if (toRemove.getLayoutData() == null) {
			return;
		}
		
		int idx = getIndex(toRemove);
		
		controls[idx].remove(toRemove);
		toRemove.setLayoutData(null);
	}
	
	/**
	 * Returns an index into the controls array, above, indicating the position
	 * where this trim control is located.
	 * 
	 * @param toQuery
	 * @return
	 */
	private int getIndex(Control toQuery) {
		TrimLayoutData data = (TrimLayoutData)toQuery.getLayoutData();
		
		if (data == null) {
			return NONTRIM;
		}
		
		return data.positionIndex;
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
					Control next = (Control)iter.next();
					
					if (next.isDisposed() || getIndex(next) != idx) {
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
			trimSize[TOP] = maxDimensions(controls[TOP], widthHint, SWT.DEFAULT).y;
		}
		if (trimSize[BOTTOM] == SWT.DEFAULT) {	
			trimSize[BOTTOM] = maxDimensions(controls[BOTTOM], widthHint, SWT.DEFAULT).y;
		}
		if (trimSize[LEFT] == SWT.DEFAULT) {
			trimSize[LEFT] = maxDimensions(controls[LEFT], SWT.DEFAULT, heightHint).x;
		}
		if (trimSize[RIGHT] == SWT.DEFAULT) {
			trimSize[RIGHT] = maxDimensions(controls[RIGHT], SWT.DEFAULT, heightHint).x;
		}		
		return trimSize;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Layout#computeSize(org.eclipse.swt.widgets.Composite, int, int, boolean)
	 */
	protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
		Point result = new Point(wHint, hHint);
		
		int[] trimSize = getTrimSizes(wHint, hHint);
		int horizontalTrim = trimSize[LEFT] + trimSize[RIGHT] + (2 * marginWidth) + leftSpacing + rightSpacing;
		int verticalTrim = trimSize[TOP] + trimSize[BOTTOM] + (2 * marginHeight) + topSpacing + bottomSpacing;
				
		Point innerSize;
		if (centerArea == null) {
			innerSize = new Point(0,0);
		} else {
			innerSize = centerArea.computeSize(
					wHint == SWT.DEFAULT ? wHint : wHint - horizontalTrim,
					hHint == SWT.DEFAULT ? hHint: hHint - verticalTrim);
		}
		
		if (wHint == SWT.DEFAULT) {
			result.x = innerSize.x + horizontalTrim;
		} else if (hHint == SWT.DEFAULT) {
			result.y = innerSize.y + verticalTrim;
		}
		
		return new Point(0,0);
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
		int widthOfCenterPane = clientArea.width - trimSize[LEFT] - trimSize[RIGHT] - leftSpacing - rightSpacing;
		int rightOfCenterPane = clientArea.x + clientArea.width - trimSize[RIGHT];
		
		int topOfLayout = clientArea.y;
		int topOfCenterPane = topOfLayout + trimSize[TOP] + topSpacing;
		int heightOfCenterPane = clientArea.height - trimSize[TOP] - trimSize[BOTTOM] - topSpacing - bottomSpacing;
		int bottomOfCenterPane = clientArea.y + clientArea.height - trimSize[BOTTOM];
		
		arrangeHorizontally(new Rectangle(leftOfLayout, topOfLayout, clientArea.width, trimSize[TOP]), controls[TOP]);
		arrangeHorizontally(new Rectangle(leftOfCenterPane, bottomOfCenterPane, widthOfCenterPane, trimSize[BOTTOM]), controls[BOTTOM]);
		arrangeVertically(new Rectangle(leftOfLayout, topOfCenterPane, trimSize[LEFT], clientArea.height - trimSize[TOP]), controls[LEFT]);
		arrangeVertically(new Rectangle(rightOfCenterPane, topOfCenterPane, trimSize[RIGHT], clientArea.height - trimSize[TOP]), controls[RIGHT]);
		
		if (centerArea != null) {
			centerArea.setBounds(leftOfCenterPane, topOfCenterPane, widthOfCenterPane, heightOfCenterPane);
		}
	}

	/**
	 * Arranges all the given controls in a horizontal row that fills the given rectangle.
	 * 
	 * @param area area to be filled by the controls
	 * @param controls controls that will span the rectangle
	 */
	private static void arrangeHorizontally(Rectangle area, List controls) {
		Point currentPosition = new Point(area.x, area.y);
		
		Iterator iter = controls.iterator();
		
		while (iter.hasNext()) {
			Control next = (Control)iter.next();

			if (iter.hasNext()) {

				TrimLayoutData data = (TrimLayoutData)next.getLayoutData();
				
				int width = data.widthHint;
				if (width == SWT.DEFAULT) {	
					width = next.computeSize(SWT.DEFAULT, area.height).x;
				}
				
				next.setBounds(currentPosition.x, currentPosition.y, width, area.height);

				currentPosition.x += width;
				
			} else {
				next.setBounds(currentPosition.x, currentPosition.y, area.width - currentPosition.x + area.x, area.height);
			}			
		}
	}

	/**
	 * Arranges all the given controls in a horizontal row that fills the given rectangle
	 * 
	 * @param area area to be filled by the controls
	 * @param controls controls that will span the rectangle
	 */
	private static void arrangeVertically(Rectangle area, List controls) {
		Point currentPosition = new Point(area.x, area.y);
		
		Iterator iter = controls.iterator();
		
		while (iter.hasNext()) {
			Control next = (Control)iter.next();
		
			if (iter.hasNext()) {
				TrimLayoutData data = (TrimLayoutData)next.getLayoutData();
				
				int height = data.heightHint;
				if (height == SWT.DEFAULT) {
					height = next.computeSize(area.width, SWT.DEFAULT).y;
				}
			
				next.setBounds(currentPosition.x, currentPosition.y, area.width, height);

				currentPosition.y += height;
				
			} else {
				next.setBounds(currentPosition.x, currentPosition.y, area.width, area.height 
						- currentPosition.y + area.y);
			}
		}
	}

	/**
	 * Sets the widget that will occupy the central area of the layout. Typically,
	 * this will be a composite that contains the main widgetry of the application.
	 * 
	 * @param composite control that will occupy the center of the layout.
	 */
	public void setCenterControl(Control center) {
		centerArea = center;
	}
	
	/**
	 * Returns the control in the center of this layout
	 * 
	 * @return
	 */
	public Control getCenterControl() {
		return centerArea;
	}
}
