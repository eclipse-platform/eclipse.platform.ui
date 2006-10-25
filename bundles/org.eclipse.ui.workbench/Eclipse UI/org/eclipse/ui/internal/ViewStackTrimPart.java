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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.util.Geometry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.internal.presentations.PresentablePart;
import org.eclipse.ui.internal.presentations.util.TabbedStackPresentation;
import org.eclipse.ui.presentations.IPresentablePart;

/**
 * A trim element representing a ViewStack in the trim.
 * <p>
 * The placeholder must be non-null and its
 * 'real' container must be a <code>ViewStack</code>.
 * </p>
 * 
 * @since 3.3
 *
 */
public class ViewStackTrimPart extends TrimPart {	
	ContainerPlaceholder placeHolder;	
	ViewStack stack;
	
	// The orientation of the fast view pane when showing a view
	int paneOrientation;
	
	/**
	 * Construct a new trim element for the given ViewStack
	 * 
	 * @param window The window hosting the trim
	 * @param placeHolder The placeholder who's real container is
	 * the ViewStack being represented. 
	 */
	public ViewStackTrimPart(WorkbenchWindow window, ContainerPlaceholder placeHolder) {
		super(window, placeHolder.getRealContainer());

		setPlaceholder(placeHolder);

		// Set the display orientation based on the stack's geometry
    	Rectangle stackBounds = stack.getBounds();
    	paneOrientation = (stackBounds.width > stackBounds.height) ? SWT.HORIZONTAL : SWT.VERTICAL;
	}   	

	/**
	 * Sets the current placeholder. This is used by the ViewStack
	 * when its trim state changes, causing it to create a new placeholder.
	 * 
	 * @param placeHolder the current placeholder
	 */
	public void setPlaceholder(ContainerPlaceholder placeHolder) {
		this.placeHolder = placeHolder;
		this.stack = (ViewStack) placeHolder.getRealContainer();
	}

	/**
	 * Add a button for every view reference in the stack being
	 * represented.
	 */
	protected void addItems() {
		List orderedViews = getTrueViewOrder();
		if (orderedViews.size() == 0)
			return;
		
        // Add a button for each view reference in the stack
		for (Iterator iterator = orderedViews.iterator(); iterator.hasNext();) {
			IViewReference ref = (IViewReference) iterator.next();

			// Set up the item's 'look'
			ToolItem viewButton = new ToolItem(toolBar, SWT.CHECK);        
			viewButton.setImage(ref.getTitleImage());       
	        viewButton.setToolTipText(ref.getTitle());
	        viewButton.addSelectionListener(new SelectionListener() {
				public void widgetDefaultSelected(SelectionEvent e) {
					showView(e);
				}
				public void widgetSelected(SelectionEvent e) {
					showView(e);
				}
	        });
	        
	        viewButton.setData(ref);
		}
	}

	/**
	 * Returns the 'side' to place the current fast view on. This should
	 * really be changed...
	 * 
	 * @return The side (calc'd a la the FastViewBar:getViewSide method)
	 */
	public int getPaneOrientation() {
		// determine where the bar is in relation to the workbench window
		Rectangle tsBounds = getControl().getBounds();
		Rectangle wbBounds = getControl().getShell().getBounds();
		
	    if (paneOrientation == SWT.HORIZONTAL) {
	    	if (curSide == SWT.LEFT || curSide == SWT.RIGHT) {
	    		// is the trim bar nearer to the top or the bottom?
	    		if (Geometry.centerPoint(tsBounds).y < Geometry.centerPoint(wbBounds).y)
	    			return SWT.TOP;

	    		return SWT.BOTTOM;
	    	}

    		return curSide;
	    }
	
	    // Vertical
		if (curSide == SWT.TOP || curSide == SWT.BOTTOM) {
			// is the trim bar nearer to the left or the right?
			if (Geometry.centerPoint(tsBounds).x < Geometry.centerPoint(wbBounds).x)
				return SWT.LEFT;

			return SWT.RIGHT;
		}

		return curSide;
	}

	/**
	 * Show the selected view as a fast view.
	 * 
	 * @param e The event causing the view to be shown
	 * 
	 */
	protected void showView(SelectionEvent e) {
		Perspective persp = window.getActiveWorkbenchPage().getActivePerspective();
		ToolItem item = (ToolItem) e.getSource();
		IViewReference ref = (IViewReference) item.getData();
		persp.toggleFastView(ref);
	}

	/**
	 * @return a List of <code>IViewReference</code>
	 * sorted into the order in which they appear in the
	 * visual stack.
	 */
	private List getTrueViewOrder() {
		List orderedViews = new ArrayList();
		if (stack.getPresentation() instanceof TabbedStackPresentation) {
			TabbedStackPresentation tsp = (TabbedStackPresentation) stack.getPresentation();
			
			// KLUDGE!! uses a 'testing only' API
			IPresentablePart[] parts = tsp.getPartList();
			for (int i = 0; i < parts.length; i++) {
				if (parts[i] instanceof PresentablePart) {
					PresentablePart part = (PresentablePart) parts[i];
					IWorkbenchPartReference ref = part.getPane().getPartReference();
					if (ref instanceof IViewReference)
						orderedViews.add(ref);		
				}
			}
		}
		
		return orderedViews;
	}
	
	/**
	 * @param refToFind The reference being checked for
	 * @return true if this stack owns the reference.
	 */
	public boolean hasViewRef(IViewReference refToFind) {
		ToolItem[] items = toolBar.getItems();
		for (int i = 0; i < items.length; i++) {
			IViewReference ref = (IViewReference) items[i].getData();
			if (ref == refToFind)
				return true;
		}
		
		return false;
	}

	/**
	 * @return The list of all view references in the stack
	 */
	public List getViewRefs() {
		List refs = new ArrayList(toolBar.getItemCount());
		ToolItem[] items = toolBar.getItems();
		for (int i = 0; i < items.length; i++) {
			Object data = items[i].getData();
			if (data instanceof IViewReference)
				refs.add(data);
		}
		
		return refs;
	}
	
	/**
	 * @return The side that the fast view pane should be attached to
	 * based on the position of the trim element.
	 */
	public int getViewSide() {
        if (paneOrientation == SWT.HORIZONTAL) {
        	if (curSide == SWT.TOP || curSide == SWT.BOTTOM)
        		return curSide;
        	
        	return SWT.TOP;
        }
        
        // Vertical
        if (curSide == SWT.LEFT || curSide == SWT.RIGHT)
        	return curSide;
        
        // Are we on the left or right 'end' of the trim area?
        Point trimCenter = Geometry.centerPoint(getControl().getBounds());
        Point shellCenter = Geometry.centerPoint(getControl().getShell().getClientArea());
        return (trimCenter.x < shellCenter.x) ? SWT.LEFT : SWT.RIGHT;
	}

	/**
	 * @return the current placeholder
	 */
	public ContainerPlaceholder getPlaceholder() {
		return placeHolder;
	}

	/**
	 * This is used by the Perspective to match the icon's
	 * state to the state of the active fast view.
	 *  
	 * @param refToSet The view reference to set the state of
	 * @param selected The new state for the icon
	 */
	public void setIconSelection(IViewReference refToSet, boolean selected) {
		ToolItem[] items = toolBar.getItems();
		for (int i = 0; i < items.length; i++) {
			IViewReference ref = (IViewReference) items[i].getData();
			if (ref == refToSet)
				items[i].setSelection(selected);
		}
	}
}
