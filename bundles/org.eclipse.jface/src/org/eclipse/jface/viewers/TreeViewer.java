/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Tom Schindl <tom.schindl@bestsolution.at> - concept of ViewerRow,
 *                                                 refactoring (bug 153993), bug 167323, 191468, 205419
 *     Matthew Hall - bug 221988
 *     Pawel Piech, WindRiver - bug 296573
 *     Lars Vogel <Lars.Vogel@gmail.com> - Bug 430873
 *******************************************************************************/

package org.eclipse.jface.viewers;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.util.Policy;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.events.TreeListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;

/**
 * A concrete viewer based on an SWT <code>Tree</code> control.
 * <p>
 * This class is not intended to be subclassed outside the viewer framework. It
 * is designed to be instantiated with a pre-existing SWT tree control and
 * configured with a domain-specific content provider, label provider, element
 * filter (optional), and element sorter (optional).
 * </p>
 * <p>
 * As of 3.2, TreeViewer supports multiple equal elements (each with a
 * different parent chain) in the tree. This support requires that clients
 * enable the element map by calling <code>setUseHashLookup(true)</code>.
 * </p>
 * <p>
 * Content providers for tree viewers must implement either the
 * {@link ITreeContentProvider} interface, (as of 3.2) the
 * {@link ILazyTreeContentProvider} interface, or (as of 3.3) the
 * {@link ILazyTreePathContentProvider}. If the content provider is an
 * <code>ILazyTreeContentProvider</code> or an
 * <code>ILazyTreePathContentProvider</code>, the underlying Tree must be
 * created using the {@link SWT#VIRTUAL} style bit, the tree viewer will not
 * support sorting or filtering, and hash lookup must be enabled by calling
 * {@link #setUseHashlookup(boolean)}.
 * </p>
 * <p>
 * Users setting up an editable tree with more than 1 column <b>have</b> to pass the
 * SWT.FULL_SELECTION style bit
 * </p>
 * @noextend This class is not intended to be subclassed by clients.
 */
public class TreeViewer extends AbstractTreeViewer {

	private static final String VIRTUAL_DISPOSE_KEY = Policy.JFACE
			+ ".DISPOSE_LISTENER"; //$NON-NLS-1$

	/**
	 * This viewer's control.
	 */
	private Tree tree;

	/**
	 * Flag for whether the tree has been disposed of.
	 */
	private boolean treeIsDisposed = false;

	private boolean contentProviderIsLazy;

	private boolean contentProviderIsTreeBased;

	/**
	 * The row object reused
	 */
	private TreeViewerRow cachedRow;

	/**
	 * true if we are inside a preservingSelection() call
	 */
	private boolean insidePreservingSelection;

