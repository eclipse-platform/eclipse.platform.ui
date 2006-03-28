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
package org.eclipse.search.internal.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import org.eclipse.core.resources.IResource;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Item;

import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.jface.viewers.ILabelProvider;

import org.eclipse.search.ui.ISearchResultViewEntry;

/**
 * Helper class for updating error markers and other decorators that work on resources.
 * Items are mapped to their element's underlying resource.
 * Method <code>resourceChanged</code> updates all items that are affected from the changed
 * elements.
 * @deprecated old search
 */
class ResourceToItemsMapper {

	private static final int NUMBER_LIST_REUSE= 10;

	// map from resource to item
	private HashMap fResourceToItem;
	private Stack fReuseLists;
	
	private ContentViewer fContentViewer;

	public ResourceToItemsMapper(ContentViewer viewer) {
		fResourceToItem= new HashMap();
		fReuseLists= new Stack();
		
		fContentViewer= viewer;
	}

	/**
	 * Must be called from the UI thread.
	 */
	public void resourceChanged(IResource changedResource) {
		Object obj= fResourceToItem.get(changedResource);
		if (obj == null) {
			// not mapped
		} else if (obj instanceof Item) {
			updateItem((Item) obj);
		} else { // List of Items
			List list= (List) obj;
			for (int k= 0; k < list.size(); k++) {
				updateItem((Item) list.get(k));
			}
		}
	}
		
	private void updateItem(Item item) {
		if (!item.isDisposed()) { // defensive code
			ILabelProvider lprovider= (ILabelProvider) fContentViewer.getLabelProvider();
			
			Object data= item.getData();

			String oldText= item.getText();
			String text= lprovider.getText(data);
			if (text != null && !text.equals(oldText)) {
				item.setText(text);
			}

			Image oldImage= item.getImage();
			Image image= lprovider.getImage(data);
			if (image != null && !image.equals(oldImage)) {
				item.setImage(image);
			}
		}
	}

	/**
	 * Adds a new item to the map.
	 * @param element Element to map
	 * @param item The item used for the element
	 */
	public void addToMap(Object element, Item item) {
		IResource resource= ((ISearchResultViewEntry)element).getResource();
		if (resource != null) {
			Object existingMapping= fResourceToItem.get(resource);
			if (existingMapping == null) {
				fResourceToItem.put(resource, item);
			} else if (existingMapping instanceof Item) {
				if (existingMapping != item) {
					List list= getNewList();
					list.add(existingMapping);
					list.add(item);
					fResourceToItem.put(resource, list);
				}
			} else { // List			
				List list= (List) existingMapping;
				if (!list.contains(item)) {
					list.add(item);
				}
			}
		}
	}

	/**
	 * Removes an element from the map.
	 */	
	public void removeFromMap(Object element, Item item) {
		IResource resource= ((ISearchResultViewEntry)element).getResource();
		if (resource != null) {
			Object existingMapping= fResourceToItem.get(resource);
			if (existingMapping == null) {
				return;
			} else if (existingMapping instanceof Item) {
				fResourceToItem.remove(resource);
			} else { // List
				List list= (List) existingMapping;
				list.remove(item);
				if (list.isEmpty()) {
					fResourceToItem.remove(list);
					releaseList(list);
				}
			}
		}
	}
	
	private List getNewList() {
		if (!fReuseLists.isEmpty()) {
			return (List) fReuseLists.pop();
		}
		return new ArrayList(2);
	}
	
	private void releaseList(List list) {
		if (fReuseLists.size() < NUMBER_LIST_REUSE) {
			fReuseLists.push(list);
		}
	}
	
	/**
	 * Clears the map.
	 */
	public void clearMap() {
		fResourceToItem.clear();
	}
	
	/**
	 * Clears the map.
	 */
	public boolean isEmpty() {
		return fResourceToItem.isEmpty();
	}	
}
