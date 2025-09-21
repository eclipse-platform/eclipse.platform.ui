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
package org.eclipse.ui.internal.editors.text;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Tree;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.SafeRunner;

import org.eclipse.core.resources.IContainer;

import org.eclipse.jface.dialogs.Dialog;
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

/*
 * XXX: This is an copy of the internal ResourceTreeAndListGroup class, see http://bugs.eclipse.org/147027
 */

/**
 * Workbench-level composite that combines a CheckboxTreeViewer and
 * CheckboxListViewer. All viewer selection-driven interactions are handled
 * within this object
 */
class SelectResourcesBlock implements ICheckStateListener, ISelectionChangedListener, ITreeViewerListener {


	/**
	 * The IElementFilter is a interface that defines
	 * the API for filtering the current selection of
	 * a ResourceTreeAndListGroup in order to find a
	 * subset to update as the result of a type filtering.
	 * This is meant as an internal class and is used exclusively
	 * by the import dialog.
	 */
	interface IElementFilter {

		void filterElements(Collection<Object> elements) throws InterruptedException;

		void filterElements(Object[] elements) throws InterruptedException;
	}


	private final Object root;

	private Object currentTreeSelection;

	private Collection<Object> expandedTreeNodes= new HashSet<>();

	private Map<Object, List<Object>> checkedStateStore= new HashMap<>(9);

	private Collection<Object> whiteCheckedTreeItems= new HashSet<>();

	private final ListenerList<ICheckStateListener> listeners= new ListenerList<>(ListenerList.IDENTITY);

	private final ITreeContentProvider treeContentProvider;

	private final IStructuredContentProvider listContentProvider;

	private final ILabelProvider treeLabelProvider;

	private final ILabelProvider listLabelProvider;

	// widgets
	private CheckboxTreeViewer treeViewer;

	private CheckboxTableViewer listViewer;

	//height hint for viewers
	private static int PREFERRED_HEIGHT= 150;

	/**
	 * Create an instance of this class. Use this constructor if you wish to specify the width
	 * and/or height of the combined widget (to only hard code one of the sizing dimensions, specify
	 * the other dimension's value as -1)
	 *
	 * @param parent the parent composite
	 * @param rootObject the root object
	 * @param treeContentProvider the tree content provider
	 * @param treeLabelProvider the tree label provider
	 * @param listContentProvider the list content provider
	 * @param listLabelProvider the list label provider
	 * @param style the style flags for the new Composite
	 * @param useHeightHint If true then use the height hint to make this group big enough
	 */
	public SelectResourcesBlock(Composite parent, Object rootObject, ITreeContentProvider treeContentProvider, ILabelProvider treeLabelProvider, IStructuredContentProvider listContentProvider, ILabelProvider listLabelProvider, int style, boolean useHeightHint) {

		root= rootObject;
		this.treeContentProvider= treeContentProvider;
		this.listContentProvider= listContentProvider;
		this.treeLabelProvider= treeLabelProvider;
		this.listLabelProvider= listLabelProvider;
		createContents(parent, style, useHeightHint);
	}

	/**
	 * Add the passed listener to self's collection of clients that listen for
	 * changes to element checked states
	 *
	 * @param listener ICheckStateListener
	 */
	public void addCheckStateListener(ICheckStateListener listener) {
		listeners.add(listener);
	}

	/**
	 * Iterates over the passed elements which are being realized for the
	 * first time and check each one in the tree viewer as appropriate.
	 *
	 * @param elements the elements
	 */
	private void checkNewTreeElements(Object[] elements) {
		for (Object currentElement : elements) {
			boolean checked= checkedStateStore.containsKey(currentElement);
			treeViewer.setChecked(currentElement, checked);
			treeViewer.setGrayed(currentElement, checked && !whiteCheckedTreeItems.contains(currentElement));
		}
	}

	/**
	 * An item was checked in one of self's two views. Determine which view this
	 * occurred in and delegate appropriately
	 *
	 * @param event the check state event
	 * @see org.eclipse.jface.viewers.ICheckStateListener#checkStateChanged(org.eclipse.jface.viewers.CheckStateChangedEvent)
	 */
	@Override
	public void checkStateChanged(final CheckStateChangedEvent event) {

		//Potentially long operation - show a busy cursor
		BusyIndicator.showWhile(treeViewer.getControl().getDisplay(), () -> {
			if (event.getCheckable().equals(treeViewer)) {
				treeItemChecked(event.getElement(), event.getChecked());
			} else {
				listItemChecked(event.getElement(), event.getChecked(), true);
			}

			notifyCheckStateChangeListeners(event);
		});
	}