	/**
	 * Creates a tree viewer on a newly-created tree control under the given
	 * parent. The tree control is created using the SWT style bits
	 * <code>MULTI, H_SCROLL, V_SCROLL,</code> and <code>BORDER</code>. The
	 * viewer has no input, no content provider, a default label provider, no
	 * sorter, and no filters.
	 *
	 * @param parent
	 *            the parent control
	 */
	public TreeViewer(Composite parent) {
		this(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
	}

	/**
	 * Creates a tree viewer on a newly-created tree control under the given
	 * parent. The tree control is created using the given SWT style bits. The
	 * viewer has no input, no content provider, a default label provider, no
	 * sorter, and no filters.
	 *
	 * @param parent
	 *            the parent control
	 * @param style
	 *            the SWT style bits used to create the tree.
	 */
	public TreeViewer(Composite parent, int style) {
		this(new Tree(parent, style));
	}

	/**
	 * Creates a tree viewer on the given tree control. The viewer has no input,
	 * no content provider, a default label provider, no sorter, and no filters.
	 *
	 * @param tree
	 *            the tree control
	 */
	public TreeViewer(Tree tree) {
		super();
		this.tree = tree;
		hookControl(tree);
	}

	@Override
	protected void addTreeListener(Control c, TreeListener listener) {
		((Tree) c).addTreeListener(listener);
	}

	@Override
	protected Widget getColumnViewerOwner(int columnIndex) {
		if (columnIndex < 0 || ( columnIndex > 0 && columnIndex >= getTree().getColumnCount() ) ) {
			return null;
		}

		if (getTree().getColumnCount() == 0)// Hang it off the table if it
			return getTree();

		return getTree().getColumn(columnIndex);
	}

	@Override
	protected Item[] getChildren(Widget o) {
		if (o instanceof TreeItem) {
			return ((TreeItem) o).getItems();
		}
		if (o instanceof Tree) {
			return ((Tree) o).getItems();
		}
		return null;
	}

	@Override
	public Control getControl() {
		return tree;
	}

	@Override
	protected boolean getExpanded(Item item) {
		return ((TreeItem) item).getExpanded();
	}

	@Override
	protected Item getItemAt(Point p) {
		TreeItem[] selection = tree.getSelection();

		if( selection.length == 1 ) {
			int columnCount = tree.getColumnCount();

			for( int i = 0; i < columnCount; i++ ) {
				if( selection[0].getBounds(i).contains(p) ) {
					return selection[0];
				}
			}
		}

		return getTree().getItem(p);
	}

	@Override
	protected int getItemCount(Control widget) {
		return ((Tree) widget).getItemCount();
	}

	@Override
	protected int getItemCount(Item item) {
		return ((TreeItem) item).getItemCount();
	}

	@Override
	protected Item[] getItems(Item item) {
		return ((TreeItem) item).getItems();
	}

	/**
	 * The tree viewer implementation of this <code>Viewer</code> framework
	 * method ensures that the given label provider is an instance of either
	 * <code>ITableLabelProvider</code> or <code>ILabelProvider</code>. If
	 * it is an <code>ITableLabelProvider</code>, then it provides a separate
	 * label text and image for each column. If it is an
	 * <code>ILabelProvider</code>, then it provides only the label text and
	 * image for the first column, and any remaining columns are blank.
	 */
	@Override
	public IBaseLabelProvider getLabelProvider() {
		return super.getLabelProvider();
	}

	@Override
	protected Item getParentItem(Item item) {
		return ((TreeItem) item).getParentItem();
	}

	@Override
	protected Item[] getSelection(Control widget) {
		return ((Tree) widget).getSelection();
	}

	/**
	 * Returns this tree viewer's tree control.
	 *
	 * @return the tree control
	 */
	public Tree getTree() {
		return tree;
	}

	@Override
	protected void hookControl(Control control) {
		super.hookControl(control);
		Tree treeControl = (Tree) control;

		if ((treeControl.getStyle() & SWT.VIRTUAL) != 0) {
			treeControl.addDisposeListener(e -> {
				treeIsDisposed = true;
				unmapAllElements();
			});
			treeControl.addListener(SWT.SetData, event -> {
				if (contentProviderIsLazy) {
					TreeItem item = (TreeItem) event.item;
					TreeItem parentItem = item.getParentItem();
					int index = event.index;
					virtualLazyUpdateWidget(
							parentItem == null ? (Widget) getTree()
									: parentItem, index);
				}
			});
		}
	}

	@Override
	protected ColumnViewerEditor createViewerEditor() {
		return new TreeViewerEditor(this,null,new ColumnViewerEditorActivationStrategy(this),ColumnViewerEditor.DEFAULT);
	}

	@Override
	protected Item newItem(Widget parent, int flags, int ix) {
		TreeItem item;

		if (parent instanceof TreeItem) {
			item = (TreeItem) createNewRowPart(getViewerRowFromItem(parent),
					flags, ix).getItem();
		} else {
			item = (TreeItem) createNewRowPart(null, flags, ix).getItem();
		}

		return item;
	}

	@Override
	protected void removeAll(Control widget) {
		((Tree) widget).removeAll();
	}

	@Override
	protected void setExpanded(Item node, boolean expand) {
		((TreeItem) node).setExpanded(expand);
		if (contentProviderIsLazy) {
			// force repaints to happen
			getControl().update();
		}
	}

	@Override
	protected void setSelection(List items) {

		Item[] current = getSelection(getTree());

		// Don't bother resetting the same selection
		if (isSameSelection(items, current)) {
			return;
		}

		TreeItem[] newItems = new TreeItem[items.size()];
		items.toArray(newItems);
		getTree().setSelection(newItems);
	}

	@Override
	protected void showItem(Item item) {
		getTree().showItem((TreeItem) item);
	}

	@Override
	protected Item getChild(Widget widget, int index) {
		if (widget instanceof TreeItem) {
			return ((TreeItem) widget).getItem(index);
		}
		if (widget instanceof Tree) {
			return ((Tree) widget).getItem(index);
		}
		return null;
	}

	@Override
	protected void assertContentProviderType(IContentProvider provider) {
		if (provider instanceof ILazyTreeContentProvider
				|| provider instanceof ILazyTreePathContentProvider) {
			return;
		}
		super.assertContentProviderType(provider);
	}

	@Override
	protected Object[] getRawChildren(Object parent) {
		if (contentProviderIsLazy) {
			return new Object[0];
		}
		return super.getRawChildren(parent);
	}

	@Override
	void preservingSelection(Runnable updateCode, boolean reveal) {
		if (insidePreservingSelection || !getPreserveSelection()){
			// avoid preserving the selection if called reentrantly,
			// see bug 172640
			updateCode.run();
			return;
		}
		insidePreservingSelection = true;
		try {
			super.preservingSelection(updateCode, reveal);
		} finally {
			insidePreservingSelection = false;
		}
	}

	/**
	 * For a TreeViewer with a tree with the VIRTUAL style bit set, set the
	 * number of children of the given element or tree path. To set the number
	 * of children of the invisible root of the tree, you can pass the input
	 * object or an empty tree path.
	 *
	 * @param elementOrTreePath
	 *            the element, or tree path
	 * @param count
	 *
	 * @since 3.2
	 */
	public void setChildCount(final Object elementOrTreePath, final int count) {
		if (checkBusy())
			return;
		preservingSelection(() -> {
			if (internalIsInputOrEmptyPath(elementOrTreePath)) {
				getTree().setItemCount(count);
				return;
			}
			Widget[] items = internalFindItems(elementOrTreePath);
			for (Widget item : items) {
				TreeItem treeItem = (TreeItem) item;
				treeItem.setItemCount(count);
			}
		});
	}

	/**
	 * For a TreeViewer with a tree with the VIRTUAL style bit set, replace the
	 * given parent's child at index with the given element. If the given parent
	 * is this viewer's input or an empty tree path, this will replace the root
	 * element at the given index.
	 * <p>
	 * This method should be called by implementers of ILazyTreeContentProvider
	 * to populate this viewer.
	 * </p>
	 *
	 * @param parentElementOrTreePath
	 *            the parent of the element that should be updated, or the tree
	 *            path to that parent
	 * @param index
	 *            the index in the parent's children
	 * @param element
	 *            the new element
	 *
	 * @see #setChildCount(Object, int)
	 * @see ILazyTreeContentProvider
	 * @see ILazyTreePathContentProvider
	 *
	 * @since 3.2
	 */
	public void replace(final Object parentElementOrTreePath, final int index,
			final Object element) {
		if (checkBusy())
			return;
		Item[] selectedItems = getSelection(getControl());
		TreeSelection selection = (TreeSelection) getSelection();
		Widget[] itemsToDisassociate;
		if (parentElementOrTreePath instanceof TreePath) {
			TreePath elementPath = ((TreePath) parentElementOrTreePath)
					.createChildPath(element);
			itemsToDisassociate = internalFindItems(elementPath);
		} else {
			itemsToDisassociate = internalFindItems(element);
		}
		if (internalIsInputOrEmptyPath(parentElementOrTreePath)) {
			if (index < tree.getItemCount()) {
				TreeItem item = tree.getItem(index);
				selection = adjustSelectionForReplace(selectedItems, selection, item, element, getRoot());
				// disassociate any different item that represents the
				// same element under the same parent (the tree)
				for (Widget widget : itemsToDisassociate) {
					if (widget instanceof TreeItem) {
						TreeItem itemToDisassociate = (TreeItem) widget;
						if (itemToDisassociate != item
								&& itemToDisassociate.getParentItem() == null) {
							int indexToDisassociate = getTree().indexOf(
									itemToDisassociate);
							disassociate(itemToDisassociate);
							getTree().clear(indexToDisassociate, true);
						}
					}
				}
				Object oldData = item.getData();
				updateItem(item, element);
				if (!TreeViewer.this.equals(oldData, element)) {
					item.clearAll(true);
				}
			}
		} else {
			Widget[] parentItems = internalFindItems(parentElementOrTreePath);
			for (Widget widget : parentItems) {
				TreeItem parentItem = (TreeItem) widget;
				if (index < parentItem.getItemCount()) {
					TreeItem item = parentItem.getItem(index);
					selection = adjustSelectionForReplace(selectedItems, selection, item, element, parentItem.getData());
					// disassociate any different item that represents the
					// same element under the same parent (the tree)
					for (Widget widgetToDisassociate : itemsToDisassociate) {
						if (widgetToDisassociate instanceof TreeItem) {
							TreeItem itemToDisassociate = (TreeItem) widgetToDisassociate;
							if (itemToDisassociate != item
									&& itemToDisassociate.getParentItem() == parentItem) {
								int indexToDisaccociate = parentItem
										.indexOf(itemToDisassociate);
								disassociate(itemToDisassociate);
								parentItem.clear(indexToDisaccociate, true);
							}
						}
					}
					Object oldData = item.getData();
					updateItem(item, element);
					if (!TreeViewer.this.equals(oldData, element)) {
						item.clearAll(true);
					}
				}
			}
		}
		// Restore the selection if we are not already in a nested preservingSelection:
		if (!insidePreservingSelection) {
			setSelectionToWidget(selection, false);
			// send out notification if old and new differ
			ISelection newSelection = getSelection();
			if (!newSelection.equals(selection)) {
				handleInvalidSelection(selection, newSelection);
			}
		}
	}

	/**
	 * Fix for bug 185673: If the currently replaced item was selected, add it
	 * to the selection that is being restored. Only do this if its getData() is
	 * currently null
	 *
	 * @param selectedItems
	 * @param selection
	 * @param item
	 * @param element
	 * @return the adjusted selection
	 */
	private TreeSelection adjustSelectionForReplace(Item[] selectedItems,
			TreeSelection selection, TreeItem item, Object element, Object parentElement) {
		if (item.getData() != null || selectedItems.length == selection.size()
				|| parentElement == null) {
			// Don't do anything - we are not seeing an instance of bug 185673
			return selection;
		}
		for (Item selectedItem : selectedItems) {
			if (item == selectedItem) {
				// The current item was selected, but its data is null.
				// The data will be replaced by the given element, so to keep
				// it selected, we have to add it to the selection.
				TreePath[] originalPaths = selection.getPaths();
				int length = originalPaths.length;
				TreePath[] paths = new TreePath[length + 1];
				System.arraycopy(originalPaths, 0, paths, 0, length);
				// set the element temporarily so that we can call getTreePathFromItem
				item.setData(element);
				paths[length] = getTreePathFromItem(item);
				item.setData(null);
				return new TreeSelection(paths, selection.getElementComparer());
			}
		}
		// The item was not selected, return the given selection
		return selection;
	}

	@Override
	public boolean isExpandable(Object element) {
		if (contentProviderIsLazy) {
			TreeItem treeItem = (TreeItem) internalExpand(element, false);
			if (treeItem == null) {
				return false;
			}
			virtualMaterializeItem(treeItem);
			return treeItem.getItemCount() > 0;
		}
		return super.isExpandable(element);
	}

	@Override
	protected Object getParentElement(Object element) {
		boolean oldBusy = isBusy();
		setBusy(true);
		try {
			if (contentProviderIsLazy && !contentProviderIsTreeBased && !(element instanceof TreePath)) {
				ILazyTreeContentProvider lazyTreeContentProvider = (ILazyTreeContentProvider) getContentProvider();
				return lazyTreeContentProvider.getParent(element);
			}
			if (contentProviderIsLazy && contentProviderIsTreeBased && !(element instanceof TreePath)) {
				ILazyTreePathContentProvider lazyTreePathContentProvider = (ILazyTreePathContentProvider) getContentProvider();
				TreePath[] parents = lazyTreePathContentProvider
				.getParents(element);
				if (parents != null && parents.length > 0) {
					return parents[0];
				}
			}
			return super.getParentElement(element);
		} finally {
			setBusy(oldBusy);
		}
	}

	@Override
	void createChildren(Widget widget, boolean materialize) {
		if (contentProviderIsLazy) {
			Object element = widget.getData();
			if (element == null && widget instanceof TreeItem) {
				// parent has not been materialized
				virtualMaterializeItem((TreeItem) widget);
				// try getting the element now that updateElement was called
				element = widget.getData();
			}
			if (element ==  null) {
				// give up because the parent is still not materialized
				return;
			}
			Item[] children = getChildren(widget);
			if (children.length == 1 && children[0].getData() == null) {
				// found a dummy node
				virtualLazyUpdateChildCount(widget, children.length);
				children = getChildren(widget);
			}
			// touch all children to make sure they are materialized
			for (int i = 0; i < children.length; i++) {
				if (children[i].getData() == null) {
					if (materialize) {
						virtualLazyUpdateWidget(widget, i);
					} else {
						((TreeItem)children[i]).clearAll(true);
					}
				}
			}
			return;
		}
		super.createChildren(widget, materialize);
	}

	@Override
	protected void internalAdd(Widget widget, Object parentElement,
			Object[] childElements) {
		if (contentProviderIsLazy) {
			if (widget instanceof TreeItem) {
				TreeItem ti = (TreeItem) widget;
				int count = ti.getItemCount() + childElements.length;
				ti.setItemCount(count);
				ti.clearAll(false);
			} else {
				Tree t = (Tree) widget;
				t.setItemCount(t.getItemCount() + childElements.length);
				t.clearAll(false);
			}
			return;
		}
		super.internalAdd(widget, parentElement, childElements);
	}

	private void virtualMaterializeItem(TreeItem treeItem) {
		if (treeItem.getData() != null) {
			// already materialized
			return;
		}
		if (!contentProviderIsLazy) {
			return;
		}
		int index;
		Widget parent = treeItem.getParentItem();
		if (parent == null) {
			parent = treeItem.getParent();
		}
		Object parentElement = parent.getData();
		if (parentElement != null) {
			if (parent instanceof Tree) {
				index = ((Tree) parent).indexOf(treeItem);
			} else {
				index = ((TreeItem) parent).indexOf(treeItem);
			}
			virtualLazyUpdateWidget(parent, index);
		}
	}

	@Override
	protected void internalRefreshStruct(Widget widget, Object element,
			boolean updateLabels) {
		if (contentProviderIsLazy) {
			// clear all starting with the given widget
			if (widget instanceof Tree) {
				((Tree) widget).clearAll(true);
			} else if (widget instanceof TreeItem) {
				((TreeItem) widget).clearAll(true);
			}
			int index = 0;
			Widget parent = null;
			if (widget instanceof TreeItem) {
				TreeItem treeItem = (TreeItem) widget;
				parent = treeItem.getParentItem();
				if (parent == null) {
					parent = treeItem.getParent();
				}
				if (parent instanceof Tree) {
					index = ((Tree) parent).indexOf(treeItem);
				} else {
					index = ((TreeItem) parent).indexOf(treeItem);
				}
			}
			virtualRefreshExpandedItems(parent, widget, element, index);
			return;
		}
		super.internalRefreshStruct(widget, element, updateLabels);
	}

	/**
	 * Traverses the visible (expanded) part of the tree and updates child
	 * counts.
	 *
	 * @param parent the parent of the widget, or <code>null</code> if the widget is the tree
	 * @param widget
	 * @param element
	 * @param index the index of the widget in the children array of its parent, or 0 if the widget is the tree
	 */
	private void virtualRefreshExpandedItems(Widget parent, Widget widget, Object element, int index) {
		if (widget instanceof Tree) {
			if (element == null) {
				((Tree) widget).setItemCount(0);
				return;
			}
			virtualLazyUpdateChildCount(widget, getChildren(widget).length);
		} else if (((TreeItem) widget).getExpanded()) {
			// prevent SetData callback
			((TreeItem)widget).setText(" "); //$NON-NLS-1$
			virtualLazyUpdateWidget(parent, index);
		} else {
			return;
		}
		Item[] items = getChildren(widget);
		for (int i = 0; i < items.length; i++) {
			Item item = items[i];
			Object data = item.getData();
			virtualRefreshExpandedItems(widget, item, data, i);
		}
	}

	/*
	 * To unmap elements correctly, we need to register a dispose listener with
	 * the item if the tree is virtual.
	 */
	@Override
	protected void mapElement(Object element, final Widget item) {
		super.mapElement(element, item);
		// make sure to unmap elements if the tree is virtual
		if ((getTree().getStyle() & SWT.VIRTUAL) != 0) {
			// only add a dispose listener if item hasn't already on assigned
			// because it is reused
			if (item.getData(VIRTUAL_DISPOSE_KEY) == null) {
				item.setData(VIRTUAL_DISPOSE_KEY, Boolean.TRUE);
				item.addDisposeListener(e -> {
					if (!treeIsDisposed) {
						Object data = item.getData();
						if (usingElementMap() && data != null) {
							unmapElement(data, item);
						}
					}
				});
			}
		}
	}

	@Override
	protected ViewerRow getViewerRowFromItem(Widget item) {
		if( cachedRow == null ) {
			cachedRow = new TreeViewerRow((TreeItem) item);
		} else {
			cachedRow.setItem((TreeItem) item);
		}

		return cachedRow;
	}

	/**
	 * Create a new ViewerRow at rowIndex
	 *
	 * @param parent
	 * @param style
	 * @param rowIndex
	 * @return ViewerRow
	 */
	private ViewerRow createNewRowPart(ViewerRow parent, int style, int rowIndex) {
		if (parent == null) {
			if (rowIndex >= 0) {
				return getViewerRowFromItem(new TreeItem(tree, style, rowIndex));
			}
			return getViewerRowFromItem(new TreeItem(tree, style));
		}

		if (rowIndex >= 0) {
			return getViewerRowFromItem(new TreeItem((TreeItem) parent.getItem(),
					SWT.NONE, rowIndex));
		}

		return getViewerRowFromItem(new TreeItem((TreeItem) parent.getItem(),
				SWT.NONE));
	}

	@Override
	protected void internalInitializeTree(Control widget) {
		if (contentProviderIsLazy) {
			if (widget instanceof Tree && widget.getData() != null) {
				virtualLazyUpdateChildCount(widget, 0);
				return;
			}
		}
		super.internalInitializeTree(tree);
	}

	@Override
	protected void updatePlus(Item item, Object element) {
		if (contentProviderIsLazy) {
			Object data = item.getData();
			int itemCount = 0;
			if (data != null) {
				// item is already materialized
				itemCount = ((TreeItem) item).getItemCount();
			}
			virtualLazyUpdateHasChildren(item, itemCount);
		} else {
			super.updatePlus(item, element);
		}
	}

	/**
	 * Removes the element at the specified index of the parent.  The selection is updated if required.
	 *
	 * @param parentOrTreePath the parent element, the input element, or a tree path to the parent element
	 * @param index child index
	 * @since 3.3
	 */
	public void remove(final Object parentOrTreePath, final int index) {
		if (checkBusy())
			return;
		final List oldSelection = new LinkedList(Arrays
				.asList(((TreeSelection) getSelection()).getPaths()));
		preservingSelection(() -> {
			TreePath removedPath = null;
			if (internalIsInputOrEmptyPath(parentOrTreePath)) {
				Tree tree = (Tree) getControl();
				if (index < tree.getItemCount()) {
					TreeItem item1 = tree.getItem(index);
					if (item1.getData() != null) {
						removedPath = getTreePathFromItem(item1);
						disassociate(item1);
					}
					item1.dispose();
				}
			} else {
				Widget[] parentItems = internalFindItems(parentOrTreePath);
				for (Widget parentWidget : parentItems) {
					TreeItem parentItem = (TreeItem) parentWidget;
					if (parentItem.isDisposed())
						continue;
					if (index < parentItem.getItemCount()) {
						TreeItem item2 = parentItem.getItem(index);

						if (item2.getData() == null) {
							// If getData()==null and index == 0, and the
							// parent item is collapsed, then we are
							// being asked to remove the dummy node. We'll
							// just ignore the request to remove the dummy
							// node (bug 292322 and bug 296573).
							if (index > 0 || parentItem.getExpanded()) {
								item2.dispose();
							}
						} else {
							removedPath = getTreePathFromItem(item2);
							disassociate(item2);
							item2.dispose();
						}
					}
				}
			}
			if (removedPath != null) {
				boolean removed = false;
				for (Iterator it = oldSelection.iterator(); it
						.hasNext();) {
					TreePath path = (TreePath) it.next();
					if (path.startsWith(removedPath, getComparer())) {
						it.remove();
						removed = true;
					}
				}
				if (removed) {
					setSelection(new TreeSelection(
							(TreePath[]) oldSelection
									.toArray(new TreePath[oldSelection
											.size()]), getComparer()),
							false);
				}

			}
		});
	}

	@Override
	protected void handleTreeExpand(TreeEvent event) {
	    // Fix for Bug 271744 because windows expanding doesn't fire a focus lost
		if( isCellEditorActive() ) {
			applyEditorValue();
		}

		if (contentProviderIsLazy) {
			if (event.item.getData() != null) {
				Item[] children = getChildren(event.item);
				if (children.length == 1 && children[0].getData()==null) {
					// we have a dummy child node, ask for an updated child
					// count
					virtualLazyUpdateChildCount(event.item, children.length);
				}
				fireTreeExpanded(new TreeExpansionEvent(this, event.item
						.getData()));
			}
			return;
		}
		super.handleTreeExpand(event);
	}

	@Override
	protected void handleTreeCollapse(TreeEvent event) {
		// Fix for Bug 271744 because windows is firing collapse before
		// focus lost event
		if( isCellEditorActive() ) {
			applyEditorValue();
		}

		super.handleTreeCollapse(event);
	}

	/**
	 * Sets the content provider used by this <code>TreeViewer</code>.
	 * <p>
	 * Content providers for tree viewers must implement either
	 * {@link ITreeContentProvider}, or {@link ITreePathContentProvider}, or
	 * {@link ILazyTreeContentProvider}, or
	 * {@link ILazyTreePathContentProvider}.
	 */
	@Override
	public void setContentProvider(IContentProvider provider) {
		contentProviderIsLazy = (provider instanceof ILazyTreeContentProvider)
				|| (provider instanceof ILazyTreePathContentProvider);
		contentProviderIsTreeBased = provider instanceof ILazyTreePathContentProvider;
		super.setContentProvider(provider);
	}

	/**
	 * For a TreeViewer with a tree with the VIRTUAL style bit set, inform the
	 * viewer about whether the given element or tree path has children. Avoid
	 * calling this method if the number of children has already been set.
	 *
	 * @param elementOrTreePath
	 *            the element, or tree path
	 * @param hasChildren
	 *
	 * @since 3.3
	 */
	public void setHasChildren(final Object elementOrTreePath, final boolean hasChildren) {
		if (checkBusy())
			return;
		preservingSelection(() -> {
			if (internalIsInputOrEmptyPath(elementOrTreePath)) {
				if (hasChildren) {
					virtualLazyUpdateChildCount(getTree(),
							getChildren(getTree()).length);
				} else {
					setChildCount(elementOrTreePath, 0);
				}
				return;
			}
			Widget[] items = internalFindItems(elementOrTreePath);
			for (Widget widget : items) {
				TreeItem item = (TreeItem) widget;
				if (!hasChildren) {
					item.setItemCount(0);
				} else {
					if (!item.getExpanded()) {
						item.setItemCount(1);
						TreeItem child = item.getItem(0);
						if (child.getData() != null) {
							disassociate(child);
						}
						item.clear(0, true);
					} else {
		                virtualLazyUpdateChildCount(item, item.getItemCount());
		            }
				}
			}
		});
	}

	/**
	 * Update the widget at index.
	 * @param widget
	 * @param index
	 */
	private void virtualLazyUpdateWidget(Widget widget, int index) {
		boolean oldBusy = isBusy();
		setBusy(false);
		try {
			if (contentProviderIsTreeBased) {
				TreePath treePath;
				if (widget instanceof Item) {
					if (widget.getData() == null) {
						// we need to materialize the parent first
						// see bug 167668
						// however, that would be too risky
						// see bug 182782 and bug 182598
						// so we just ignore this call altogether
						// and don't do this: virtualMaterializeItem((TreeItem) widget);
						return;
					}
					treePath = getTreePathFromItem((Item) widget);
				} else {
					treePath = TreePath.EMPTY;
				}
				((ILazyTreePathContentProvider) getContentProvider())
						.updateElement(treePath, index);
			} else {
				((ILazyTreeContentProvider) getContentProvider()).updateElement(
						widget.getData(), index);
			}
		} finally {
			setBusy(oldBusy);
		}
	}

	/**
	 * Update the child count
	 * @param widget
	 * @param currentChildCount
	 */
	private void virtualLazyUpdateChildCount(Widget widget, int currentChildCount) {
		boolean oldBusy = isBusy();
		setBusy(false);
		try {
			if (contentProviderIsTreeBased) {
				TreePath treePath;
				if (widget instanceof Item) {
					treePath = getTreePathFromItem((Item) widget);
				} else {
					treePath = TreePath.EMPTY;
				}
				((ILazyTreePathContentProvider) getContentProvider())
				.updateChildCount(treePath, currentChildCount);
			} else {
				((ILazyTreeContentProvider) getContentProvider()).updateChildCount(widget.getData(), currentChildCount);
			}
		} finally {
			setBusy(oldBusy);
		}
	}

	/**
	 * Update the item with the current child count.
	 * @param item
	 * @param currentChildCount
	 */
	private void virtualLazyUpdateHasChildren(Item item, int currentChildCount) {
		boolean oldBusy = isBusy();
		setBusy(false);
		try {
			if (contentProviderIsTreeBased) {
				TreePath treePath;
				treePath = getTreePathFromItem(item);
				if (currentChildCount == 0 || !((TreeItem)item).getExpanded()) {
					// item is not expanded (but may have a plus currently)
					((ILazyTreePathContentProvider) getContentProvider())
					.updateHasChildren(treePath);
				} else {
					((ILazyTreePathContentProvider) getContentProvider())
					.updateChildCount(treePath, currentChildCount);
				}
			} else {
				((ILazyTreeContentProvider) getContentProvider()).updateChildCount(item.getData(), currentChildCount);
			}
		} finally {
			setBusy(oldBusy);
		}
	}

	@Override
	protected void disassociate(Item item) {
		if (contentProviderIsLazy) {
			// avoid causing a callback:
			item.setText(" "); //$NON-NLS-1$
		}
		super.disassociate(item);
	}

	@Override
	protected int doGetColumnCount() {
		return tree.getColumnCount();
	}

	/**
	 * Sets a new selection for this viewer and optionally makes it visible.
	 * <p>
	 * <b>Currently the <code>reveal</code> parameter is not honored because
	 * {@link Tree} does not provide an API to only select an item without
	 * scrolling it into view</b>
	 * </p>
	 *
	 * @param selection
	 *            the new selection
	 * @param reveal
	 *            <code>true</code> if the selection is to be made visible,
	 *            and <code>false</code> otherwise
	 */
	@Override
	public void setSelection(ISelection selection, boolean reveal) {
		super.setSelection(selection, reveal);
	}

	@Override
	public void editElement(Object element, int column) {
		if( element instanceof TreePath ) {
			try {
				getControl().setRedraw(false);
				setSelection(new TreeSelection((TreePath) element));
				TreeItem[] items = tree.getSelection();

				if( items.length == 1 ) {
					ViewerRow row = getViewerRowFromItem(items[0]);

					if (row != null) {
						ViewerCell cell = row.getCell(column);
						if (cell != null) {
							triggerEditorActivationEvent(new ColumnViewerEditorActivationEvent(cell));
						}
					}
				}
			} finally {
				getControl().setRedraw(true);
			}
		} else {
			super.editElement(element, column);
		}
	}

}
