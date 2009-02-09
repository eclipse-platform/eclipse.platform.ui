/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.navigator.resources;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import org.eclipse.swt.widgets.Item;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.navigator.ICommonViewerMapper;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;

/**
 * Adds a supplemental map for the CommonViewer to efficiently handle resource
 * changes.  When objects are added to the Viewer's map, this is called to see
 * if there is an associated resource.  If so, it's added to the map here.
 * When resource change notifications happen, this map is checked, and if the
 * resource is found, this class causes the Viewer to be updated.  If the 
 * resource is not found, the notification can be ignored because the object
 * corresponding to the resource is not present in the viewer.
 * 
 */
public class ResourceToItemsMapper implements ICommonViewerMapper {

	private static final int NUMBER_LIST_REUSE = 10;

	// map from resource to item
	private HashMap _resourceToItem;
	private Stack _reuseLists;

	private CommonViewer _commonViewer;

	public ResourceToItemsMapper(CommonViewer viewer) {
		_resourceToItem = new HashMap();
		_reuseLists = new Stack();

		_commonViewer = viewer;
		viewer.setMapper(this);
	}

	public void addToMap(Object element, Item item) {
		IResource resource = getCorrespondingResource(element);
		if (resource != null) {
			Object existingMapping = _resourceToItem.get(resource);
			if (existingMapping == null) {
				_resourceToItem.put(resource, item);
			} else if (existingMapping instanceof Item) {
				if (existingMapping != item) {
					List list = getNewList();
					list.add(existingMapping);
					list.add(item);
					_resourceToItem.put(resource, list);
				}
			} else { // List
				List list = (List) existingMapping;
				if (!list.contains(item)) {
					list.add(item);
				}
			}
		}
	}

	public void removeFromMap(Object element, Item item) {
		IResource resource = getCorrespondingResource(element);
		if (resource != null) {
			Object existingMapping = _resourceToItem.get(resource);
			if (existingMapping == null) {
				return;
			} else if (existingMapping instanceof Item) {
				_resourceToItem.remove(resource);
			} else { // List
				List list = (List) existingMapping;
				list.remove(item);
				if (list.isEmpty()) {
					_resourceToItem.remove(list);
					releaseList(list);
				}
			}
		}
	}

	public void clearMap() {
		_resourceToItem.clear();
	}

	public boolean isEmpty() {
		return _resourceToItem.isEmpty();
	}

	private List getNewList() {
		if (!_reuseLists.isEmpty()) {
			return (List) _reuseLists.pop();
		}
		return new ArrayList(2);
	}

	private void releaseList(List list) {
		if (_reuseLists.size() < NUMBER_LIST_REUSE) {
			_reuseLists.push(list);
		}
	}

	public boolean handlesObject(Object object) {
		return object instanceof IResource;
	}
	

	/**
	 * Must be called from the UI thread.
	 * 
	 * @param changedResource
	 *            Changed resource
	 */
	public void objectChanged(Object changedResource) {
		Object obj = _resourceToItem.get(changedResource);
		if (obj == null) {
			// not mapped
		} else if (obj instanceof Item) {
			updateItem((Item) obj);
		} else { // List of Items
			List list = (List) obj;
			for (int k = 0; k < list.size(); k++) {
				updateItem((Item) list.get(k));
			}
		}
	}

	private void updateItem(Item item) {
		if (!item.isDisposed()) {
			_commonViewer.doUpdateItem(item);
		}
	}

	private static IResource getCorrespondingResource(Object element) {
		if (element instanceof IResource)
			return (IResource) element;
		if (element instanceof IAdaptable)
			return (IResource) ((IAdaptable) element).getAdapter(IResource.class);
		return null;
	}
}