	/**
	 * Lay out and initialize self's visual components.
	 *
	 * @param parent org.eclipse.swt.widgets.Composite
	 * @param style the style flags for the new Composite
	 * @param useHeightHint If true use the preferredHeight.
	 */
	private void createContents(Composite parent, int style, boolean useHeightHint) {
		// group pane
		Composite composite= new Composite(parent, style);
		composite.setFont(parent.getFont());
		GridLayout layout= new GridLayout();
		layout.numColumns= 2;
		layout.makeColumnsEqualWidth= true;
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		createTreeViewer(composite, useHeightHint);
		createListViewer(composite, useHeightHint);

		initialize();
	}

	/**
	 * Creates this block's list viewer.
	 *
	 * @param parent the parent control
	 * @param useHeightHint if <code>true</code> use the height hints
	 */
	private void createListViewer(Composite parent, boolean useHeightHint) {
		listViewer= CheckboxTableViewer.newCheckList(parent, SWT.BORDER);
		GridData data= new GridData(GridData.FILL_BOTH);
		if (useHeightHint) {
			data.heightHint= PREFERRED_HEIGHT;
		}
		data.widthHint= getViewerWidthHint(parent);
		listViewer.getTable().setLayoutData(data);
		listViewer.getTable().setFont(parent.getFont());
		listViewer.setContentProvider(listContentProvider);
		listViewer.setLabelProvider(listLabelProvider);
		listViewer.addCheckStateListener(this);
	}

	/**
	 * Create this block's tree viewer.
	 *
	 * @param parent the parent control
	 * @param useHeightHint if <code>true</code> use the height hints
	 */
	private void createTreeViewer(Composite parent, boolean useHeightHint) {
		Tree tree= new Tree(parent, SWT.CHECK | SWT.BORDER);
		GridData data= new GridData(GridData.FILL_BOTH);
		if (useHeightHint) {
			data.heightHint= PREFERRED_HEIGHT;
		}
		data.widthHint= getViewerWidthHint(parent);
		tree.setLayoutData(data);
		tree.setFont(parent.getFont());

		treeViewer= new CheckboxTreeViewer(tree);
		treeViewer.setUseHashlookup(true);
		treeViewer.setContentProvider(treeContentProvider);
		treeViewer.setLabelProvider(treeLabelProvider);
		treeViewer.addTreeListener(this);
		treeViewer.addCheckStateListener(this);
		treeViewer.addSelectionChangedListener(this);
	}

	private int getViewerWidthHint(Control control) {
		GC gc= new GC(control);
		int width= Dialog.convertWidthInCharsToPixels(gc.getFontMetrics(), 50);
		gc.dispose();
		return width;
	}

