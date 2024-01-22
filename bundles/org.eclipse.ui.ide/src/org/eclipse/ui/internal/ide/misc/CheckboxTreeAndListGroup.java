/*******************************************************************************
 * Copyright (c) 2000, 2018 IBM Corporation and others.
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
package org.eclipse.ui.internal.ide.misc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.commands.common.EventManager;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Tree;

/**
 *	Workbench-level composite that combines a CheckboxTreeViewer and CheckboxListViewer.
 *	All viewer selection-driven interactions are handled within this object
 */
public class CheckboxTreeAndListGroup extends EventManager implements
		ICheckStateListener, ISelectionChangedListener, ITreeViewerListener {
	private Object root;

	private Object currentTreeSelection;

	private List<Object> expandedTreeNodes = new ArrayList<>();

	private Map<Object, List<Object>> checkedStateStore = new HashMap<>(9);

	private List<Object> whiteCheckedTreeItems = new ArrayList<>();

	private ITreeContentProvider treeContentProvider;

	private IStructuredContentProvider listContentProvider;

	private ILabelProvider treeLabelProvider;

	private ILabelProvider listLabelProvider;

	private ViewerComparator treeComparator;

	private ViewerComparator listComparator;

	// widgets
	private CheckboxTreeViewer treeViewer;

	private CheckboxTableViewer listViewer;


	/**
	 *  Create an instance of this class.  Use this constructor if you wish to specify
	 *	the width and/or height of the combined widget (to only hardcode one of the
	 *	sizing dimensions, specify the other dimension's value as -1)
	 */
	public CheckboxTreeAndListGroup(Composite parent, Object rootObject,
			ITreeContentProvider treeContentProvider,
			ILabelProvider treeLabelProvider,
			IStructuredContentProvider listContentProvider,
			ILabelProvider listLabelProvider, int style, int width, int height) {

		this(parent, rootObject, treeContentProvider, treeLabelProvider, null,
				listContentProvider, listLabelProvider, null, style, width, height);
	}

	/**
	 *  Create an instance of this class.  Use this constructor if you wish to specify
	 *	the width and/or height of the combined widget (to only hardcode one of the
	 *	sizing dimensions, specify the other dimension's value as -1)
	 */
	public CheckboxTreeAndListGroup(Composite parent, Object rootObject,
			ITreeContentProvider treeContentProvider,
			ILabelProvider treeLabelProvider,
			ViewerComparator treeComparator,
			IStructuredContentProvider listContentProvider,
			ILabelProvider listLabelProvider,
			ViewerComparator listComparator,
			int style, int width, int height) {

		root = rootObject;
		this.treeContentProvider = treeContentProvider;
		this.listContentProvider = listContentProvider;
		this.treeLabelProvider = treeLabelProvider;
		this.listLabelProvider = listLabelProvider;
		this.treeComparator = treeComparator;
		this.listComparator = listComparator;
		createContents(parent, width, height, style);
	}

	/**
	 * This method must be called just before this window becomes visible.
	 */
	public void aboutToOpen() {
		determineWhiteCheckedDescendents(root);
		checkNewTreeElements(treeContentProvider.getElements(root));
		currentTreeSelection = null;

		//select the first element in the list
		Object[] elements = treeContentProvider.getElements(root);
		Object primary = elements.length > 0 ? elements[0] : null;
		if (primary != null) {
			treeViewer.setSelection(new StructuredSelection(primary));
		}
		treeViewer.getControl().setFocus();
	}

	/**
	 *	Add the passed listener to self's collection of clients
	 *	that listen for changes to element checked states
	 *
	 *	@param listener ICheckStateListener
	 */
	public void addCheckStateListener(ICheckStateListener listener) {
		addListenerObject(listener);
	}

	/**
	 *	Add the receiver and all of it's ancestors to the checkedStateStore if they
	 * are not already there.
	 */
	private void addToHierarchyToCheckedStore(Object treeElement) {

		// if this tree element is already gray then its ancestors all are as well
		if (!checkedStateStore.containsKey(treeElement)) {
			checkedStateStore.put(treeElement, new ArrayList<>());
		}

		Object parent = treeContentProvider.getParent(treeElement);
		if (parent != null) {
			addToHierarchyToCheckedStore(parent);
		}
	}

	/**
	 *	Return a boolean indicating whether all children of the passed tree element
	 *	are currently white-checked
	 *
	 *	@return boolean
	 *	@param treeElement java.lang.Object
	 */
	protected boolean areAllChildrenWhiteChecked(Object treeElement) {
		for (Object element : treeContentProvider.getChildren(treeElement)) {
			if (!whiteCheckedTreeItems.contains(element)) {
				return false;
			}
		}

		return true;
	}

	/**
	 *	Return a boolean indicating whether all list elements associated with
	 *	the passed tree element are currently checked
	 *
	 *	@return boolean
	 *	@param treeElement java.lang.Object
	 */
	protected boolean areAllElementsChecked(Object treeElement) {
		List<?> checkedElements = checkedStateStore.get(treeElement);
		if (checkedElements == null) {
			return false;
		}

		return getListItemsSize(treeElement) == checkedElements.size();
	}

	/**
	 *	Iterate through the passed elements which are being realized for the first
	 *	time and check each one in the tree viewer as appropriate
	 */
	protected void checkNewTreeElements(Object[] elements) {
		for (Object currentElement : elements) {
			boolean checked = checkedStateStore.containsKey(currentElement);
			treeViewer.setChecked(currentElement, checked);
			treeViewer.setGrayed(currentElement, checked
					&& !whiteCheckedTreeItems.contains(currentElement));
		}
	}

	/**
	 *	An item was checked in one of self's two views.  Determine which
	 *	view this occurred in and delegate appropriately
	 *
	 *	@param event CheckStateChangedEvent
	 */
	@Override
	public void checkStateChanged(final CheckStateChangedEvent event) {

		//Potentially long operation - show a busy cursor
		BusyIndicator.showWhile(treeViewer.getControl().getDisplay(),
				() -> {
					if (event.getCheckable().equals(treeViewer)) {
						treeItemChecked(event.getElement(), event
								.getChecked());
					} else {
						listItemChecked(event.getElement(), event
								.getChecked(), true);
					}

					notifyCheckStateChangeListeners(event);
				});
	}

	/**
	 *	Lay out and initialize self's visual components.
	 *
	 *	@param parent org.eclipse.swt.widgets.Composite
	 *	@param width int
	 *	@param height int
	 */
	protected void createContents(Composite parent, int width, int height,
			int style) {
		// group pane
		Composite composite = new Composite(parent, style);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.makeColumnsEqualWidth = true;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		composite.setFont(parent.getFont());

		createTreeViewer(composite, width / 2, height);
		createListViewer(composite, width / 2, height);

		initialize();
	}

	/**
	 *	Create this group's list viewer.
	 */
	protected void createListViewer(Composite parent, int width, int height) {
		listViewer = CheckboxTableViewer.newCheckList(parent, SWT.BORDER);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.widthHint = width;
		data.heightHint = height;
		listViewer.getTable().setLayoutData(data);
		listViewer.getTable().setFont(parent.getFont());
		listViewer.setContentProvider(listContentProvider);
		listViewer.setLabelProvider(listLabelProvider);
		listViewer.setComparator(listComparator);
		listViewer.addCheckStateListener(this);
	}

	/**
	 *	Create this group's tree viewer.
	 */
	protected void createTreeViewer(Composite parent, int width, int height) {
		Tree tree = new Tree(parent, SWT.CHECK | SWT.BORDER);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.widthHint = width;
		data.heightHint = height;
		tree.setLayoutData(data);
		tree.setFont(parent.getFont());

		treeViewer = new CheckboxTreeViewer(tree);
		treeViewer.setContentProvider(treeContentProvider);
		treeViewer.setLabelProvider(treeLabelProvider);
		treeViewer.setComparator(treeComparator);
		treeViewer.addTreeListener(this);
		treeViewer.addCheckStateListener(this);
		treeViewer.addSelectionChangedListener(this);
	}

	/**
	 * Returns a boolean indicating whether the passed tree element should be
	 * at LEAST gray-checked.  Note that this method does not consider whether
	 * it should be white-checked, so a specified tree item which should be
	 * white-checked will result in a <code>true</code> answer from this method.
	 * To determine whether a tree item should be white-checked use method
	 * #determineShouldBeWhiteChecked(Object).
	 *
	 * @param treeElement java.lang.Object
	 * @return boolean
	 * @see #determineShouldBeWhiteChecked(java.lang.Object)
	 */
	protected boolean determineShouldBeAtLeastGrayChecked(Object treeElement) {
		// if any list items associated with treeElement are checked then it
		// retains its gray-checked status regardless of its children
		List<?> checked = checkedStateStore.get(treeElement);
		if (checked != null && (!checked.isEmpty())) {
			return true;
		}

		// if any children of treeElement are still gray-checked then treeElement
		// must remain gray-checked as well
		for (Object child : treeContentProvider.getChildren(treeElement)) {
			if (checkedStateStore.containsKey(child)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Returns a boolean indicating whether the passed tree item should be
	 * white-checked.
	 *
	 * @return boolean
	 * @param treeElement java.lang.Object
	 */
	protected boolean determineShouldBeWhiteChecked(Object treeElement) {
		return areAllChildrenWhiteChecked(treeElement)
				&& areAllElementsChecked(treeElement);
	}

	/**
	 *	Recursively add appropriate tree elements to the collection of
	 *	known white-checked tree elements.
	 *
	 *	@param treeElement java.lang.Object
	 */
	protected void determineWhiteCheckedDescendents(Object treeElement) {
		// always go through all children first since their white-checked
		// statuses will be needed to determine the white-checked status for
		// this tree element
		for (Object child : treeContentProvider.getElements(treeElement)) {
			determineWhiteCheckedDescendents(child);
		}

		// now determine the white-checked status for this tree element
		if (determineShouldBeWhiteChecked(treeElement)) {
			setWhiteChecked(treeElement, true);
		}
	}

	/**
	 * Cause the tree viewer to expand all its items
	 */
	public void expandAll() {
		treeViewer.expandAll();
	}

	/**
	 *	Answer a flat collection of all of the checked elements in the
	 *	list portion of self
	 *
	 *	@return java.util.Vector
	 */
	public Iterator<?> getAllCheckedListItems() {
		List<Object> result = new ArrayList<>();
		Iterator<List<Object>> listCollectionsEnum = checkedStateStore.values().iterator();

		while (listCollectionsEnum.hasNext()) {
			Iterator<Object> currentCollection = listCollectionsEnum.next().iterator();
			while (currentCollection.hasNext()) {
				result.add(currentCollection.next());
			}
		}

		return result.iterator();
	}

	/**
	 *	Answer a collection of all of the checked elements in the tree portion
	 *	of self
	 *
	 *	@return java.util.Vector
	 */
	public Set<Object> getAllCheckedTreeItems() {
		return checkedStateStore.keySet();
	}

	/**
	 *	Answer the number of elements that have been checked by the
	 *	user.
	 *
	 *	@return int
	 */
	public int getCheckedElementCount() {
		return checkedStateStore.size();
	}

	/**
	 *	Return a count of the number of list items associated with a
	 *	given tree item.
	 *
	 *	@return int
	 *	@param treeElement java.lang.Object
	 */
	protected int getListItemsSize(Object treeElement) {
		Object[] elements = listContentProvider.getElements(treeElement);
		return elements.length;
	}

	/**
	 * Get the table the list viewer uses.
	 * @return org.eclipse.swt.widgets.Table
	 */
	public Table getListTable() {
		return this.listViewer.getTable();
	}

	/**
	 *	Logically gray-check all ancestors of treeItem by ensuring that they
	 *	appear in the checked table
	 */
	protected void grayCheckHierarchy(Object treeElement) {

		// if this tree element is already gray then its ancestors all are as well
		if (checkedStateStore.containsKey(treeElement)) {
			return; // no need to proceed upwards from here
		}

		checkedStateStore.put(treeElement, new ArrayList<>());
		if (determineShouldBeWhiteChecked(treeElement)) {
			setWhiteChecked(treeElement, true);
		}
		Object parent = treeContentProvider.getParent(treeElement);
		if (parent != null) {
			grayCheckHierarchy(parent);
		}
	}

	/**
	 *	Set the initial checked state of the passed list element to true.
	 *
	 *	@param element the element in the list to select
	 */
	public void initialCheckListItem(Object element) {
		Object parent = treeContentProvider.getParent(element);
		currentTreeSelection = parent;
		//As this is not done from the UI then set the box for updating from the selection to false
		listItemChecked(element, true, false);
		updateHierarchy(parent);
	}

	/**
	 *	Set the initial checked state of the passed element to true,
	 *	as well as to all of its children and associated list elements
	 *
	 *	@param element the element in the tree to select
	 */
	public void initialCheckTreeItem(Object element) {
		treeItemChecked(element, true);
	}

	/**
	 *	Initialize this group's viewers after they have been laid out.
	 */
	protected void initialize() {
		treeViewer.setInput(root);
	}

	/**
	 *	Callback that's invoked when the checked status of an item in the list
	 *	is changed by the user. Do not try and update the hierarchy if we are building the
	 *  initial list.
	 */
	protected void listItemChecked(Object listElement, boolean state,
			boolean updatingFromSelection) {
		List<Object> checkedListItems = checkedStateStore
				.get(currentTreeSelection);

		if (state) {
			if (checkedListItems == null) {
				// since the associated tree item has gone from 0 -> 1 checked
				// list items, tree checking may need to be updated
				grayCheckHierarchy(currentTreeSelection);
				checkedListItems = checkedStateStore
						.get(currentTreeSelection);
			}
			checkedListItems.add(listElement);
		} else {
			checkedListItems.remove(listElement);
			if (checkedListItems.isEmpty()) {
				// since the associated tree item has gone from 1 -> 0 checked
				// list items, tree checking may need to be updated
				ungrayCheckHierarchy(currentTreeSelection);
			}
		}

		if (updatingFromSelection) {
			updateHierarchy(currentTreeSelection);
		}
	}

	/**
	 *	Notify all checked state listeners that the passed element has had
	 *	its checked state changed to the passed state
	 */
	protected void notifyCheckStateChangeListeners(
			final CheckStateChangedEvent event) {
		for (Object listener : getListeners()) {
			final ICheckStateListener checkStateListener = (ICheckStateListener) listener;
			SafeRunner.run(new SafeRunnable() {
				@Override
				public void run() {
					checkStateListener.checkStateChanged(event);
				}
			});
		}
	}

	/**
	 *Set the contents of the list viewer based upon the specified selected
	 *tree element.  This also includes checking the appropriate list items.
	 *
	 *@param treeElement java.lang.Object
	 */
	protected void populateListViewer(final Object treeElement) {
		listViewer.setInput(treeElement);
		List<Object> listItemsToCheck = checkedStateStore.get(treeElement);

		if (listItemsToCheck != null) {
			Iterator<Object> listItemsEnum = listItemsToCheck.iterator();
			while (listItemsEnum.hasNext()) {
				listViewer.setChecked(listItemsEnum.next(), true);
			}
		}
	}

	/**
	 *	Remove the passed listener from self's collection of clients
	 *	that listen for changes to element checked states
	 *
	 *	@param listener ICheckStateListener
	 */
	public void removeCheckStateListener(ICheckStateListener listener) {
		removeListenerObject(listener);
	}

	/**
	 *	Handle the selection of an item in the tree viewer
	 *
	 *	@param event SelectionChangedEvent
	 */
	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		IStructuredSelection selection = event.getStructuredSelection();
		Object selectedElement = selection.getFirstElement();
		if (selectedElement == null) {
			currentTreeSelection = null;
			listViewer.setInput(currentTreeSelection);
			return;
		}

		// ie.- if not an item deselection
		if (selectedElement != currentTreeSelection) {
			populateListViewer(selectedElement);
		}

		currentTreeSelection = selectedElement;
	}

	/**
	 * Select or deselect all of the elements in the tree depending on the value of the selection
	 * boolean. Be sure to update the displayed files as well.
	 *
	 * @param selection boolean indicating whether or not to select all elements
	 */
	public void setAllSelections(final boolean selection) {

		//Potentially long operation - show a busy cursor
		BusyIndicator.showWhile(treeViewer.getControl().getDisplay(),
				() -> {
					setTreeChecked(root, selection);
					listViewer.setAllChecked(selection);
				});
	}

	/**
	 *	Set the list viewer's providers to those passed
	 *
	 *	@param contentProvider ITreeContentProvider
	 *	@param labelProvider ILabelProvider
	 */
	public void setListProviders(IStructuredContentProvider contentProvider,
			ILabelProvider labelProvider) {
		listViewer.setContentProvider(contentProvider);
		listViewer.setLabelProvider(labelProvider);
	}

	/**
	 *	Set the comparator that is to be applied to self's list viewer
	 *
	 *	@param comparator the comparator for the list viewer
	 */
	public void setListComparator(ViewerComparator comparator) {
		listViewer.setComparator(comparator);
	}

	/**
	 * Set the root of the widget to be new Root. Regenerate all of the tables and lists from this
	 * value.
	 */
	public void setRoot(Object newRoot) {
		this.root = newRoot;
		initialize();
	}

	/**
	 *	Set the checked state of the passed tree element appropriately, and
	 *	do so recursively to all of its child tree elements as well
	 */
	protected void setTreeChecked(Object treeElement, boolean state) {

		if (treeElement.equals(currentTreeSelection)) {
			listViewer.setAllChecked(state);
		}

		if (state) {
			List<Object> listItemsChecked = new ArrayList<>(
					Arrays.asList(listContentProvider.getElements(treeElement)));
			checkedStateStore.put(treeElement, listItemsChecked);
		} else {
			checkedStateStore.remove(treeElement);
		}

		setWhiteChecked(treeElement, state);
		treeViewer.setChecked(treeElement, state);
		treeViewer.setGrayed(treeElement, false);

		// now logically check/uncheck all children as well
		for (Object child : treeContentProvider.getChildren(treeElement)) {
			setTreeChecked(child, state);
		}
	}

	/**
	 *	Set the tree viewer's providers to those passed
	 *
	 *	@param contentProvider ITreeContentProvider
	 *	@param labelProvider ILabelProvider
	 */
	public void setTreeProviders(ITreeContentProvider contentProvider,
			ILabelProvider labelProvider) {
		treeViewer.setContentProvider(contentProvider);
		treeViewer.setLabelProvider(labelProvider);
	}

	/**
	 *	Set the comparator that is to be applied to self's tree viewer
	 *
	 *	@param comparator the comparator for the tree
	 */
	public void setTreeComparator(ViewerComparator comparator) {
		treeViewer.setComparator(comparator);
	}

	/**
	 *	Adjust the collection of references to white-checked tree elements appropriately.
	 *
	 *	@param treeElement java.lang.Object
	 *	@param isWhiteChecked boolean
	 */
	protected void setWhiteChecked(Object treeElement, boolean isWhiteChecked) {
		if (isWhiteChecked) {
			if (!whiteCheckedTreeItems.contains(treeElement)) {
				whiteCheckedTreeItems.add(treeElement);
			}
		} else {
			whiteCheckedTreeItems.remove(treeElement);
		}
	}

	/**
	 *	Handle the collapsing of an element in a tree viewer
	 */
	@Override
	public void treeCollapsed(TreeExpansionEvent event) {
		// We don't need to do anything with this
	}

	/**
	 *	Handle the expansionsion of an element in a tree viewer
	 */
	@Override
	public void treeExpanded(TreeExpansionEvent event) {

		Object item = event.getElement();

		// First see if the children need to be given their checked state at all.  If they've
		// already been realized then this won't be necessary
		if (!expandedTreeNodes.contains(item)) {
			expandedTreeNodes.add(item);
			checkNewTreeElements(treeContentProvider.getChildren(item));
		}
	}

	/**
	 *  Callback that's invoked when the checked status of an item in the tree
	 *  is changed by the user.
	 */
	protected void treeItemChecked(Object treeElement, boolean state) {

		// recursively adjust all child tree elements appropriately
		setTreeChecked(treeElement, state);

		Object parent = treeContentProvider.getParent(treeElement);
		if (parent == null) {
			return;
		}

		// now update upwards in the tree hierarchy
		if (state) {
			grayCheckHierarchy(parent);
		} else {
			ungrayCheckHierarchy(parent);
		}

		updateHierarchy(treeElement);
	}

	/**
	 *	Logically un-gray-check all ancestors of treeItem iff appropriate.
	 */
	protected void ungrayCheckHierarchy(Object treeElement) {
		if (!determineShouldBeAtLeastGrayChecked(treeElement)) {
			checkedStateStore.remove(treeElement);
		}

		Object parent = treeContentProvider.getParent(treeElement);
		if (parent != null) {
			ungrayCheckHierarchy(parent);
		}
	}

	/**
	 *	Set the checked state of self and all ancestors appropriately
	 */
	protected void updateHierarchy(Object treeElement) {

		boolean whiteChecked = determineShouldBeWhiteChecked(treeElement);
		boolean shouldBeAtLeastGray = determineShouldBeAtLeastGrayChecked(treeElement);

		treeViewer.setChecked(treeElement, shouldBeAtLeastGray);
		setWhiteChecked(treeElement, whiteChecked);
		if (!whiteChecked) {
			treeViewer.setGrayed(treeElement, shouldBeAtLeastGray);
		}

		// proceed up the tree element hierarchy
		Object parent = treeContentProvider.getParent(treeElement);
		if (parent != null) {
			updateHierarchy(parent);
		}
	}

	/**
	 * Update the selections of the tree elements in items to reflect the new
	 * selections provided.
	 * @param items Map with keys of Object (the tree element) and values of List (the selected
	 * list elements).
	 */
	public void updateSelections(final Map<Object, List<Object>> items) {
		// Potentially long operation - show a busy cursor
		BusyIndicator.showWhile(treeViewer.getControl().getDisplay(),
				() -> {
					//Update the store before the hierarchy to prevent updating parents before all of the children are done
					for (Entry<?, List<Object>> entry : items.entrySet()) {
						Object key1 = entry.getKey();
						//Replace the items in the checked state store with those from the supplied items
						List<Object> selections = entry.getValue();
						if (selections.isEmpty()) {
							//If it is empty remove it from the list
							checkedStateStore.remove(key1);
						} else {
							checkedStateStore.put(key1, selections);
							// proceed up the tree element hierarchy
							Object parent = treeContentProvider
									.getParent(key1);
							if (parent != null) {
								addToHierarchyToCheckedStore(parent);
							}
						}
					}

					//Now update hierarchies
					for (Entry<Object, List<Object>> entry : items.entrySet()) {
						Object key2 = entry.getKey();
						updateHierarchy(key2);
						if (currentTreeSelection != null
								&& currentTreeSelection.equals(key2)) {
							listViewer.setAllChecked(false);
							listViewer.setCheckedElements(entry.getValue().toArray());
						}
					}
				});

	}
}
