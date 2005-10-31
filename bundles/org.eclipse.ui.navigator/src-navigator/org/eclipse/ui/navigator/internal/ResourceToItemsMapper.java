/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
/*
 * Created on Aug 12, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.eclipse.ui.navigator.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IViewerLabelProvider;
import org.eclipse.jface.viewers.ViewerLabel;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Item;
import org.eclipse.ui.navigator.AdaptabilityUtility;

/**
 * Helper class for updating error markers and other decorators that work on resources. Items are
 * mapped to their element's underlying resource. Method <code>resourceChanged</code> updates all
 * items that are affected from the changed elements.
 * 
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part of a work in
 * progress. There is a guarantee neither that this API will work nor that it will remain the same.
 * Please do not use this API without consulting with the Platform/UI team.
 * </p>
 * 
 * @since 3.2
 */
public class ResourceToItemsMapper {

	private static final int NUMBER_LIST_REUSE = 10;

	// map from resource to item
	private HashMap fResourceToItem;
	private Stack fReuseLists;

	private ContentViewer fContentViewer;
	private static final Class IRESOURCE_CLASS = IResource.class;


	public ResourceToItemsMapper(ContentViewer viewer) {
		fResourceToItem = new HashMap();
		fReuseLists = new Stack();

		fContentViewer = viewer;
	}

	/**
	 * Must be called from the UI thread.
	 */
	public void resourceChanged(IResource changedResource) {
		Object obj = fResourceToItem.get(changedResource);
		if (obj == null) {
			// not mapped
		} else if (obj instanceof Item) {
			Item item = (Item) obj;
			/*if (item.isDisposed()) 
				fResourceToItem.remove(changedResource);
			else*/
				updateItem(item);
		} else { // List of Items
			List list = (List) obj;
			for (int k = 0; k < list.size(); k++) {
				updateItem((Item) list.get(k));
			}
			/*for (int k=list.size(); k>0;k--) {
				Item item = (Item) list.get(k-1);
				if (item.isDisposed() ) 
					list.remove(item);
				else
					updateItem(item);
			}*/
		}
	}

	private void updateItem(Item item) {
		Object data = null;
		try {
			if (!item.isDisposed()) { // defensive code
				ILabelProvider lprovider = (ILabelProvider) fContentViewer.getLabelProvider();

				data = item.getData();
				if (data==null)
					return;
				
				// If it is an IItemLabelProvider than short circuit: patch Tod (bug 55012)
				if (lprovider instanceof IViewerLabelProvider) {
					IViewerLabelProvider provider = (IViewerLabelProvider) lprovider;

					ViewerLabel updateLabel = new ViewerLabel(item.getText(), item.getImage());
					provider.updateLabel(updateLabel, data);

					if (updateLabel.hasNewImage()) {
						item.setImage(updateLabel.getImage());
					}
					if (updateLabel.hasNewText()) {
						item.setText(updateLabel.getText());
					}
				} else {
					Image oldImage = item.getImage();
					Image image = lprovider.getImage(data);
					if (image != null && !image.equals(oldImage)) {
						item.setImage(image);
					}
					String oldText = item.getText();
					String text = lprovider.getText(data);
					if (text != null && !text.equals(oldText)) {
						item.setText(text);
					}
				}
			}
		} catch (Throwable ex) {
			System.out.println("An error occurred in " + getClass().getName() + ".doUpdateItem(Item, Object):"); //$NON-NLS-1$ //$NON-NLS-2$
			System.out.println("\titem = \"" + (item.isDisposed() ? " Is Disposed. " : item.toString()) + "\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			if (data != null)
				System.out.println("\tmodel element = \"" + data + "\""); //$NON-NLS-1$ //$NON-NLS-2$
			ex.printStackTrace();
		}
	}

	/**
	 * Adds a new item to the map.
	 * 
	 * @param element
	 *            Element to map
	 * @param item
	 *            The item used for the element
	 */
	public void addToMap(Object element, Item item) {
		IResource resource = getCorrespondingResource(element);
		if (resource != null) {
			Object existingMapping = fResourceToItem.get(resource);
			if (existingMapping == null) {
				fResourceToItem.put(resource, item);
			} else if (existingMapping instanceof Item) {
				if (existingMapping != item) {
					List list = getNewList();
					list.add(existingMapping);
					list.add(item);
					fResourceToItem.put(resource, list);
				}
			} else { // List
				List list = (List) existingMapping;
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
		IResource resource = getCorrespondingResource(element);
		if (resource != null) {
			Object existingMapping = fResourceToItem.get(resource);
			if (existingMapping == null) {
				return;
			} else if (existingMapping instanceof Item) {
				fResourceToItem.remove(resource);
			} else { // List
				List list = (List) existingMapping;
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
	 * Tests if the map is empty
	 */
	public boolean isEmpty() {
		return fResourceToItem.isEmpty();
	}

	/**
	 * Method that decides which elements can have error markers Returns null if an element can not
	 * have error markers.
	 */
	private static IResource getCorrespondingResource(Object element) {
		if (element != null) {
			if (element instanceof IResource)
				return (IResource) element;
			return (IResource) AdaptabilityUtility.getAdapter(element, IRESOURCE_CLASS);
		}
		return null;
	}

}