	/**
	 * Returns a boolean indicating whether the passed tree element should be at
	 * LEAST gray-checked. Note that this method does not consider whether it
	 * should be white-checked, so a specified tree item which should be
	 * white-checked will result in a <code>true</code> answer from this
	 * method.
	 *
	 * @param treeElement java.lang.Object
	 * @return boolean
	 */
	private boolean determineShouldBeAtLeastGrayChecked(Object treeElement) {
		// if any list items associated with treeElement are checked then it
		// retains its gray-checked status regardless of its children
		List<Object> checked= checkedStateStore.get(treeElement);
		if (checked != null && (!checked.isEmpty())) {
			return true;
		}

		// if any children of treeElement are still gray-checked then
		// treeElement
		// must remain gray-checked as well. Only ask expanded nodes
		if (expandedTreeNodes.contains(treeElement)) {
			for (Object element : treeContentProvider.getChildren(treeElement)) {
				if (checkedStateStore.containsKey(element)) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Expands an element in a tree viewer.
	 *
	 * @param element the element to be expanded
	 */
	private void expandTreeElement(final Object element) {
		BusyIndicator.showWhile(treeViewer.getControl().getDisplay(), () -> {

			// First see if the children need to be given their checked
			// state at all. If they've
			// already been realized then this won't be necessary
			if (expandedTreeNodes.contains(element)) {
				checkNewTreeElements(treeContentProvider.getChildren(element));
			} else {

				expandedTreeNodes.add(element);
				if (whiteCheckedTreeItems.contains(element)) {
					//If this is the first expansion and this is a white
					// checked node then check the children
					for (Object child : treeContentProvider.getChildren(element)) {
						if (!whiteCheckedTreeItems.contains(child)) {
							setWhiteChecked(child, true);
							treeViewer.setChecked(child, true);
							checkedStateStore.put(child, new ArrayList<>());
						}
					}

					//Now be sure to select the list of items too
					setListForWhiteSelection(element);
				}
			}

		});
	}

	/**
	 * Adds all of the selected children of <code>treeElement</code> to result recursively. This
	 * does not set any values in the checked state.
	 *
	 * @param  treeElement the tree element being queried
	 * @param parentLabel the parent label
	 * @param addAll a boolean to indicate if the checked state store needs to be queried
	 * @param filter the filter being used on the data
	 * @throws InterruptedException in case of interruption
	 */
	private void findAllSelectedListElements(Object treeElement, String parentLabel, boolean addAll, IElementFilter filter) throws InterruptedException {

		String fullLabel= null;

		if (addAll) {
			filter.filterElements(listContentProvider.getElements(treeElement));
		} else { //Add what we have stored
			if (checkedStateStore.containsKey(treeElement)) {
				filter.filterElements(checkedStateStore.get(treeElement));
			}
		}

		Object[] treeChildren= treeContentProvider.getChildren(treeElement);
		for (Object child : treeChildren) {
			if (addAll) {
				findAllSelectedListElements(child, fullLabel, true, filter);
			} else { //Only continue for those with checked state
				if (checkedStateStore.containsKey(child)) {
					findAllSelectedListElements(child, fullLabel, whiteCheckedTreeItems.contains(child), filter);
				}
			}

		}
	}

	/**
	 * Find all of the white checked children of the treeElement and add them to
	 * the collection. If the element itself is white select add it. If not then
	 * add any selected list elements and recurse down to the children.
	 *
	 * @param treeElement java.lang.Object
	 * @param result java.util.Collection
	 */
	private void findAllWhiteCheckedItems(Object treeElement, Collection<Object> result) {

		if (whiteCheckedTreeItems.contains(treeElement)) {
			result.add(treeElement);
		} else {
			Collection<Object> listChildren= checkedStateStore.get(treeElement);
			//if it is not in the store then it and its children are not interesting
			if (listChildren == null) {
				return;
			}
			result.addAll(listChildren);
			for (Object element : treeContentProvider.getChildren(treeElement)) {
				findAllWhiteCheckedItems(element, result);
			}
		}
	}

	/**
	 * Returns a flat list of all of the leaf elements which are checked. Filter
	 * then based on the supplied ElementFilter. If monitor is canceled then
	 * return null
	 *
	 * @param filter - the filter for the data
	 * @throws InterruptedException in case of interruption
	 */
	private void getAllCheckedListItems(IElementFilter filter) throws InterruptedException {
		//Loop through the children of the root as the root is not in the store
		for (Object element : treeContentProvider.getChildren(root)) {
			findAllSelectedListElements(element, null, whiteCheckedTreeItems.contains(element), filter);
		}
	}

	/**
	 * Returns a flat list of all of the leaf elements which are checked.
	 *
	 * @return all of the leaf elements which are checked. This API does not
	 *         return null in order to keep backwards compatibility.
	 */
	public List<Object> getAllCheckedListItems() {

		final ArrayList<Object> returnValue= new ArrayList<>();

		IElementFilter passThroughFilter= new IElementFilter() {

			@Override
			public void filterElements(Collection<Object> elements) throws InterruptedException {
				returnValue.addAll(elements);
			}

			@Override
			public void filterElements(Object[] elements) throws InterruptedException {
				Collections.addAll(returnValue, elements);
			}
		};

		try {
			getAllCheckedListItems(passThroughFilter);
		} catch (InterruptedException exception) {
			return new ArrayList<>();
		}
		return returnValue;

	}

	/**
	 * Returns a list of all of the items that are white checked. Any folders
	 * that are white checked are added and then any files from white checked
	 * folders are added.
	 *
	 * @return the list of all of the items that are white checked
	 */
	public List<Object> getAllWhiteCheckedItems() {

		List<Object> result= new ArrayList<>();

		//Loop through the children of the root as the root is not in the
		// store
		for (Object element : treeContentProvider.getChildren(root)) {
			findAllWhiteCheckedItems(element, result);
		}

		return result;
	}

	/**
	 * Logically gray-check all ancestors of <code>treeElement</code> by ensuring that they
	 * appear in the checked table.
	 *
	 * @param treeElement the tree element
	 */
	private void grayCheckHierarchy(Object treeElement) {

		//expand the element first to make sure we have populated for it
		expandTreeElement(treeElement);

		// if this tree element is already gray then its ancestors all are as
		// well
		if (checkedStateStore.containsKey(treeElement)) {
			return; // no need to proceed upwards from here
		}

		checkedStateStore.put(treeElement, new ArrayList<>());
		Object parent= treeContentProvider.getParent(treeElement);
		if (parent != null) {
			grayCheckHierarchy(parent);
		}
	}

	/**
	 * Set the checked state of self and all ancestors appropriately. Do not
	 * white check anyone - this is only done down a hierarchy.
	 *
	 * @param treeElement the tree element
	 */
	private void grayUpdateHierarchy(Object treeElement) {

		boolean shouldBeAtLeastGray= determineShouldBeAtLeastGrayChecked(treeElement);

		treeViewer.setGrayChecked(treeElement, shouldBeAtLeastGray);

		if (whiteCheckedTreeItems.contains(treeElement)) {
			whiteCheckedTreeItems.remove(treeElement);
		}

		// proceed up the tree element hierarchy
		Object parent= treeContentProvider.getParent(treeElement);
		if (parent != null) {
			grayUpdateHierarchy(parent);
		}
	}

	public void selectAndReveal(Object treeElement) {
		treeViewer.reveal(treeElement);
		IStructuredSelection selection= new StructuredSelection(treeElement);
		treeViewer.setSelection(selection);
	}

	/**
	 * Initialize this group's viewers after they have been laid out.
	 */
	private void initialize() {
		treeViewer.setInput(root);
		this.expandedTreeNodes= new ArrayList<>();
		this.expandedTreeNodes.add(root);
		Object[] topElements= treeContentProvider.getElements(root);
		if (topElements.length == 1) {
			treeViewer.setExpandedState(topElements[0], true);
		}
	}

	/**
	 * Callback that's invoked when the checked status of an item in the list is
	 * changed by the user. Do not try and update the hierarchy if we are
	 * building the initial list.
	 *
	 * @param listElement the list element
	 * @param state the checked state
	 * @param updatingFromSelection <code>true</code> if we are inside an
	 *            update caused by selection
	 */
	private void listItemChecked(Object listElement, boolean state, boolean updatingFromSelection) {
		List<Object> checkedListItems= checkedStateStore.get(currentTreeSelection);
		//If it has not been expanded do so as the selection of list items will
		// affect gray state
		if (!expandedTreeNodes.contains(currentTreeSelection)) {
			expandTreeElement(currentTreeSelection);
		}

		if (state) {
			if (checkedListItems == null) {
				// since the associated tree item has gone from 0 -> 1 checked
				// list items, tree checking may need to be updated
				grayCheckHierarchy(currentTreeSelection);
				checkedListItems= checkedStateStore.get(currentTreeSelection);
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

		//Update the list with the selections if there are any
		if (!checkedListItems.isEmpty()) {
			checkedStateStore.put(currentTreeSelection, checkedListItems);
		}
		if (updatingFromSelection) {
			grayUpdateHierarchy(currentTreeSelection);
		}
	}

	/**
	 * Notify all checked state listeners with the given event.
	 *
	 * @param event the event
	 */
	private void notifyCheckStateChangeListeners(final CheckStateChangedEvent event) {
		for (ICheckStateListener l : listeners) {
			SafeRunner.run(new SafeRunnable() {
				@Override
				public void run() {
					l.checkStateChanged(event);
				}
			});
		}
	}

	/**
	 * Set the contents of the list viewer based upon the specified selected
	 * tree element. This also includes checking the appropriate list items.
	 *
	 * @param treeElement java.lang.Object
	 */
	private void populateListViewer(final Object treeElement) {
		listViewer.setInput(treeElement);

		//If the element is white checked but not expanded we have not set up
		// all of the children yet
		if (!(expandedTreeNodes.contains(treeElement)) && whiteCheckedTreeItems.contains(treeElement)) {

			//Potentially long operation - show a busy cursor
			BusyIndicator.showWhile(treeViewer.getControl().getDisplay(), () -> {
				setListForWhiteSelection(treeElement);
				listViewer.setAllChecked(true);
			});

		} else {
			List<Object> listItemsToCheck= checkedStateStore.get(treeElement);

			if (listItemsToCheck != null) {
				Iterator<Object> listItemsEnum= listItemsToCheck.iterator();
				while (listItemsEnum.hasNext()) {
					listViewer.setChecked(listItemsEnum.next(), true);
				}
			}
		}
	}

	/**
	 * Logically gray-check all ancestors of <code>item</code> by ensuring
	 * that they appear in the checked table. Add any elements to the
	 * <code>selectedNodes</code> so we can track that has been done.
	 *
	 * @param item the tree item
	 * @param selectedNodes the set of selected nodes
	 */
	private void primeHierarchyForSelection(Object item, Set<Object> selectedNodes) {

		//Only prime it if we haven't visited yet
		if (selectedNodes.contains(item)) {
			return;
		}

		checkedStateStore.put(item, new ArrayList<>());

		//mark as expanded as we are going to populate it after this
		expandedTreeNodes.add(item);
		selectedNodes.add(item);

		Object parent= treeContentProvider.getParent(item);
		if (parent != null) {
			primeHierarchyForSelection(parent, selectedNodes);
		}
	}

	/**
	 * Remove the passed listener from self's collection of clients that listen
	 * for changes to element checked states
	 *
	 * @param listener ICheckStateListener
	 */
	public void removeCheckStateListener(ICheckStateListener listener) {
		listeners.remove(listener);
	}

	/**
	 * Handle the selection of an item in the tree viewer
	 *
	 * @param event SelectionChangedEvent
	 */
	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		IStructuredSelection selection= event.getStructuredSelection();
		Object selectedElement= selection.getFirstElement();
		if (selectedElement == null) {
			currentTreeSelection= null;
			listViewer.setInput(null);
			return;
		}

		// i.e.- if not an item deselection
		if (selectedElement != currentTreeSelection) {
			populateListViewer(selectedElement);
		}

		currentTreeSelection= selectedElement;
	}

	/**
	 * Select or deselect all of the elements in the tree depending on the value
	 * of <code>selection</code>. Be sure to update the displayed files as
	 * well.
	 *
	 * @param selection <code>true</code> if selection should be set,
	 *            <code>false</code> if it should be removed
	 */
	public void setAllSelections(final boolean selection) {

		//If there is no root there is nothing to select
		if (root == null) {
			return;
		}

		//Potentially long operation - show a busy cursor
		BusyIndicator.showWhile(treeViewer.getControl().getDisplay(), () -> {
			setTreeChecked(root, selection);
			listViewer.setAllChecked(selection);
		});
	}

	public void refresh() {
		//Potentially long operation - show a busy cursor
		BusyIndicator.showWhile(treeViewer.getControl().getDisplay(), () -> {
			treeViewer.refresh();
			populateListViewer(currentTreeSelection);
		});
	}

	/**
	 * The treeElement has been white selected. Get the list for the element and
	 * set it in the checked state store.
	 *
	 * @param treeElement the element being updated
	 */
	private void setListForWhiteSelection(Object treeElement) {

		Object[] listItems= listContentProvider.getElements(treeElement);
		List<Object> listItemsChecked= new ArrayList<>();
		Collections.addAll(listItemsChecked, listItems);

		checkedStateStore.put(treeElement, listItemsChecked);
	}

	/**
	 * Set the <code>comparator</code> that is to be applied to self's list viewer
	 *
	 * @param comparator the comparator to be set
	 */
	public void setListComparator(ViewerComparator comparator) {
		listViewer.setComparator(comparator);
	}

	/**
	 * Set the checked state of the passed <code>treeElement</code>
	 * appropriately, and do so recursively to all of its child tree elements as
	 * well.
	 *
	 * @param treeElement the root of the subtree
	 * @param state <code>true</code> if checked, <code>false</code> otherwise
	 */
	private void setTreeChecked(Object treeElement, boolean state) {

		if (treeElement.equals(currentTreeSelection)) {
			listViewer.setAllChecked(state);
		}

		if (state) {
			setListForWhiteSelection(treeElement);
		} else {
			checkedStateStore.remove(treeElement);
		}

		setWhiteChecked(treeElement, state);
		treeViewer.setChecked(treeElement, state);
		treeViewer.setGrayed(treeElement, false);

		// now logically check/uncheck all children as well if it has been
		// expanded
		if (expandedTreeNodes.contains(treeElement)) {
			for (Object element : treeContentProvider.getChildren(treeElement)) {
				setTreeChecked(element, state);
			}
		}
	}

	/**
	 * Set the comparator that is to be applied to self's tree viewer.
	 *
	 * @param comparator the comparator to be set
	 */
	public void setTreeComparator(ViewerComparator comparator) {
		treeViewer.setComparator(comparator);
	}

	/**
	 * Adjust the collection of references to white-checked tree elements
	 * appropriately.
	 *
	 * @param treeElement the root of the subtree
	 * @param isWhiteChecked <code>true</code> if white checked,
	 *            <code>false</code> otherwise
	 */
	private void setWhiteChecked(Object treeElement, boolean isWhiteChecked) {
		if (isWhiteChecked) {
			if (!whiteCheckedTreeItems.contains(treeElement)) {
				whiteCheckedTreeItems.add(treeElement);
			}
		} else {
			whiteCheckedTreeItems.remove(treeElement);
		}
	}

	/**
	 * Handle the collapsing of an element in a tree viewer.
	 *
	 * @param event the collapse event
	 */
	@Override
	public void treeCollapsed(TreeExpansionEvent event) {
		// We don't need to do anything with this
	}

	/**
	 * Handle the expansion of an element in a tree viewer.
	 *
	 * @param event the expansion event
	 */
	@Override
	public void treeExpanded(TreeExpansionEvent event) {
		expandTreeElement(event.getElement());
	}

	/**
	 * Callback that's invoked when the checked status of an item in the tree is
	 * changed by the user.
	 *
	 * @param treeElement the tree element that has been checked/unchecked
	 * @param state <code>true</code> if checked, <code>false</code> if
	 *            unchecked
	 */
	private void treeItemChecked(Object treeElement, boolean state) {

		// recursively adjust all child tree elements appropriately
		setTreeChecked(treeElement, state);

		Object parent= treeContentProvider.getParent(treeElement);
		if (parent == null) {
			return;
		}

		// now update upwards in the tree hierarchy
		if (state) {
			grayCheckHierarchy(parent);
		} else {
			ungrayCheckHierarchy(parent);
		}

		//Update the hierarchy but do not white select the parent
		grayUpdateHierarchy(parent);
	}

	/**
	 * Logically un-gray-check all ancestors of <code>treeElement</code> if
	 * appropriate.
	 *
	 * @param treeElement the root of the subtree
	 */
	private void ungrayCheckHierarchy(Object treeElement) {
		if (!determineShouldBeAtLeastGrayChecked(treeElement)) {
			checkedStateStore.remove(treeElement);
		}

		Object parent= treeContentProvider.getParent(treeElement);
		if (parent != null) {
			ungrayCheckHierarchy(parent);
		}
	}

	/**
	 * Update the selections of the tree elements in items to reflect the new
	 * selections provided.
	 *
	 * @param items Map with keys of Object (the tree element) and values of
	 *            List (the selected list elements). NOTE: This method does not
	 *            special case keys with no values (i.e., a tree element with an
	 *            empty list). If a tree element does not have any selected
	 *            items, do not include the element in the Map.
	 */
	public void updateSelections(Map<IContainer, List<Object>> items) {
		// We are replacing all selected items with the given selected items,
		// so reinitialize everything.
		this.listViewer.setAllChecked(false);
		this.treeViewer.setCheckedElements(new Object[0]);
		this.whiteCheckedTreeItems= new HashSet<>();
		Set<Object> selectedNodes= new HashSet<>();
		checkedStateStore= new HashMap<>();

		// Update the store before the hierarchy to prevent updating parents
		// before all of the children are done

		for (Entry<IContainer, List<Object>> entry : items.entrySet()) {
			Object key = entry.getKey();
			primeHierarchyForSelection(key, selectedNodes);
			checkedStateStore.put(key, entry.getValue());
		}


		// Update the checked tree items. Since each tree item has a selected
		// item, all the tree items will be gray checked.
		treeViewer.setCheckedElements(checkedStateStore.keySet().toArray());
		treeViewer.setGrayedElements(checkedStateStore.keySet().toArray());

		// Update the listView of the currently selected tree item.
		if (currentTreeSelection != null) {
			Object displayItems= items.get(currentTreeSelection);
			if (displayItems != null) {
				listViewer.setCheckedElements(((List<?>) displayItems).toArray());
			}
		}
	}
}
