/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.views.internal.markers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.CoreException;


/**
 * Registry that tracks resource markers and maintains a sorted, filtered list of the markers. 
 * Notifies listeners of changes in these markers.
 */
public class MarkerRegistry implements IResourceChangeListener {
	
	private IFilter filter;
	private Comparator comparator;
	private IResource input;
	private List rawElements;
	private List elements;
	private String[] types;
	
	private List listeners;
	
	protected MarkerRegistry() {
		rawElements = new ArrayList();
		elements = new ArrayList();
		listeners = new ArrayList();
		types = new String[0];
	}
	
	private void refresh(boolean full) {
		if (input == null) {
			return;
		}
		if (full) {
			IMarker[] markers;
			rawElements = new ArrayList();
			elements = new ArrayList();
			for (int i = 0; i < types.length; i++) {
				try {
					IMarker[] newMarkers = input.findMarkers(types[i], true, IResource.DEPTH_INFINITE);
					rawElements.addAll(Arrays.asList(newMarkers));
				}
				catch (CoreException e) {
				}
			}
		}
		if (comparator != null)
			Collections.sort(rawElements, comparator);
		elements = new ArrayList(rawElements);
		if (filter != null)
			filter.filter(elements);
	}
	
	/**
	 * Perfoms a full refresh. Asks the input for all its markers then sorts and 
	 * filters those markers.
	 */
	public void refresh() {
		refresh(true);
	}
	
	/**
	 * Refilters the list of markers. Notifies listeners of differences between 
	 * the old list of markers and the new list. 
	 */
	public void refilter() {
		List additions = new ArrayList();
		List removals = new ArrayList();
		List changes = new ArrayList();
		List newElements = new ArrayList(rawElements);
		int i = 0;
		while (i < newElements.size()) {
			Object element = newElements.get(i);
			int index = Collections.binarySearch(elements, element, comparator);
			if (filter.select(element)) {
				//check if element is in old list
				if (index < 0) {
					additions.add(element);
				}
				i++;
			}
			else {
				newElements.remove(element);
				if (index >= 0) {
					removals.add(element);
				}
			}
		}
		elements = newElements;
		if (additions.size() + removals.size() + changes.size() > 0) {
			notifyListeners(additions, removals, changes);
		}
	}
	
	/**
	 * Resorts the list of markers.
	 */
	public void resort() {
		if (comparator != null)
			Collections.sort(rawElements, comparator);
		elements = new ArrayList(rawElements);
		if (filter != null)
			filter.filter(elements);
	}
	
	/**
	 * @return the registry's comparator or <code>null</code> if no comparator has been set.
	 */
	public Comparator getComparator() {
		return comparator;
	}

	/**
	 * @return a filtered, sorted list of markers.
	 */
	public List getElements() {
		return elements;
	}
	
	/**
	 * @param index
	 * @return the element in the list at the specified index
	 */
	public IMarker getMarker(int index) {
		if (index >= 0 && index < elements.size())
			return (IMarker) elements.get(index);
		return null;
	}

	/**
	 * @return the registry's filter or <code>null</code> if no filter has been assigned 
	 * to the registry.
	 */
	public IFilter getFilter() {
		return filter;
	}

	/**
	 * @return the registry's input resource
	 */
	public IResource getInput() {
		return input;
	}

	/**
	 * @return an unfiltered list of elements
	 */
	public List getRawElements() {
		return rawElements;
	}

	/**
	 * Sets the registry's comparator
	 * 
	 * @param comparator
	 */
	public void setComparator(Comparator comparator) {
		if (this.comparator == null || !this.comparator.equals(comparator)) {
			this.comparator = comparator;
			if (input != null) {
				resort();
			}
		}
	}

	/**
	 * Sets the registry's filter
	 * 
	 * @param filter 
	 */
	public void setFilter(IFilter filter) {
		if (this.filter == null || !this.filter.equals(filter)) {
			this.filter = filter;
			if (input != null) {
				refilter();
			}
		}
	}

	/**
	 * Sets the registry's input resource
	 * 
	 * @param resource
	 */
	public void setInput(IResource resource) {
		if (input != null) {
			if (input.equals(resource))
				return;
			input.getWorkspace().removeResourceChangeListener(this);
		}
		input = resource;
		if (input != null)
			resource.getWorkspace().addResourceChangeListener(this);
		refresh(true);
	}

	/**
	 * @return the base marker types that the registry is tracking
	 */
	public String[] getTypes() {
		return types;
	}

	/**
	 * Sets the base marker types to track. By default the registry will search for
	 * all markers of these types and their subtypes.
	 * 
	 * @param types
	 */
	public void setTypes(String[] types) {
		if (types == null) 
			this.types = new String[0];
		else
			this.types = types;
		refresh();
	}
	
