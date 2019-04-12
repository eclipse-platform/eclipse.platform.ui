/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
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
package org.eclipse.ui.dialogs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;

/**
 * CheckboxTreeViewer with special behaviour of the checked / gray state on
 * container (non-leaf) nodes: The grayed state is used to visualize the checked
 * state of its children. Containers are checked and non-gray if all contained
 * leafs are checked. The container is grayed if some but not all leafs are
 * checked.
 * 
 * @since 3.1
 */
public class ContainerCheckedTreeViewer extends CheckboxTreeViewer {

	/**
	 * Constructor for ContainerCheckedTreeViewer.
	 * 
	 * @see CheckboxTreeViewer#CheckboxTreeViewer(Composite)
	 */
	public ContainerCheckedTreeViewer(Composite parent) {
		super(parent);
		initViewer();
	}

	/**
	 * Constructor for ContainerCheckedTreeViewer.
	 * 
	 * @see CheckboxTreeViewer#CheckboxTreeViewer(Composite,int)
	 */
	public ContainerCheckedTreeViewer(Composite parent, int style) {
		super(parent, style);
		initViewer();
	}

	/**
	 * Constructor for ContainerCheckedTreeViewer.
	 * 
	 * @see CheckboxTreeViewer#CheckboxTreeViewer(Tree)
	 */
	public ContainerCheckedTreeViewer(Tree tree) {
		super(tree);
		initViewer();
	}

	private void initViewer() {
		setUseHashlookup(true);
		addCheckStateListener(event -> doCheckStateChanged(event.getElement()));
		addTreeListener(new ITreeViewerListener() {
			@Override
			public void treeCollapsed(TreeExpansionEvent event) {
			}

			@Override
			public void treeExpanded(TreeExpansionEvent event) {
				Widget item = findItem(event.getElement());
				if (item instanceof TreeItem) {
					initializeItem((TreeItem) item);
				}
			}
		});
	}

	/**
	 * Update element after a checkstate change.
	 * 
	 * @param element
	 */
	protected void doCheckStateChanged(Object element) {
		Widget item = findItem(element);
		if (item instanceof TreeItem) {
			TreeItem treeItem = (TreeItem) item;
			treeItem.setGrayed(false);
			updateChildrenItems(treeItem);
			updateParentItems(treeItem.getParentItem());
		}
	}

	/**
	 * Update elements after a checkstate change. This is identical to
	 * {@link #doCheckStateChanged(Object)}, but unifies the parent update of many
	 * checked siblings into a single update (instead of repeatedly updating the
	 * same parent) for performance reasons.
	 *
	 * @param elements tree elements
	 */
	private void doCheckStateChanged(Object[] elements) {
		HashSet<TreeItem> parents = new HashSet<>();
		for (Object element : elements) {
			Widget item = findItem(element);
			if (item instanceof TreeItem) {
				TreeItem treeItem = (TreeItem) item;
				treeItem.setGrayed(false);
				updateChildrenItems(treeItem);
				TreeItem parentItem = treeItem.getParentItem();
				// avoid updating the same parents repeatedly for big lists of siblings
				if (parentItem != null) {
					parents.add(parentItem);
				}
			}
		}
		for (TreeItem parent : parents) {
			updateParentItems(parent);
		}
	}

	/**
	 * The item has expanded. Updates the checked state of its children.
	 */
	private void initializeItem(TreeItem item) {
		if (item.getChecked() && !item.getGrayed()) {
			updateChildrenItems(item);
		}
	}

	/**
	 * Updates the check state of all created children
	 */
	private void updateChildrenItems(TreeItem parent) {
		Item[] children = getChildren(parent);
		boolean state = parent.getChecked();
		for (Item element : children) {
			TreeItem curr = (TreeItem) element;
			if (curr.getData() != null && ((curr.getChecked() != state) || curr.getGrayed())) {
				curr.setChecked(state);
				curr.setGrayed(false);
				updateChildrenItems(curr);
			}
		}
	}

	/**
	 * Updates the check / gray state of all parent items
	 */
	private void updateParentItems(TreeItem item) {
		if (item != null) {
			Item[] children = getChildren(item);
			boolean containsChecked = false;
			boolean containsUnchecked = false;
			for (Item element : children) {
				TreeItem curr = (TreeItem) element;
				boolean currChecked = curr.getChecked();
				containsChecked |= currChecked;
				// avoid fetching the grayed state, if we already found at least one grayed item
				if (!containsUnchecked) {
					containsUnchecked = !currChecked || curr.getGrayed();
				}
				// return as soon as both flags are set
				if (containsChecked && containsUnchecked) {
					break;
				}
			}

			// avoid accessing the widget, if there are no updates
			boolean updated = false;
			boolean checked = item.getChecked();
			if (checked != containsChecked) {
				item.setChecked(containsChecked);
				updated = true;
			}
			boolean grayed = item.getGrayed();
			boolean newGrayed = containsChecked && containsUnchecked;
			if (grayed != newGrayed) {
				item.setGrayed(newGrayed);
				updated = true;
			}

			// if this item did not change, no further parent will change
			if (updated) {
				updateParentItems(item.getParentItem());
			}
		}
	}

	@Override
	public boolean setChecked(Object element, boolean state) {
		if (super.setChecked(element, state)) {
			doCheckStateChanged(element);
			return true;
		}
		return false;
	}

	@Override
	public void setCheckedElements(Object[] elements) {
		Object[] oldCheckedElements = getCheckedElements();
		super.setCheckedElements(elements);

		Control tree = getControl();
		try {
			tree.setRedraw(false);
			if (oldCheckedElements.length > 0) {
				// calculate intersection of previously and newly checked elements to avoid
				// no-op updates
				HashSet<Object> changedElements = new HashSet<>(Arrays.asList(elements));
				for (Object element : oldCheckedElements) {
					changedElements.remove(element);
				}
				doCheckStateChanged(changedElements.toArray());
			} else {
				doCheckStateChanged(elements);
			}
		} finally {
			tree.setRedraw(true);
		}
	}

	@Override
	protected void setExpanded(Item item, boolean expand) {
		super.setExpanded(item, expand);
		if (expand && item instanceof TreeItem) {
			initializeItem((TreeItem) item);
		}
	}

	@Override
	public Object[] getCheckedElements() {
		Object[] checked = super.getCheckedElements();
		// add all items that are children of a checked node but not created yet
		ArrayList result = new ArrayList();
		for (Object curr : checked) {
			result.add(curr);
			Widget item = findItem(curr);
			if (item != null) {
				Item[] children = getChildren(item);
				// check if contains the dummy node
				if (children.length == 1 && children[0].getData() == null) {
					// not yet created
					collectChildren(curr, result);
				}
			}
		}
		return result.toArray();
	}

	/**
	 * Recursively add the filtered children of element to the result.
	 * 
	 * @param element
	 * @param result
	 */
	private void collectChildren(Object element, ArrayList result) {
		Object[] filteredChildren = getFilteredChildren(element);
		for (Object curr : filteredChildren) {
			result.add(curr);
			collectChildren(curr, result);
		}
	}

}
