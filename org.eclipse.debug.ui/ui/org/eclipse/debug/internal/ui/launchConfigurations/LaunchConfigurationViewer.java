/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.launchConfigurations;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

/**
 * This class allow the notion of the viewer to be abstracted from the launch configuration view, as well as allowing the over-riding of 
 * selection preservation when filtering/deletion occurs
 * @since 3.3
 */
public class LaunchConfigurationViewer extends TreeViewer {

	/**
	 * Constructor
	 * @param tree the tree to create the viewer on
	 */
	public LaunchConfigurationViewer(Tree tree) {
		super(tree);
	}
	
	/**
	 * Constructor
	 * @param parent
	 * @param style
	 */
	public LaunchConfigurationViewer(Composite parent, int style) {
		this(new Tree(parent, style));
	}

	/**
	 * @see org.eclipse.jface.viewers.StructuredViewer#preservingSelection(java.lang.Runnable)
	 */
	protected void preservingSelection(Runnable updateCode) {
		IStructuredSelection selection = (IStructuredSelection) getSelection();
		if(!selection.isEmpty()) {
			int[] indices = collectIndices(selection.getFirstElement());
			updateCode.run();
			ArrayList set = new ArrayList();
			Object o = null;
			for(Iterator iter = selection.iterator(); iter.hasNext();) {
				o = iter.next();
				if(internalGetWidgetToSelect(o) != null) {
					if(!set.contains(o)) {
						set.add(o);
					}
				}
			}
			if(set.isEmpty()) {
				//make a new selection based on the first item in the structured selection
				Tree tree = getTree();
				if(tree.getItemCount() > 0) {
					int index = selectIndex(tree.getItemCount(), indices[0]);
					if(index > -1) {
						o = tree.getItem(index).getData();
					}
					else {
						//parent index exists, so select child
						TreeItem pitem = tree.getItem(indices[0]); 
						o = pitem.getData();
						//pick best child, or default to parent
						index = selectIndex(pitem.getItemCount(), indices[1]);
						if(index > -1) {
							o = pitem.getItem(index);
						}
						else {
							if(pitem.getItemCount() > 0) {
								o = pitem.getItem((indices[1]-1 > -1 ? indices[1]-1 : 0)).getData();
							}
						}
					}
					if(!set.contains(o)) {
						set.add(o);
					}
				}
			}
			setSelection(new StructuredSelection(set), true);
		}
		else {
			super.preservingSelection(updateCode);
		}
	}
	
	/**
	 * Covers the case of an outlier indice
	 * @param count the count to compare the index to
	 * @param index the index to compare against the count
	 * @return the adjusted index in the event index is an outlier, or -1 if it falls within the 'count' range
	 */
	private int selectIndex(int count, int index) {
		if(index > count-1) {
			return count-1;
		}
		if(index < 0) {
			return 0;
		}
		return -1;
	}
	
	/**
	 * Collects the indices of the child and parent items for the specified element
	 * @param object the element to collect indices for
	 * @return an array of indices for the specified element
	 */
	private int[] collectIndices(Object object) {
		int[] indices = {-1, -1};
		if(object != null) {
			TreeItem item = (TreeItem) findItem(object);
			if(item != null) {
				TreePath path = getTreePathFromItem(item); 
				item = (TreeItem) findItem(path.getFirstSegment());
				if(item != null) {
					indices[0] = getTree().indexOf(item);
					if(path.getSegmentCount() == 2) {
						indices[1] = indexOf(item.getItems(), path.getLastSegment());
					}
				}
			}
		}
		return indices;
	}
	
	/**
	 * Finds the index of the specified object in the given array of tree items
	 * @param items the items to search for the specified object
	 * @param object the object to find the index of
	 * @return the index of the specified object inthe listing of tree items, or -1 if not found
	 */
	private int indexOf(TreeItem[] items, Object object) {
		if(object != null) {
			for(int i = 0; i < items.length; i++) {
				if(object.equals(items[i].getData())) {
					return i;
				}
			}
		}
		return -1;
	}
}
