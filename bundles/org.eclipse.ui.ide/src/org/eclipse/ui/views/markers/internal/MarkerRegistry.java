/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.views.markers.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.util.ListenerList;

/**
 * Registry that tracks resource markers and maintains a sorted, filtered list of the markers. 
 * Notifies listeners of changes in these markers.
 */
public class MarkerRegistry implements IResourceChangeListener,
        ITableViewContentProvider {

    private IFilter filter;

    private IResource input;

    private String[] types = new String[0];

    private ListenerList listeners = new ListenerList();

    public MarkerRegistry() {
    }

    /** 
     * Disposes the registry, releasing all listeners 
     * and any other allocated resources.
     */
    public void dispose() {
        listeners.clear();
        setInput(null);
    }

    /**
     * @return a filtered, sorted list of markers.
     */
    public Object[] getElements() {
        Object[] elements = getUnfilteredElements();
        if (filter != null) {
            Object[] filteredItems = filter.filter(elements);
            return filteredItems;
        }
        return elements;
    }

    /**
     * @return an unfiltered list of elements
     */
    public Object[] getUnfilteredElements() {
        if (input == null) {
            return new Object[0];
        }
        List elements = new ArrayList();
        for (int i = 0; i < types.length; i++) {
            try {
                IMarker[] newMarkers = input.findMarkers(types[i], true,
                        IResource.DEPTH_INFINITE);
                elements.addAll(Arrays.asList(newMarkers));
            } catch (CoreException e) {
            }
        }
        return elements.toArray();
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
     * Sets the registry's filter
     * 
     * @param filter 
     */
    public void setFilter(IFilter filter) {
        if (this.filter == null || !this.filter.equals(filter)) {
            this.filter = filter;
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
    }

    /**
     * Convenience method used if only interested in one base marker type.
     * 
     * @param type
     */
    public void setType(String type) {
        setTypes(new String[] { type });
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
        //filter additions and changes but not removals since they have already been deleted
        filterList(additions);
        filterList(changes);
        notifyListeners(additions, removals, changes);
    }

    /**
     * Recursively walks over the resource delta and gathers all marker deltas.  Marker
     * deltas are placed into one of the two given lists depending on the type of delta 
     * (add or remove).
     */
    private void getMarkerDeltas(IResourceDelta delta, List additions,
            List removals, List changes) {
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

    private void notifyListeners(List additions, List removals, List changes) {
        Object[] listeners = this.listeners.getListeners();
        for (int i = 0; i < listeners.length; i++) {
            IItemsChangedListener listener = (IItemsChangedListener) listeners[i];
            listener.itemsChanged(additions, removals, changes);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.views.internal.tableview.ITableViewContentProvider#addItemsChangedListener(org.eclipse.ui.views.internal.tableview.IItemsChangedListener)
     */
    public void addItemsChangedListener(IItemsChangedListener listener) {
        listeners.add(listener);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.views.internal.tableview.ITableViewContentProvider#removeItemsChangedListener(org.eclipse.ui.views.internal.tableview.IItemsChangedListener)
     */
    public void removeItemsChangedListener(IItemsChangedListener listener) {
        listeners.remove(listener);
    }

    /**
     * @return the number of items
     */
    public int getItemCount() {
        //TODO do this more efficiently
        return getElements().length;
    }

    public int getRawItemCount() {
        //TODO do this more efficiently
        return getUnfilteredElements().length;
    }

    private void filterList(List list) {
        if (filter == null || list == null) {
            return;
        }
        int i = 0;
        while (i < list.size()) {
            if (filter.select(list.get(i))) {
                i++;
            } else {
                list.remove(i);
            }
        }
    }

}