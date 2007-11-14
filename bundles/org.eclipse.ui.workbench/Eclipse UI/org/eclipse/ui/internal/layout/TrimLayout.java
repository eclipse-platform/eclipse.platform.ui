/*******************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others.
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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.PlatformUI;

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
public class TrimLayout extends Layout implements ICachingLayout, ITrimManager {

	/**
	 * Trim area ID.
	 */
	public static final Integer TOP_ID = new Integer(TOP);

	/**
	 * Trim area ID.
	 */
	public static final Integer BOTTOM_ID = new Integer(BOTTOM);

	/**
	 * Trim area ID.
	 */
	public static final Integer LEFT_ID = new Integer(LEFT);

	/**
	 * Trim area ID.
	 */
	public static final Integer RIGHT_ID = new Integer(RIGHT);

	/**
	 * Trim area ID.
	 */
	public static final Integer NONTRIM_ID = new Integer(NONTRIM);

	/**
	 * IDs for the current trim areas we support.
	 */
	private static final int[] TRIM_ID_INFO = { LEFT, RIGHT, TOP, BOTTOM };

	private SizeCache centerArea = new SizeCache();

	/**
	 * Map of TrimAreas by IDs.
	 */
	private Map fTrimArea = new HashMap();

	/**
	 * Map of TrimDescriptors by IDs.
	 */
	private Map fTrimDescriptors = new HashMap();
	
	private boolean trimLocked;

	private HashMap preferredLocationMap = new HashMap();

	/**
	 * Creates a new (initially empty) trim layout.
	 */
	public TrimLayout() {
		// Determine whether or not the trim is 'locked'
		final IPreferenceStore store = PlatformUI.getPreferenceStore();
        trimLocked = store.getBoolean(IWorkbenchPreferenceConstants.LOCK_TRIM);
		
		createTrimArea(TOP_ID, TOP_ID.toString());
		createTrimArea(BOTTOM_ID, BOTTOM_ID.toString());
		createTrimArea(LEFT_ID, LEFT_ID.toString());
		createTrimArea(RIGHT_ID, RIGHT_ID.toString());
	}

	private void createTrimArea(Integer id, String displayName) {
		TrimArea top = new TrimArea(id.intValue(), displayName);
		fTrimArea.put(id, top);
	}

	/**
	 * Returns the location of the given trim control. For example, returns
	 * SWT.LEFT if the control is docked on the left, SWT.RIGHT if docked on the
	 * right, etc. Returns SWT.DEFAULT if the given control is not a trim
	 * control.
	 * 
	 * @param trimControl
	 *            control to query
	 * @return The area ID of this control. If the control is not part of our
	 *         trim, return SWT.DEFAULT.
	 * @see #getAreaIds()
	 */
	public int getTrimAreaId(Control trimControl) {
		TrimDescriptor desc = findTrimDescription(trimControl);
		if (desc != null) {
			return desc.getAreaId();
		}
		return SWT.DEFAULT;
	}

	/**
	 * @param control
	 *            new window trim to be added
	 * @param areaId
	 *            the area ID
	 * @see #getAreaIds()
	 * @deprecated
	 */
	public void addTrim(IWindowTrim control, int areaId) {
		addTrim(areaId, control, null);
	}

	/**
	 * 
	 * @param trim
	 *            new window trim to be added
	 * @param areaId
	 *            the area ID
	 * @param beforeMe
	 *            if null, the control will be inserted as the last trim widget
	 *            on this side of the layout. Otherwise, the control will be
	 *            inserted before the given widget.
	 * @see #getAreaIds()
	 * @deprecated
	 */
	public void addTrim(IWindowTrim trim, int areaId, IWindowTrim beforeMe) {
		addTrim(areaId, trim, beforeMe);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.layout.ITrimManager#addTrim(int,
	 *      org.eclipse.ui.internal.IWindowTrim)
	 */
	public void addTrim(int areaId, IWindowTrim trim) {
		// If we're adding trim to the same side that it's
		// already on then don't change its order
		IWindowTrim insertBefore = null;
		List trimDescs = getAreaTrim(areaId);
		for (Iterator trimIter = trimDescs.iterator(); trimIter.hasNext();) {
			IWindowTrim curTrim = (IWindowTrim) trimIter.next();
			if (curTrim.getId().equals(trim.getId())) {
				if (trimIter.hasNext()) {
					insertBefore = (IWindowTrim) trimIter.next();
				}
			}
		}
		
		addTrim(areaId, trim, insertBefore);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.layout.ITrimManager#addTrim(int,
	 *      org.eclipse.ui.internal.IWindowTrim,
	 *      org.eclipse.ui.internal.IWindowTrim)
	 */
	public void addTrim(int areaId, IWindowTrim trim, IWindowTrim beforeMe) {
		TrimArea area = (TrimArea) fTrimArea.get(new Integer(areaId));
		if (area == null) {
			return;
		}
		
		// remove the trim from the current layout
		removeTrim(trim);

		// Create a new trim descriptor for the new area...
		TrimDescriptor desc = new TrimDescriptor(trim, areaId);
		
		// If the trim can be relocated then add a docking handle
		boolean isAlreadyAHandle = trim instanceof TrimToolBarBase;
		if (!trimLocked && trim.getValidSides() != SWT.NONE && !isAlreadyAHandle) {
			// Create a 'docking' handle to allow dragging the trim
			Composite dockingHandle = new TrimCommonUIHandle(this, trim, areaId);
			desc.setDockingCache(new SizeCache(dockingHandle));
		}

		// Add the trim control
		SizeCache cache = new SizeCache(trim.getControl());
		trim.getControl().setLayoutData(trim);
		desc.setCache(cache);
		
		// Add a dispose listener so we can clean up if the Client disposes the trim
		trim.getControl().addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				Control control = (Control) e.widget;
				if (control.getLayoutData() instanceof IWindowTrim) {
					IWindowTrim trim = (IWindowTrim) control.getLayoutData();
					removeTrim(trim);
					//forceLayout();
				}
			}
		});

		// Add the new descriptor to the map
		fTrimDescriptors.put(desc.getId(), desc);

		// insert before behaviour, revisited
		if (beforeMe != null) {
			TrimDescriptor beforeDesc = (TrimDescriptor) fTrimDescriptors
					.get(beforeMe.getId());
			if (beforeDesc != null && beforeDesc.getAreaId() == areaId) {
				area.addTrim(desc, beforeDesc);
			} else {
				area.addTrim(desc);
			}
		} else {
			area.addTrim(desc);
		}
	}

	/**
	 * Force a layout of the trim
	 */
	public void forceLayout() {
		removeDisposed();

		// we hack this by calling the LayoutUtil with the
		// first piece of trim that we find...(kludge!!)
		Iterator d = fTrimDescriptors.values().iterator();
		while (d.hasNext()) {
			TrimDescriptor desc = (TrimDescriptor) d.next();
			if (desc.getTrim().getControl() != null) {
				LayoutUtil.resize(desc.getTrim().getControl());
				return;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.layout.ITrimManager#removeTrim(org.eclipse.ui.internal.IWindowTrim)
	 */
	public void removeTrim(IWindowTrim toRemove) {
		TrimDescriptor desc = (TrimDescriptor) fTrimDescriptors.remove(toRemove
				.getId());
		if (desc == null) {
			return;
		}

		TrimArea area = (TrimArea) fTrimArea.get(new Integer(desc.getAreaId()));
		if (area != null) {
			area.removeTrim(desc);
			desc.getCache().getControl().setLayoutData(null);
		}

		// If we had a trim UI handle then dispose it
		if (desc.getDockingCache() != null) {
			Control ctrl = desc.getDockingCache().getControl();
			
			// KLUDGE!! we'll leak a handle rather than losing the
			// mouse capture (for now...)
			ctrl.setVisible(false);
			//ctrl.dispose();
			desc.setDockingCache(null);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.layout.ITrimManager#getTrim(java.lang.String)
	 */
	public IWindowTrim getTrim(String id) {
		TrimDescriptor desc = (TrimDescriptor) fTrimDescriptors.get(id);
		if (desc != null) {
			return desc.getTrim();
		}
		return null;
	}

	/**
	 * Removes any disposed widgets from this layout. This is still experimental
	 * code.
	 */
	private void removeDisposed() {
		Iterator a = fTrimArea.values().iterator();
		while (a.hasNext()) {
			TrimArea area = (TrimArea) a.next();
			Iterator d = area.getDescriptors().iterator();
			while (d.hasNext()) {
				TrimDescriptor desc = (TrimDescriptor) d.next();
				Control nextControl = desc.getTrim().getControl();
				if (nextControl == null || nextControl.isDisposed()) {
					// Remvoe the trim from the area's list (not the local copy)
					area.removeTrim(desc);
					
					// Remove it from the map
					fTrimDescriptors.remove(desc.getId());
				}
			}
		}
	}

	/**
	 * We -can't- determine the correct size for the shell because
	 * of it's underlying 'big lie' structure (i.e. many of the controls
	 * are created as children of the shell and then we play a games than
	 * makes them -appear- to be contained in a Control hoerarchy)
	 * and the fact that most of the
	 * UI elements don't really exist until shown (meaning that
	 * the normal 'computeSize' mechanism won't work).
	 *
	 * See bug 166619 for details but we'll keep returning the
	 * current value for legacy reasons...
	 * 
	 * @see org.eclipse.swt.widgets.Layout#computeSize(org.eclipse.swt.widgets.Composite,
	 *      int, int, boolean)
	 */
	protected Point computeSize(Composite composite, int wHint, int hHint,
			boolean flushCache) {
		return new Point(0, 0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.widgets.Layout#layout(org.eclipse.swt.widgets.Composite,
	 *      boolean)
	 */
	protected void layout(Composite composite, boolean flushCache) {
		//long startTime = System.currentTimeMillis();
		removeDisposed();

		// get the actual trim areas
		TrimArea top = (TrimArea) fTrimArea.get(TOP_ID);
		TrimArea bottom = (TrimArea) fTrimArea.get(BOTTOM_ID);
		TrimArea left = (TrimArea) fTrimArea.get(LEFT_ID);
		TrimArea right = (TrimArea) fTrimArea.get(RIGHT_ID);

		Rectangle clientArea = composite.getClientArea();

		// Determine the amount of space necessary for the trim areas 
		int trim_top = top.computeWrappedTrim(clientArea.width);
		int trim_bottom = bottom.computeWrappedTrim(clientArea.width);
		
		// The space left over after the top and bottom have been laid out
		// represents the 'fixed' dimension for the vertical trim 
		int verticalMajor = clientArea.height- (trim_top+trim_bottom);
		
		// Lay out the left/right trim areas
		int trim_left = left.computeWrappedTrim(verticalMajor);
		int trim_right = right.computeWrappedTrim(verticalMajor);
		
		// Tile the trim into the allotted space
		top.tileTrim(clientArea.x, clientArea.y, clientArea.width);
		bottom.tileTrim(clientArea.x, clientArea.height-trim_bottom, clientArea.width);
		left.tileTrim(clientArea.x, clientArea.y + trim_top, verticalMajor);
		right.tileTrim(clientArea.width-trim_right, clientArea.y + trim_top, verticalMajor);

		// Lay out the center area in to the 'leftover' space
		if (centerArea.getControl() != null) {
			Control caCtrl = centerArea.getControl();
			caCtrl.setBounds(clientArea.x+trim_left,
					clientArea.y+trim_top,
					clientArea.width - (trim_left+trim_right),
					clientArea.height - (trim_top+trim_bottom));
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
			TrimDescriptor desc = findTrimDescription(dirtyControl);
			if (desc != null) {
				desc.flush();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.layout.ITrimManager#getAreaIds()
	 */
	public int[] getAreaIds() {
		return (int[]) TRIM_ID_INFO.clone();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.layout.ITrimManager#getAreaTrim(int)
	 */
	public List getAreaTrim(int areaId) {
		TrimArea area = (TrimArea) fTrimArea.get(new Integer(areaId));
		if (area == null) {
			return Collections.EMPTY_LIST;
		}
		return area.getTrims();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.layout.ITrimManager#updateAreaTrim(int,
	 *      java.util.List, boolean)
	 */
	public void updateAreaTrim(int id, List trim, boolean removeExtra) {
		TrimArea area = (TrimArea) fTrimArea.get(new Integer(id));
		if (area == null) {
			return;
		}
		List current = area.getTrims();

		// add back the trim ... this takes care of moving it
		// from one trim area to another.
		Iterator i = trim.iterator();
		while (i.hasNext()) {
			IWindowTrim t = (IWindowTrim) i.next();
			t.dock(id);  // Ensure that the trim is properly oriented
			addTrim(id, t, null);
			current.remove(t);
		}

		if (removeExtra) {
			// if it wasn't removed from the current list, then it's extra
			// trim we don't need.
			i = current.iterator();
			while (i.hasNext()) {
				IWindowTrim t = (IWindowTrim) i.next();
				removeTrim(t);
			}
		}
	}

	/**
	 * Return a trim area rectangle.
	 * 
	 * @param window
	 *            the window that has the trim
	 * @param areaId
	 *            the side it's on
	 * @return the area rectangle.
	 * @since 3.2
	 * @see #getAreaIds()
	 */
	public Rectangle getTrimRect(Composite window, int areaId) {
		TrimArea area = getTrimArea(areaId);
		return window.getDisplay().map(window, null, area.getCurRect());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.layout.ITrimManager#getAllTrim()
	 */
	public List getAllTrim() {
		List trimList = new ArrayList(fTrimDescriptors.size());

		Iterator d = fTrimDescriptors.values().iterator();
		while (d.hasNext()) {
			TrimDescriptor desc = (TrimDescriptor) d.next();
			trimList.add(desc.getTrim());
		}

		return trimList;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.layout.ITrimManager#setTrimVisible(org.eclipse.ui.internal.IWindowTrim,
	 *      boolean)
	 */
	public void setTrimVisible(IWindowTrim trim, boolean visible) {
		TrimDescriptor desc = findTrimDescription(trim.getControl());

		if (desc != null) {
			desc.setVisible(visible);
		}
	}

	/**
	 * Find the trim descriptor for this control.
	 * 
	 * @param trim
	 *            the Control to find.
	 * @return the trim descriptor, or <code>null</code> if not found.
	 * @since 3.2
	 */
	private TrimDescriptor findTrimDescription(Control trim) {
		Iterator d = fTrimDescriptors.values().iterator();
		while (d.hasNext()) {
			TrimDescriptor desc = (TrimDescriptor) d.next();
			if (desc.getTrim().getControl() == trim) {
				return desc;
			}
			if (desc.getDockingCache() != null
					&& desc.getDockingCache().getControl() == trim) {
				return desc;
			}
		}
		return null;
	}

	/**
	 * Return the trim area associated with the given id
	 * 
	 * @param areaId
	 *            The id of the trim area to get
	 * @return The TrimArea or <code>null</code> if the id is not found
	 */
	public TrimArea getTrimArea(int areaId) {
		return (TrimArea) fTrimArea.get(new Integer(areaId));
	}

	/**
	 * Remember the persisted locations for the trim. This allows the code
	 * to site the trim in its preferred (i.e. cached) location on creation
	 * 
	 * @param areaId The id of the trim area being defined
	 * @param preferredLocations A list of trim ID's
	 */
	public void setPreferredLocations(int areaId, List preferredLocations) {
		preferredLocationMap.put(new Integer(areaId), preferredLocations);
	}
	
	/**
	 * If the given id has a cached location return its preferred side
	 * 
	 * @param trimId The id of the trim to be tested
	 * @return The areaId of a cached id or -1 if no cache info exists
	 */
	public int getPreferredArea(String trimId) {
		Iterator keyIter = preferredLocationMap.keySet().iterator();
		while (keyIter.hasNext()) {
			Integer key = (Integer) keyIter.next();
			List areaList = (List) preferredLocationMap.get(key);
			if (areaList.contains(trimId))
				return key.intValue();
		}
		
		return -1;
	}
	
	/**
	 * If the given id has a cached location return an existing trim
	 * element that it should be placed before (if any)
	 * 
	 * @param trimId The id of the trim to be tested
	 * @return The trim to be inserted before or <code>null</code>
	 * if no cached info exists
	 */
	public IWindowTrim getPreferredLocation(String trimId) {
		Iterator keyIter = preferredLocationMap.keySet().iterator();
		while (keyIter.hasNext()) {
			Integer key = (Integer) keyIter.next();
			List areaList = (List) preferredLocationMap.get(key);
			int index = areaList.indexOf(trimId);
			if (index != -1) {
				// OK, find the first 'real' trim after this one
				// This will be used as the 'beforeMe' parameter
				// in the 'addTrim' call
				for (int i = index+1; i < areaList.size(); i++) {
					String id = (String) areaList.get(i);
					IWindowTrim trim = getTrim(id);
					if (trim != null)
						return trim;
				}
			}
		}
		
		return null;
	}

	/**
	 * Disables the controls associated with visible trim elements. This
	 * is only used during long-running WorkbenchWindow operations to prevent users
	 * from changing the environment while the operation (i.e. a long-running editor
	 * 'save') is in progress.
	 * 
	 * The expected life-cycle is to first call this this method to disable any visible
	 * trim (and caching the elements that had to be disabled) followed by a call to
	 * 'enableTrim' passing in the list returned from this method.
	 * 
	 * @param ignoreMe Since the current UI has a disable button in the StatusLine
	 * we allow the caller to designate one piece of trim to ignore
	 * 
	 * @return The list of trim controls that were disabled during this call
	 */
	public List disableTrim(IWindowTrim ignoreMe) {
		List disabledControls = new ArrayList();

		// Disable all the trim -except- for 'ignoreMe'
		List allTrim = getAllTrim();
		for (Iterator trimIter = allTrim.iterator(); trimIter.hasNext();) {
			IWindowTrim trim = (IWindowTrim) trimIter.next();
			if (ignoreMe == trim)
				continue;
			
			Control ctrl = trim.getControl();
			if (ctrl == null || ctrl.isDisposed() || !ctrl.isVisible() || !ctrl.isEnabled())
				continue;
			
			ctrl.setEnabled(false);
			disabledControls.add(ctrl);
		}
		
		return disabledControls;
	}
	
	/**
	 * Enables the controls in the list. This list is expected to be a non-modified
	 * List as returned from a call to 'disableTrim'.
	 * @param disabledControls The list of controls to enable
	 */
	public void enableTrim(List disabledControls) {
		// Simply re-enable any controls in the list
		for (Iterator dcIter = disabledControls.iterator(); dcIter.hasNext();) {
			Control ctrl = (Control) dcIter.next();
			
			if (!ctrl.isDisposed() && !ctrl.isEnabled())
				ctrl.setEnabled(true);
		}
	}
}
