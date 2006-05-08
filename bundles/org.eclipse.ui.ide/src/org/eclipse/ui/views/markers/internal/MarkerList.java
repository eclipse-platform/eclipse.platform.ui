/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.views.markers.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Represents a list of ConcreteMarkers.
 */
public class MarkerList {
	
	private int[] markerCounts = null;

	private ConcreteMarker[] markers;

	/**
	 * Lazily created marker table - maps IMarkers onto ConcreteMarkers. Null if
	 * not created
	 */
	private Map markerTable;

	/**
	 * Creates an initially empty marker list
	 */
	public MarkerList() {
		this(new ConcreteMarker[0]);
	}

	public MarkerList(Collection markers) {
		this((ConcreteMarker[]) markers.toArray(new ConcreteMarker[markers
				.size()]));
	}

	/**
	 * Creates a list containing the given set of markers
	 * 
	 * @param markers
	 */
	public MarkerList(ConcreteMarker[] markers) {
		this.markers = markers;
	}

	/**
	 * Clears any cached collation keys. Use to free up some memory if the
	 * markers in this list won't be sorted for awhile.
	 */
	public void clearCache() {
		for (int i = 0; i < markers.length; i++) {
			ConcreteMarker marker = markers[i];

			marker.clearCache();
		}

		markerTable = null;
	}

	/**
	 * Returns the marker table or lazily creates it if it doesn't exist yet
	 * 
	 * @return a map of IMarker onto ConcreteMarker, containing all the
	 *         ConcreteMarkers in the list
	 */
	private Map getMarkerMap() {
		if (markerTable == null) {
			markerTable = new HashMap();

			for (int idx = 0; idx < markers.length; idx++) {
				ConcreteMarker marker = markers[idx];
				markerTable.put(marker.getMarker(), marker);
			}
		}

		return markerTable;
	}

	/**
	 * Returns an existing marker from the list that is associated with the
	 * given IMarker
	 * 
	 * @param toFind
	 *            the IMarker to lookup in the list
	 * @return the ConcreteMarker that corresponds to the given IMarker
	 */
	public ConcreteMarker getMarker(IMarker toFind) {
		return (ConcreteMarker) getMarkerMap().get(toFind);
	}
	
	/**
	 * Return the list of IMarkers contained in the receiver.
	 * @return IMarker[]
	 */
	public IMarker[] getIMarkers(){
		IMarker[] iMarkers = new IMarker[markers.length];
		for (int i = 0; i < markers.length; i++) {
			iMarkers[i] = markers[i].getMarker();			
		}
		return iMarkers;
	}

	public static ConcreteMarker createMarker(IMarker marker)
			throws CoreException {
		if (marker.isSubtypeOf(IMarker.TASK)) {
			return new TaskMarker(marker);
		} else if (marker.isSubtypeOf(IMarker.BOOKMARK)) {
			return new BookmarkMarker(marker);
		} else if (marker.isSubtypeOf(IMarker.PROBLEM)) {
			return new ProblemMarker(marker);
		} else {
			return new ConcreteMarker(marker);
		}
	}

	public void refresh() {
		for (int markerIdx = 0; markerIdx < markers.length; markerIdx++) {
			ConcreteMarker next = markers[markerIdx];
			next.refresh();
		}
	}

	public List asList() {
		return Arrays.asList(markers);
	}

	public MarkerList findMarkers(Collection ofIMarker) {
		List result = new ArrayList(ofIMarker.size());

		Iterator iter = ofIMarker.iterator();
		while (iter.hasNext()) {
			IMarker next = (IMarker) iter.next();

			ConcreteMarker marker = getMarker(next);
			if (marker != null) {
				result.add(marker);
			}
		}

		return new MarkerList(result);
	}

	public static ConcreteMarker[] createMarkers(IMarker[] source)
			throws CoreException {
		ConcreteMarker[] result = new ConcreteMarker[source.length];

		for (int idx = 0; idx < source.length; idx++) {
			result[idx] = createMarker(source[idx]);
		}

		return result;
	}

