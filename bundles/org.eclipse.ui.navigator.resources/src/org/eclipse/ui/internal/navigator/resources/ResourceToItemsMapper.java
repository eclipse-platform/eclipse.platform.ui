/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.swt.widgets.Item;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.navigator.ICommonViewerMapper;

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

	// map from resource to item. Value can be single Item of List<Item>
	private HashMap<IResource, Object> _resourceToItem;
	private Stack<List<Item>> _reuseLists;

	private CommonViewer _commonViewer;

	public ResourceToItemsMapper(CommonViewer viewer) {
		_resourceToItem = new HashMap<IResource, Object>();
		_reuseLists = new Stack<List<Item>>();

		_commonViewer = viewer;
		viewer.setMapper(this);
	}

	@Override
	public void addToMap(Object element, Item item) {
		IResource resource = getCorrespondingResource(element);
		if (resource != null) {
			Object existingMapping = _resourceToItem.get(resource);
			if (existingMapping == null) {
				_resourceToItem.put(resource, item);
			} else if (existingMapping instanceof Item) {
				if (existingMapping != item) {
					List<Item> list = getNewList();
					list.add((Item)existingMapping);
					list.add(item);
					_resourceToItem.put(resource, list);
				}
			} else { // List
				@SuppressWarnings("unchecked")
				List<Item> list = (List<Item>) existingMapping;
				if (!list.contains(item)) {
					list.add(item);
				}
			}
		}
	}

	@Override
	public void removeFromMap(Object element, Item item) {
		IResource resource = getCorrespondingResource(element);
		if (resource != null) {
			Object existingMapping = _resourceToItem.get(resource);
			if (existingMapping == null) {
				return;
			} else if (existingMapping instanceof Item) {
				_resourceToItem.remove(resource);
			} else { // List
				@SuppressWarnings("unchecked")
				List<Item> list = (List<Item>) existingMapping;
				list.remove(item);
				if (list.isEmpty()) {
					_resourceToItem.remove(list);
					releaseList(list);
				}
			}
		}
	}

	@Override
	public void clearMap() {
		_resourceToItem.clear();
	}

	@Override
	public boolean isEmpty() {
		return _resourceToItem.isEmpty();
	}

	private List<Item> getNewList() {
		if (!_reuseLists.isEmpty()) {
			return _reuseLists.pop();
		}
		return new ArrayList<Item>(2);
	}

	private void releaseList(List<Item> list) {
		if (_reuseLists.size() < NUMBER_LIST_REUSE) {
			_reuseLists.push(list);
		}
	}

	@Override
	public boolean handlesObject(Object object) {
		return object instanceof IResource;
	}


	/**
	 * Must be called from the UI thread.
	 *
	 * @param changedResource
	 *            Changed resource
	 */
	@Override
	public void objectChanged(Object changedResource) {
		Object obj = _resourceToItem.get(changedResource);
		if (obj == null) {
			// not mapped
		} else if (obj instanceof Item) {
			updateItem((Item) obj);
		} else { // List of Items
			@SuppressWarnings("unchecked")
			List<Item> list = (List<Item>) obj;
			for (Item item : list) {
				updateItem(item);
			}
		}
	}

	private void updateItem(Item item) {
		if (!item.isDisposed()) {
			_commonViewer.doUpdateItem(item);
		}
	}

	private static IResource getCorrespondingResource(Object element) {
		return Adapters.adapt(element, IResource.class);
	}
}