	/**
	 * Convenience method used if only interested in one base marker type.
	 * 
	 * @param type
	 */
	public void setType(String type) {
		setTypes(new String[] { type });
	}

	private void add(IMarker marker) {
		if (marker == null)
			return;
			
		if (rawElements.contains(marker))
			return;
			
		if (comparator == null) {
			rawElements.add(marker);
			if (filter == null || filter.select(marker))
				elements.add(marker);
		}
		else {
			int rawPosition = Collections.binarySearch(rawElements, marker, comparator);
			if (rawPosition < 0)
				rawElements.add(-1 - rawPosition, marker);
			if (filter == null || filter.select(marker)) {
				int position = Collections.binarySearch(elements, marker, comparator);
				if (position < 0)
					elements.add(-1 - position, marker);
			}
		}
	}
	
	private void add(List markers) {
		if (markers == null)
			return;
		for (int i = 0; i < markers.size(); i++) {
			Object obj = markers.get(i);
			if (obj instanceof IMarker)
				add((IMarker) obj);
		}
	}
	
	private void remove(IMarker marker) {
		if (marker == null)
			return;
		rawElements.remove(marker);
		elements.remove(marker);
	}
	
	private void remove(List markers) {
		if (markers == null)
			return;
		for (int i = 0; i < markers.size(); i++) {
			Object obj = markers.get(i);
			if (obj instanceof IMarker)
				remove((IMarker) obj);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.IResourceChangeListener#resourceChanged(org.eclipse.core.resources.IResourceChangeEvent)
	 */
	public void resourceChanged(IResourceChangeEvent event) {
	
		// gather all marker changes from the delta.
		// be sure to do this in the calling thread, 
		// as the delta is destroyed when this method returns
		final List additions = new ArrayList();
		final List removals = new ArrayList();
		final List changes = new ArrayList();
		
		IResourceDelta delta = event.getDelta();
		if (delta == null) 
			return;
		getMarkerDeltas(delta, additions, removals, changes);
		add(additions);
		remove(removals);
		notifyListeners(additions, removals, changes);
	}
	
	/**
	 * Recursively walks over the resource delta and gathers all marker deltas.  Marker
	 * deltas are placed into one of the two given lists depending on the type of delta 
	 * (add or remove).
	 */
	private void getMarkerDeltas(IResourceDelta delta, List additions, List removals, List changes) {
		IMarkerDelta[] markerDeltas = delta.getMarkerDeltas();
		for (int i = 0; i < markerDeltas.length; i++) {
			IMarkerDelta markerDelta = markerDeltas[i];
			IMarker marker = markerDelta.getMarker();
			switch (markerDelta.getKind()) {
				case IResourceDelta.ADDED: {
					boolean added = false;
					for (int j = 0; j < types.length && !added; j++) {
						if (markerDelta.isSubtypeOf(types[j])) {
							additions.add(marker);
							added = true;
						}
					}
					break;
				}
				case IResourceDelta.REMOVED: {
					boolean added = false;
					for (int j = 0; j < types.length && !added; j++) {
						if (markerDelta.isSubtypeOf(types[j])) {
							removals.add(marker);
							added = true;
						}
					}
					break;
				}
				case IResourceDelta.CHANGED: {
					boolean added = false;
					for (int j = 0; j < types.length && !added; j++) {
						if (markerDelta.isSubtypeOf(types[j])) {
							changes.add(marker);
							added = true;
						}
					}
					break;
				}
			}
		}
	
		//recurse on child deltas
		IResourceDelta[] children = delta.getAffectedChildren();
		for (int i = 0; i < children.length; i++) {
			getMarkerDeltas(children[i], additions, removals, changes);
		}
	}
	
	/**
	 * Adds an IMarkerChangedListener to the list of listeners. Has no effect if the list 
	 * already contains an identical listener.
	 * 
	 * @param listener the listener
	 */
	public void addMarkerChangedListener(IMarkerChangedListener listener) {
		if (listeners == null || listener == null)
			return;
		if (!listeners.contains(listener))
			listeners.add(listener);
	}
	
	/**
	 * Removes the listener from the list of listeners. Has no effect if the list does not
	 * contain the listener.
	 * 
	 * @param listener
	 */
	public void removeMarkerChangedListener(IMarkerChangedListener listener) {
		if (listeners == null || listener == null)
			return;
		listeners.remove(listener);
	}
	
	private void notifyListeners(List additions, List removals, List changes) {
		if (listeners == null)
			return;
		for (int i = 0; i < listeners.size(); i++) {
			IMarkerChangedListener listener = (IMarkerChangedListener) listeners.get(i);
			listener.markerChanged(additions, removals, changes);
		}
	}
	
}