	/**
	 * Computes the set of markers that match the given filter
	 * 
	 * @param filters
	 *            the filters to apply
	 * @param mon
	 *            the monitor to update
	 * @param ignoreExceptions
	 *            whether or not exception will be shown
	 * @return MarkerList
	 * @throws CoreException
	 */
	public static MarkerList compute(MarkerFilter[] filters,
			IProgressMonitor mon, boolean ignoreExceptions)
			throws CoreException {

		Collection returnMarkers = new HashSet();// avoid duplicates

		for (int i = 0; i < filters.length; i++) {
			returnMarkers.addAll(filters[i].findMarkers(mon, ignoreExceptions));
		}
		return new MarkerList(returnMarkers);
	}

	/**
	 * Returns a new MarkerList containing all markers in the workspace of the
	 * specified types
	 * 
	 * @param types
	 * @return IMarker[]
	 * @throws CoreException
	 */
	public static IMarker[] compute(String[] types) throws CoreException {

		ArrayList result = new ArrayList();
		IResource input = ResourcesPlugin.getWorkspace().getRoot();

		for (int i = 0; i < types.length; i++) {
			IMarker[] newMarkers = input.findMarkers(types[i], true,
					IResource.DEPTH_INFINITE);
			result.addAll(Arrays.asList(newMarkers));
		}

		return (IMarker[]) result.toArray(new IMarker[result.size()]);
	}

	/**
	 * Returns the markers in the list. Read-only.
	 * 
	 * @return an array of markers in the list
	 */
	public ConcreteMarker[] toArray() {
		return markers;
	}

	/**
	 * Returns the markers in this list. Read-only.
	 * 
	 * @return the markers in the list
	 */
	// public Collection getMarkers() {
	// return markers;
	// }
	/**
	 * Returns the number of items in the list
	 * 
	 * @return the number of items
	 */
	public int getItemCount() {
		return markers.length;
	}

	/**
	 * Returns the number of error markers in the list
	 * 
	 * @return the number of errors
	 */
	public int getErrors() {
		return getMarkerCounts()[IMarker.SEVERITY_ERROR];
	}

	/**
	 * Returns the number of info markers in the list
	 * 
	 * @return the number of info markers
	 */
	public int getInfos() {
		return getMarkerCounts()[IMarker.SEVERITY_INFO];
	}

	/**
	 * Returns the number of warning markers in the list
	 * 
	 * @return the number of warning markers
	 */
	public int getWarnings() {
		return getMarkerCounts()[IMarker.SEVERITY_WARNING];
	}

	/**
	 * Returns an array of marker counts where getMarkerCounts()[severity] is
	 * the number of markers in the list with the given severity.
	 * 
	 * @return an array of marker counts
	 */
	private int[] getMarkerCounts() {
		if (markerCounts == null) {
			markerCounts = new int[] { 0, 0, 0 };

			for (int idx = 0; idx < markers.length; idx++) {
				ConcreteMarker marker = markers[idx];

				if (marker instanceof ProblemMarker) {
					int severity = ((ProblemMarker) markers[idx]).getSeverity();
					if (severity >= 0 && severity <= 2) {
						markerCounts[severity]++;
					}
				}

			}
		}
		return markerCounts;
	}

	/**
	 * Get the array that is the internal representation of the marker list
	 * without making a copy.
	 * 
	 * @return Object[]
	 */
	public Object[] getArray() {
		return markers;
	}

	/**
	 * Get the size of the receiver.
	 * 
	 * @return int
	 */
	public int getSize() {
		return getArray().length;
	}

	/**
	 * Return the markers at index
	 * 
	 * @param index
	 * @return ConcreteMarker
	 */
	public ConcreteMarker getMarker(int index) {
		return markers[index];
	}

	/**
	 * Add the addedMarkers to the receiver.
	 * @param addedMarkers Collection of ConcreteMarker
	 * @param removedMarkers Collection of ConcreteMarker
	 */
	public void updateMarkers(Collection addedMarkers,Collection removedMarkers) {
		List list = new ArrayList(asList());
		list.addAll(addedMarkers);		
		list.removeAll(removedMarkers);	
		markers = new ConcreteMarker[list.size()];
		list.toArray(markers);
	}
	
	/**
	 * Refresh all of the markers in the receiver.
	 */
	public void refreshAll() {
		for (int i = 0; i < markers.length; i++) {
			markers[i].refresh();
		}		
	}

	/**
	 * Clear all of the group settings in the receiver.
	 */
	public void clearGroups() {
		for (int i = 0; i < markers.length; i++) {
			markers[i].setGroup(null);
		}
		
	}
}
