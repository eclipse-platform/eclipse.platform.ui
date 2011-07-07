/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.breakpoints;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.IBreakpointsListener;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.internal.core.IInternalDebugCoreConstants;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.importexport.breakpoints.IImportExportConstants;
import org.eclipse.debug.ui.AbstractBreakpointOrganizerDelegate;
import org.eclipse.debug.ui.IBreakpointOrganizerDelegateExtension;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;

/**
 * Breakpoint organizers for breakpoint working sets.
 * 
 * @since 3.1
 */
public class BreakpointSetOrganizer extends AbstractBreakpointOrganizerDelegate implements IBreakpointOrganizerDelegateExtension, IPropertyChangeListener, IBreakpointsListener {

	private IWorkingSetManager fWorkingSetManager = PlatformUI.getWorkbench().getWorkingSetManager();
	
	/**
	 * A cache for mapping markers to the working set they belong to
	 * @since 3.2
	 */
	private BreakpointWorkingSetCache fCache = null;
	
	// Cache of the default working set, so we can know when it changes name
	private static IWorkingSet fDefaultWorkingSet = null;
	
	
	/**
	 * Constructs a working set breakpoint organizer. Listens for changes in
	 * working sets and fires property change notification.
	 */
	public BreakpointSetOrganizer() {
		fWorkingSetManager.addPropertyChangeListener(this);
		fCache = new BreakpointWorkingSetCache();
		DebugUIPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(this);
		DebugPlugin.getDefault().getBreakpointManager().addBreakpointListener(this);
		fDefaultWorkingSet = getDefaultWorkingSet();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.IBreakpointOrganizerDelegate#getCategories(org.eclipse.debug.core.model.IBreakpoint)
	 */
	public IAdaptable[] getCategories(IBreakpoint breakpoint) {
		List result = new ArrayList();
		IWorkingSet[] workingSets = fWorkingSetManager.getWorkingSets();
		for (int i = 0; i < workingSets.length; i++) {
			IWorkingSet set = workingSets[i];
			if (IDebugUIConstants.BREAKPOINT_WORKINGSET_ID.equals(set.getId())) {
				IAdaptable[] elements = set.getElements();
				for (int j = 0; j < elements.length; j++) {
					IAdaptable adaptable = elements[j];
					if (adaptable.equals(breakpoint)) {
						result.add(new WorkingSetCategory(set));
						break;
					}
				}
			}
		}
		return (IAdaptable[]) result.toArray(new IAdaptable[result.size()]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.IBreakpointOrganizerDelegate#dispose()
	 */
	public void dispose() {
		fWorkingSetManager.removePropertyChangeListener(this);
		fWorkingSetManager = null;
		DebugPlugin.getDefault().getBreakpointManager().removeBreakpointListener(this);
		DebugUIPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(this);
		super.dispose();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		String property = event.getProperty();
		if (property.equals(IInternalDebugUIConstants.MEMENTO_BREAKPOINT_WORKING_SET_NAME)) {
			IWorkingSet defaultWorkingSet = getDefaultWorkingSet();
			if (defaultWorkingSet != null) {
				fireCategoryChanged(new WorkingSetCategory(defaultWorkingSet));
			} else {
				fireCategoryChanged(null);
			}
		}

		IWorkingSet set = null;
		Object newValue = event.getNewValue();
		if (newValue instanceof IWorkingSet) {
			set = (IWorkingSet) newValue;
		}
		else if (event.getOldValue() instanceof IWorkingSet) {
			set = (IWorkingSet) event.getOldValue();
		}
		if(set == null) {
			return;
		}
		//fix for bug 103731
		if (property.equals(IWorkingSetManager.CHANGE_WORKING_SET_NAME_CHANGE)) {
			if (newValue.equals(fDefaultWorkingSet)) {
				setDefaultWorkingSet((IWorkingSet) newValue);
			}
		}
		if (property.equals(IWorkingSetManager.CHANGE_WORKING_SET_REMOVE)) {
			if (event.getOldValue().equals(fDefaultWorkingSet)) {
				setDefaultWorkingSet(null);
			}
		}
		if(property.equals(IWorkingSetManager.CHANGE_WORKING_SET_ADD)) {
			IAdaptable[] breakpoints = set.getElements();
			for (int i = 0; i < breakpoints.length; i++) {
				if (breakpoints[i] instanceof IBreakpoint) {
					IMarker marker = ((IBreakpoint)breakpoints[i]).getMarker();
					fCache.addEntry(marker, set.getName());
					fCache.flushMarkerCache(marker);
				}
			}
		}
		if (set != null	&& IDebugUIConstants.BREAKPOINT_WORKINGSET_ID.equals(set.getId())) {
			fireCategoryChanged(new WorkingSetCategory(set));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.IBreakpointsListener#breakpointsAdded(org.eclipse.debug.core.model.IBreakpoint[])
	 */
	public void breakpointsAdded(IBreakpoint[] breakpoints) {
		Map setToBreakpoints = new HashMap();
		for (int i = 0; i < breakpoints.length; i++) {
			IMarker marker = breakpoints[i].getMarker();
			String[] names = getWorkingsetAttributeFromMarker(marker, IInternalDebugUIConstants.WORKING_SET_NAME);
			//add it to the default set if the listing is empty
			if (names.length == 0) {
				queueToSet(breakpoints[i], getDefaultWorkingSet(), setToBreakpoints);
			} else {
				for (int j = 1; j < names.length; j++) {
					IWorkingSet set = PlatformUI.getWorkbench().getWorkingSetManager().getWorkingSet(names[j]);
					// if we cannot find the one we want, try to get the default
					if (set == null) {
						set = getDefaultWorkingSet();
					}
					queueToSet(breakpoints[i], set, setToBreakpoints);
				}
			}
		}
		Iterator iterator = setToBreakpoints.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry entry = (Entry) iterator.next();
			IWorkingSet set = (IWorkingSet) entry.getKey();
			List list = (List) entry.getValue();
			addBreakpointsToSet((IBreakpoint[]) list.toArray(new IBreakpoint[list.size()]), set);
		}
	}
	
	private void queueToSet(IBreakpoint breakpoint, IWorkingSet set, Map queue) {
		List list = (List) queue.get(set);
		if (list == null) {
			list = new ArrayList();
			queue.put(set, list);
		}
		list.add(breakpoint);
	}
	
	/**
	 * Adds a breakpoint to a working set
	 * @param breakpoints the breakpoints to add
	 * @param set the set to add it to or <code>null</code> if none
	 * 
	 * @since 3.2
	 */
	private void addBreakpointsToSet(IBreakpoint[] breakpoints, IWorkingSet set) {
		if (set != null) {
			IAdaptable[] elements = set.getElements();
			Set collection = new HashSet(elements.length);
			List list = new ArrayList(elements.length + breakpoints.length);
			for(int i = 0; i < elements.length; i++) {
				collection.add(elements[i]);
				list.add(elements[i]);
			}
			for (int i = 0; i < breakpoints.length; i++) {
				IBreakpoint breakpoint = breakpoints[i];
				if (!collection.contains(breakpoint)) {
					list.add(breakpoint);
					fCache.addEntry(breakpoint.getMarker(), set.getName()); //fix for bug 103731	
					fCache.flushMarkerCache(breakpoint.getMarker());
				}
			}
			set.setElements((IAdaptable[]) list.toArray(new IAdaptable[list.size()]));
		}
	}	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.IBreakpointsListener#breakpointsRemoved(org.eclipse.debug.core.model.IBreakpoint[],
	 *      org.eclipse.core.resources.IMarkerDelta[])
	 */
	public void breakpointsRemoved(IBreakpoint[] breakpoints,
			IMarkerDelta[] deltas) {
		IWorkingSet[] workingSets = fWorkingSetManager.getWorkingSets();
		IWorkingSet set = null;
		for (int i = 0; i < workingSets.length; i++) {
			set = workingSets[i];
			if (IDebugUIConstants.BREAKPOINT_WORKINGSET_ID.equals(set.getId())) {
				clean(set);
			}
		}
	}

	/**
	 * Removes deleted breakpoints from the given working set.
	 * 
	 * @param workingSet
	 *            breakpoint working set
	 */
	private void clean(IWorkingSet workingSet) {
		IAdaptable[] elements = workingSet.getElements();
		IBreakpointManager manager = DebugPlugin.getDefault().getBreakpointManager();
		boolean update = false;
		for (int i = 0; i < elements.length; i++) {
			IAdaptable adaptable = elements[i];
			if (adaptable instanceof IBreakpoint) {
				IBreakpoint breakpoint = (IBreakpoint) adaptable;
				if (!manager.isRegistered(breakpoint)) {
					update = true;
					elements[i] = null;
				}
			}
		}
		if (update) {
			List newElements = new ArrayList(elements.length);
			for (int i = 0; i < elements.length; i++) {
				IAdaptable adaptable = elements[i];
				if (adaptable != null) {
					newElements.add(adaptable);
				}
			}
			workingSet.setElements((IAdaptable[]) newElements.toArray(new IAdaptable[newElements.size()]));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.IBreakpointsListener#breakpointsChanged(org.eclipse.debug.core.model.IBreakpoint[],
	 *      org.eclipse.core.resources.IMarkerDelta[])
	 */
	public void breakpointsChanged(IBreakpoint[] breakpoints, IMarkerDelta[] deltas) {
	}

	/**
	 * Returns the active default breakpoint working set, or <code>null</code>
	 * if none.
	 * 
	 * @return the active default breakpoint working set, or <code>null</code>
	 */
	public static IWorkingSet getDefaultWorkingSet() {
		IPreferenceStore preferenceStore = DebugUIPlugin.getDefault().getPreferenceStore();
		String name = preferenceStore.getString(IInternalDebugUIConstants.MEMENTO_BREAKPOINT_WORKING_SET_NAME);
		if (name != null) {
			return PlatformUI.getWorkbench().getWorkingSetManager().getWorkingSet(name);
		}
		return null;
	}

	/**
	 * Sets the active default breakpoint working set, or <code>null</code> if
	 * none.
	 * 
	 * @param set
	 *            default working set or <code>null</code>
	 */
	public static void setDefaultWorkingSet(IWorkingSet set) {
		String name = IInternalDebugCoreConstants.EMPTY_STRING;
		if (set != null) {
			// only consider breakpoint working sets
			if (IDebugUIConstants.BREAKPOINT_WORKINGSET_ID.equals(set.getId())) {
				name = set.getName();
			}
		}
		fDefaultWorkingSet = set;
		DebugUIPlugin.getDefault().getPluginPreferences().setValue(IInternalDebugUIConstants.MEMENTO_BREAKPOINT_WORKING_SET_NAME, name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.IBreakpointOrganizerDelegate#canRemove(org.eclipse.debug.core.model.IBreakpoint,
	 *      org.eclipse.core.runtime.IAdaptable)
	 */
	public boolean canRemove(IBreakpoint breakpoint, IAdaptable category) {
		if (category instanceof WorkingSetCategory) {
			WorkingSetCategory wsc = (WorkingSetCategory) category;
			return IDebugUIConstants.BREAKPOINT_WORKINGSET_ID.equals(wsc.getWorkingSet().getId());
		}
		return super.canRemove(breakpoint, category);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.IBreakpointOrganizerDelegate#canAdd(org.eclipse.debug.core.model.IBreakpoint,
	 *      org.eclipse.core.runtime.IAdaptable)
	 */
	public boolean canAdd(IBreakpoint breakpoint, IAdaptable category) {
		if (category instanceof WorkingSetCategory) {
			WorkingSetCategory wsc = (WorkingSetCategory) category;
			return IDebugUIConstants.BREAKPOINT_WORKINGSET_ID.equals(wsc.getWorkingSet().getId());
		}
		return super.canAdd(breakpoint, category);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.IBreakpointOrganizerDelegate#addBreakpoint(org.eclipse.debug.core.model.IBreakpoint,
	 *      org.eclipse.core.runtime.IAdaptable)
	 */
	public void addBreakpoint(IBreakpoint breakpoint, IAdaptable category) {
		addBreakpoints(new IBreakpoint[]{breakpoint}, category);
	}
	
	/**
	 * Gets the working set names from the marker
	 * 
	 * @param marker them marker to get the names from
	 * @param type the type attribute to look up
	 * @return the listing of markers or an empty String array, never <code>null</code>
	 * 
	 * @since 3.2
	 */
	private String[] getWorkingsetAttributeFromMarker(IMarker marker, String type) {
		try {
			String name = (String) marker.getAttribute(type);
			if (name != null) {
				return name.split("\\" + IImportExportConstants.DELIMITER); //$NON-NLS-1$
			}
		}
		catch (CoreException e) {DebugPlugin.log(e);}
		return new String[] {};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.IBreakpointOrganizerDelegate#removeBreakpoint(org.eclipse.debug.core.model.IBreakpoint,
	 *      org.eclipse.core.runtime.IAdaptable)
	 */
	public void removeBreakpoint(IBreakpoint breakpoint, IAdaptable category) {
		if (category instanceof WorkingSetCategory) {
			IWorkingSet set = ((WorkingSetCategory) category).getWorkingSet();
			IAdaptable[] elements = set.getElements();
			List list = new ArrayList();
			for (int i = 0; i < elements.length; i++) {
				IAdaptable adaptable = elements[i];
				if (!adaptable.equals(breakpoint)) {
					list.add(adaptable);
				}
			}
			fCache.removeMappedEntry(breakpoint.getMarker(), set.getName());
			fCache.flushMarkerCache(breakpoint.getMarker());
			set.setElements((IAdaptable[]) list.toArray(new IAdaptable[list.size()]));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.IBreakpointOrganizerDelegate#getCategories()
	 */
	public IAdaptable[] getCategories() {
		IWorkingSet[] workingSets = fWorkingSetManager.getWorkingSets();
		List all = new ArrayList();
		for (int i = 0; i < workingSets.length; i++) {
			IWorkingSet set = workingSets[i];
			if (IDebugUIConstants.BREAKPOINT_WORKINGSET_ID.equals(set
					.getId())) {
				all.add(new WorkingSetCategory(set));
			}
		}
		return (IAdaptable[]) all.toArray(new IAdaptable[all.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IBreakpointOrganizerDelegateExtension#addBreakpoints(org.eclipse.debug.core.model.IBreakpoint[], org.eclipse.core.runtime.IAdaptable)
	 */
	public void addBreakpoints(IBreakpoint[] breakpoints, IAdaptable category) {
		if (category instanceof WorkingSetCategory) {
			IWorkingSet set = ((WorkingSetCategory) category).getWorkingSet();
			addBreakpointsToSet(breakpoints, set);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IBreakpointOrganizerDelegateExtension#removeBreakpoints(org.eclipse.debug.core.model.IBreakpoint[], org.eclipse.core.runtime.IAdaptable)
	 */
	public void removeBreakpoints(IBreakpoint[] breakpoints, IAdaptable category) {
		if (category instanceof WorkingSetCategory) {
			IWorkingSet set = ((WorkingSetCategory) category).getWorkingSet();
			IAdaptable[] elements = set.getElements();
			List list = new ArrayList(elements.length);
			for (int i = 0; i < elements.length; i++) {
				list.add(elements[i]);
			}
			for (int i = 0; i < breakpoints.length; i++) {
				IBreakpoint breakpoint = breakpoints[i];
				fCache.removeMappedEntry(breakpoint.getMarker(), set.getName());
				fCache.flushMarkerCache(breakpoint.getMarker());
				list.remove(breakpoint);
			}
			set.setElements((IAdaptable[]) list.toArray(new IAdaptable[list.size()]));
		}
	}
}
