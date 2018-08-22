/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.search.internal.ui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.PlatformObject;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;

import org.eclipse.search.ui.ISearchResultViewEntry;
import org.eclipse.search.ui.SearchUI;

/**
 * Represents an entry in the search result view
 * @deprecated old search
 */
@Deprecated
public class SearchResultViewEntry extends PlatformObject implements ISearchResultViewEntry {

	private Object fGroupByKey= null;
	private IResource fResource= null;
	private IMarker fMarker= null;
	private ArrayList<IMarker> fMarkers= null;
	private ArrayList<Map<String, Object>> fAttributes;
	private int fSelectedMarkerIndex;
	private long fModificationStamp= IResource.NULL_STAMP;
	private String fMarkerType;

	public SearchResultViewEntry(Object groupByKey, IResource resource) {
		fGroupByKey= groupByKey;
		fResource= resource;
		if (fResource != null)
			fModificationStamp= fResource.getModificationStamp();
	}

	//---- Accessors ------------------------------------------------
	@Override
	public Object getGroupByKey() {
		return fGroupByKey;
	}

	void setGroupByKey(Object groupByKey) {
		fGroupByKey= groupByKey;
	}

	@Override
	public IResource getResource() {
		return fResource;
	}

	@Override
	public int getMatchCount() {
		if (fMarkers != null)
			return fMarkers.size();
		if (fMarkers == null && fMarker != null)
			return 1;
		return 0;
	}

	boolean isPotentialMatch() {
		if (fMarker != null)
			return fMarker.getAttribute(SearchUI.POTENTIAL_MATCH, false);
		return false;
	}

	List<Map<String, Object>> getAttributesPerMarker() {
		if (fAttributes == null)
			return new ArrayList<>(0);
		return fAttributes;
	}

	public long getModificationStamp() {
		return fModificationStamp;
	}

	void clearMarkerList() {
		fMarker= null;
		if (fMarkers != null)
			fMarkers.clear();
	}

	void add(IMarker marker) {
		if (marker != null && fMarkerType == null) {
			try {
				fMarkerType= marker.getType();
			} catch (CoreException ex) {
				// will default to org.eclipse.search.searchmarker
			}
		}

		if (fMarker == null) {
			fMarker= marker;
			if (fMarkers != null)
				fMarkers.add(marker);
			return;
		}
		if (fMarkers == null) {
			fMarkers= new ArrayList<>(10);
			addByStartpos(fMarkers, fMarker);
		}
		addByStartpos(fMarkers, marker);
	}

	void setSelectedMarkerIndex(int index) {
		fSelectedMarkerIndex= index;
	}

	@Override
	public IMarker getSelectedMarker() {
		fSelectedMarkerIndex= Math.min(fSelectedMarkerIndex, getMatchCount() - 1);
		if (fMarkers == null && fMarker == null)
			return null;
		if (fMarkers != null && fSelectedMarkerIndex >= 0)
			return fMarkers.get(fSelectedMarkerIndex);
		return fMarker;
	}

	public List<IMarker> getMarkers() {
		if (fMarkers == null && fMarker == null)
			return new ArrayList<>(0);
		else if (fMarkers == null && fMarker != null) {
			List<IMarker> markers= new ArrayList<>(1);
			markers.add(fMarker);
			return markers;
		}
		return fMarkers;
	}

	String getMarkerType() {
		if (fMarkerType == null)
			return SearchUI.SEARCH_MARKER;
		return fMarkerType;
	}

	boolean contains(IMarker marker) {
		if (fMarkers == null && fMarker == null)
			return false;
		if (fMarkers == null)
			return fMarker.equals(marker);
		return fMarkers.contains(marker);
	}

	void remove(IMarker marker) {
		if (marker == null)
			return;

		if (fMarkers == null) {
			if (fMarker != null && fMarker.equals(marker))
				fMarker= null;
		}
		else {
			fMarkers.remove(marker);
			if (fMarkers.size() == 1) {
				fMarker= fMarkers.get(0);
				fMarkers= null;
			}
		}
	}

	void backupMarkers() {
		if (fResource != null)
			fModificationStamp= fResource.getModificationStamp();
		List<IMarker> markers= getMarkers();
		fAttributes= new ArrayList<>(markers.size());
		Iterator<IMarker> iter= markers.iterator();
		while (iter.hasNext()) {
			IMarker marker= iter.next();
			Map<String, Object> attributes= null;
			try {
				attributes= marker.getAttributes();
			} catch (CoreException ex) {
				// don't backup corrupt marker
				continue;
			}
			fAttributes.add(attributes);
		}
	}

	private void addByStartpos(ArrayList<IMarker> markers, IMarker marker) {
		int startPos= marker.getAttribute(IMarker.CHAR_START, -1);
		int i= 0;
		int markerCount= markers.size();
		while (i < markerCount && startPos >= markers.get(i).getAttribute(IMarker.CHAR_START, -1))
			i++;
		markers.add(i, marker);
		if (i == 0)
			fMarker= marker;
	}

	@Override
	public <T> T getAdapter(Class<T> adapter) {
		return super.getAdapter(adapter);
	}
}
