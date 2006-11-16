/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Tom Schindl <tom.schindl@bestsolution.at> - concept of ViewerRow
 *******************************************************************************/

package org.eclipse.jface.viewers;

import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.util.Policy;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TreeEditor;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.events.TreeListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
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
 * Content providers for tree viewers must implement either the
 * {@link ITreeContentProvider} interface, (as of 3.2) the
 * {@link ILazyTreeContentProvider} interface, or (as of 3.3) the
 * {@link ILazyTreePathContentProvider}. If the content provider is an
 * <code>ILazyTreeContentProvider</code> or an
 * <code>ILazyTreePathContentProvider</code>, the underlying Tree must be
 * created using the {@link SWT#VIRTUAL} style bit, and the tree viewer will not
 * support sorting or filtering.
 * </p>
 */
public class TreeViewer extends AbstractTreeViewer {

	private static final String VIRTUAL_DISPOSE_KEY = Policy.JFACE
			+ ".DISPOSE_LISTENER"; //$NON-NLS-1$

	/**
	 * Internal tree viewer implementation.
	 */
	private TreeEditorImpl treeViewerImpl;

	/**
	 * This viewer's control.
	 */
	private Tree tree;

	/**
	 * This viewer's tree editor.
	 */
	private TreeEditor treeEditor;

	/**
	 * Flag for whether the tree has been disposed of.
	 */
	private boolean treeIsDisposed = false;

	private boolean contentProviderIsLazy;
	private boolean contentProviderIsTreeBased;

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
		treeEditor = new TreeEditor(tree);
		initTreeViewerImpl();
	}

	/*
	 * (non-Javadoc) Method declared in AbstractTreeViewer.
	 */
	protected void addTreeListener(Control c, TreeListener listener) {
		((Tree) c).addTreeListener(listener);
	}

	/**
	 * Cancels a currently active cell editor. All changes already done in the
	 * cell editor are lost.
	 * 
	 * @since 3.1
	 */
	public void cancelEditing() {
		treeViewerImpl.cancelEditing();
	}

	/*
	 * (non-Javadoc) Method declared in AbstractTreeViewer.
	 */
	protected void doUpdateItem(final Item item, Object element) {
		if (!(item instanceof TreeItem)) {
			return;
		}
		TreeItem treeItem = (TreeItem) item;
		if (treeItem.isDisposed()) {
			unmapElement(element, treeItem);
			return;
		}

		int columnCount = getTree().getColumnCount();
		if (columnCount == 0)// If no columns are created then fake one
			columnCount = 1;

		for (int column = 0; column < columnCount; column++) {
			ViewerColumn columnViewer = getViewerColumn(column);
			columnViewer.refresh(updateCell(getViewerRowFromItem(treeItem),
					column));

			// As it is possible for user code to run the event
			// loop check here.
			if (item.isDisposed()) {
				unmapElement(element, item);
				return;
			}

		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ColumnViewer#getColumnViewerOwner(int)
	 */
	protected Widget getColumnViewerOwner(int columnIndex) {
		if (columnIndex < 0 || columnIndex > getTree().getColumnCount()) {
			return null;
		}

		if (getTree().getColumnCount() == 0)// Hang it off the table if it
			return getTree();

		return getTree().getColumn(columnIndex);
	}

	/**
	 * Override to handle tree paths.
	 * 
	 * @see org.eclipse.jface.viewers.StructuredViewer#buildLabel(org.eclipse.jface.viewers.ViewerLabel,
	 *      java.lang.Object)
	 */
	protected void buildLabel(ViewerLabel updateLabel, Object elementOrPath) {
		Object element;
		if (elementOrPath instanceof TreePath) {
			TreePath path = (TreePath) elementOrPath;
			IBaseLabelProvider provider = getLabelProvider();
			if (provider instanceof ITreePathLabelProvider) {
				ITreePathLabelProvider pprov = (ITreePathLabelProvider) provider;
				buildLabel(updateLabel, path, pprov);
				return;
			}
			element = path.getLastSegment();
		} else {
			element = elementOrPath;
		}
		super.buildLabel(updateLabel, element);
	}

	/**
	 * Starts editing the given element.
	 * 
	 * @param element
	 *            the element
	 * @param column
	 *            the column number
	 * @since 3.1
	 */
	public void editElement(Object element, int column) {
		treeViewerImpl.editElement(element, column);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ColumnViewer#getCellEditors()
	 */
	public CellEditor[] getCellEditors() {
		return treeViewerImpl.getCellEditors();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ColumnViewer#getCellModifier()
	 */
	public ICellModifier getCellModifier() {
		return treeViewerImpl.getCellModifier();
	}

	/*
	 * (non-Javadoc) Method declared in AbstractTreeViewer.
	 */
	protected Item[] getChildren(Widget o) {
		if (o instanceof TreeItem) {
			return ((TreeItem) o).getItems();
		}
		if (o instanceof Tree) {
			return ((Tree) o).getItems();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ColumnViewer#getColumnProperties()
	 */
	public Object[] getColumnProperties() {
		return treeViewerImpl.getColumnProperties();
	}

	/*
	 * (non-Javadoc) Method declared in Viewer.
	 */
	public Control getControl() {
		return tree;
	}

	/*
	 * (non-Javadoc) Method declared in AbstractTreeViewer.
	 */
	protected boolean getExpanded(Item item) {
		return ((TreeItem) item).getExpanded();
	}


	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ColumnViewer#getItemAt(org.eclipse.swt.graphics.Point)
	 */
	protected Item getItemAt(Point p) {
		return getTree().getItem(p);
	}
	
	/*
	 * (non-Javadoc) Method declared in AbstractTreeViewer.
	 */
	protected int getItemCount(Control widget) {
		return ((Tree) widget).getItemCount();
	}

	/*
	 * (non-Javadoc) Method declared in AbstractTreeViewer.
	 */
	protected int getItemCount(Item item) {
		return ((TreeItem) item).getItemCount();
	}

	/*
	 * (non-Javadoc) Method declared in AbstractTreeViewer.
	 */
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
	public IBaseLabelProvider getLabelProvider() {
		return super.getLabelProvider();
	}

	/*
	 * (non-Javadoc) Method declared in AbstractTreeViewer.
	 */
	protected Item getParentItem(Item item) {
		return ((TreeItem) item).getParentItem();
	}

	/*
	 * (non-Javadoc) Method declared in AbstractTreeViewer.
	 */
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.AbstractTreeViewer#hookControl(org.eclipse.swt.widgets.Control)
	 */
	protected void hookControl(Control control) {
		super.hookControl(control);
		Tree treeControl = (Tree) control;
		treeControl.addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
				treeViewerImpl.handleMouseDown(e);
			}
		});
		if ((treeControl.getStyle() & SWT.VIRTUAL) != 0) {
			treeControl.addDisposeListener(new DisposeListener() {
				public void widgetDisposed(DisposeEvent e) {
					treeIsDisposed = true;
					unmapAllElements();
				}
			});
			treeControl.addListener(SWT.SetData, new Listener() {

				public void handleEvent(Event event) {
					if (contentProviderIsLazy) {
						TreeItem item = (TreeItem) event.item;
						TreeItem parentItem = item.getParentItem();
						int index;
						if (parentItem != null) {
							index = parentItem.indexOf(item);
						} else {
							index = getTree().indexOf(item);
						}
						virtualLazyUpdateWidget(parentItem == null ? (Widget)getTree() : parentItem, index);
					}
				}

			});
		}
	}

	/**
	 * Initializes the tree viewer implementation.
	 */
	private void initTreeViewerImpl() {
		treeViewerImpl = new TreeEditorImpl(this) {
			Rectangle getBounds(Item item, int columnNumber) {
				return ((TreeItem) item).getBounds(columnNumber);
			}

			int getColumnCount() {
				return getTree().getColumnCount();
			}

			Item[] getSelection() {
				return getTree().getSelection();
			}

			void setEditor(Control w, Item item, int columnNumber) {
				treeEditor.setEditor(w, (TreeItem) item, columnNumber);
			}

			void setSelection(IStructuredSelection selection, boolean b) {
				TreeViewer.this.setSelection(selection, b);
			}

			void showSelection() {
				getTree().showSelection();
			}

			void setLayoutData(CellEditor.LayoutData layoutData) {
				treeEditor.grabHorizontal = layoutData.grabHorizontal;
				treeEditor.horizontalAlignment = layoutData.horizontalAlignment;
				treeEditor.minimumWidth = layoutData.minimumWidth;
			}

			void handleDoubleClickEvent() {
				Viewer viewer = getViewer();
				fireDoubleClick(new DoubleClickEvent(viewer, viewer
						.getSelection()));
				fireOpen(new OpenEvent(viewer, viewer.getSelection()));
			}
		};
	}

	/**
	 * Returns whether there is an active cell editor.
	 * 
	 * @return <code>true</code> if there is an active cell editor, and
	 *         <code>false</code> otherwise
	 * @since 3.1
	 */
	public boolean isCellEditorActive() {
		return treeViewerImpl.isCellEditorActive();
	}

	/*
	 * (non-Javadoc) Method declared in AbstractTreeViewer.
	 */
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

	/*
	 * (non-Javadoc) Method declared in AbstractTreeViewer.
	 */
	protected void removeAll(Control widget) {
		((Tree) widget).removeAll();
	}

	/**
	 * Sets the cell editors of this tree viewer.
	 * 
	 * @param editors
	 *            the list of cell editors
	 * @since 3.1
	 */
	public void setCellEditors(CellEditor[] editors) {
		treeViewerImpl.setCellEditors(editors);
	}

	/**
	 * Sets the cell modifier of this tree viewer.
	 * 
	 * @param modifier
	 *            the cell modifier
	 * @since 3.1
	 */
	public void setCellModifier(ICellModifier modifier) {
		treeViewerImpl.setCellModifier(modifier);
	}

	/**
	 * Sets the column properties of this tree viewer. The properties must
	 * correspond with the columns of the tree control. They are used to
	 * identify the column in a cell modifier.
	 * 
	 * @param columnProperties
	 *            the list of column properties
	 * @since 3.1
	 */
	public void setColumnProperties(String[] columnProperties) {
		treeViewerImpl.setColumnProperties(columnProperties);
	}

	/*
	 * (non-Javadoc) Method declared in AbstractTreeViewer.
	 */
	protected void setExpanded(Item node, boolean expand) {
		((TreeItem) node).setExpanded(expand);
		if (contentProviderIsLazy) {
			// force repaints to happen
			getControl().update();
		}
	}

	/**
	 * The tree viewer implementation of this <code>Viewer</code> framework
	 * method ensures that the given label provider is an instance of either
	 * <code>ITableLabelProvider</code> or <code>ILabelProvider</code>.
	 * <p>
	 * If the label provider is an {@link ITableLabelProvider}, then it
	 * provides a separate label text and image for each column. Implementers of
	 * <code>ITableLabelProvider</code> may also implement
	 * {@link ITableColorProvider} and/or {@link ITableFontProvider} to provide
	 * colors and/or fonts. Note that the underlying {@link Tree} must be
	 * configured with {@link TreeColumn} objects in this case.
	 * </p>
	 * <p>
	 * If the label provider is an <code>ILabelProvider</code>, then it
	 * provides only the label text and image for the first column, and any
	 * remaining columns are blank. Implementers of <code>ILabelProvider</code>
	 * may also implement {@link IColorProvider} and/or {@link IFontProvider} to
	 * provide colors and/or fonts.
	 * </p>
	 */
	public void setLabelProvider(IBaseLabelProvider labelProvider) {
		Assert.isTrue(labelProvider instanceof ITableLabelProvider
				|| labelProvider instanceof ILabelProvider
				|| labelProvider instanceof CellLabelProvider);
		clearColumnParts();// Clear before refresh
		super.setLabelProvider(labelProvider);

	}

	/**
	 * Clear the viewer parts for the columns
	 */
	private void clearColumnParts() {
		TreeColumn[] columns = getTree().getColumns();
		if (columns.length == 0)
			getTree().setData(ViewerColumn.COLUMN_VIEWER_KEY, null);
		else {
			for (int i = 0; i < columns.length; i++) {
				columns[i].setData(ViewerColumn.COLUMN_VIEWER_KEY, null);

			}
		}

	}

	/*
	 * (non-Javadoc) Method declared in AbstractTreeViewer.
	 */
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

	/**
	 * Returns <code>true</code> if the given list and array of items refer to
	 * the same model elements. Order is unimportant.
	 * 
	 * @param items
	 *            the list of items
	 * @param current
	 *            the array of items
	 * @return <code>true</code> if the refer to the same elements,
	 *         <code>false</code> otherwise
	 * 
	 * @since 3.1
	 */
	protected boolean isSameSelection(List items, Item[] current) {
		// If they are not the same size then they are not equivalent
		int n = items.size();
		if (n != current.length) {
			return false;
		}

		CustomHashtable itemSet = newHashtable(n * 2 + 1);
		for (Iterator i = items.iterator(); i.hasNext();) {
			Item item = (Item) i.next();
			Object element = item.getData();
			itemSet.put(element, element);
		}

		// Go through the items of the current collection
		// If there is a mismatch return false
		for (int i = 0; i < current.length; i++) {
			if (current[i].getData() == null
					|| !itemSet.containsKey(current[i].getData())) {
				return false;
			}
		}

		return true;
	}

	/*
	 * (non-Javadoc) Method declared in AbstractTreeViewer.
	 */
	protected void showItem(Item item) {
		getTree().showItem((TreeItem) item);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.AbstractTreeViewer#getChild(org.eclipse.swt.widgets.Widget,
	 *      int)
	 */
	protected Item getChild(Widget widget, int index) {
		if (widget instanceof TreeItem) {
			return ((TreeItem) widget).getItem(index);
		}
		if (widget instanceof Tree) {
			return ((Tree) widget).getItem(index);
		}
		return null;
	}

	protected void assertContentProviderType(IContentProvider provider) {
		if (provider instanceof ILazyTreeContentProvider
				|| provider instanceof ILazyTreePathContentProvider) {
			return;
		}
		super.assertContentProviderType(provider);
	}

	protected Object[] getRawChildren(Object parent) {
		if (contentProviderIsLazy) {
			return new Object[0];
		}
		return super.getRawChildren(parent);
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
		preservingSelection(new Runnable() {
			public void run() {
				if (internalIsInputOrEmptyPath(elementOrTreePath)) {
					getTree().setItemCount(count);
					return;
				}
				Widget[] items = internalFindItems(elementOrTreePath);
				for (int i = 0; i < items.length; i++) {
					TreeItem treeItem = (TreeItem) items[i];
					treeItem.setItemCount(count);
				}
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
		preservingSelection(new Runnable() {
			public void run() {
				if (internalIsInputOrEmptyPath(parentElementOrTreePath)) {
					if (index < tree.getItemCount()) {
						updateItem(tree.getItem(index), element);
					}
				} else {
					Widget[] parentItems = internalFindItems(parentElementOrTreePath);
					for (int i = 0; i < parentItems.length; i++) {
						TreeItem parentItem = (TreeItem) parentItems[i];
						if (index < parentItem.getItemCount()) {
							updateItem(parentItem.getItem(index), element);
						}
					}
				}
			}

		});
	}

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

	protected Object getParentElement(Object element) {
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
	}

	protected void createChildren(Widget widget) {
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
					virtualLazyUpdateWidget(widget, i);
				}
			}
			return;
		}
		super.createChildren(widget);
	}

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.AbstractTreeViewer#internalRefreshStruct(org.eclipse.swt.widgets.Widget,
	 *      java.lang.Object, boolean)
	 */
	protected void internalRefreshStruct(Widget widget, Object element,
			boolean updateLabels) {
		if (contentProviderIsLazy) {
			// first phase: update child counts
			virtualRefreshChildCounts(widget, element);
			// second phase: update labels
			if (updateLabels) {
				if (widget instanceof Tree) {
					((Tree) widget).clearAll(true);
				} else if (widget instanceof TreeItem) {
					((TreeItem) widget).clearAll(true);
				}
			}
			return;
		}
		super.internalRefreshStruct(widget, element, updateLabels);
	}

	/**
	 * Traverses the visible (expanded) part of the tree and updates child
	 * counts.
	 * 
	 * @param widget
	 * @param element
	 */
	private void virtualRefreshChildCounts(Widget widget, Object element) {
		if (widget instanceof Tree || ((TreeItem) widget).getExpanded()) {
			// widget shows children - it is safe to call getChildren
			if (element != null) {
				virtualLazyUpdateChildCount(widget, getChildren(widget).length);
			} else {
				if (widget instanceof Tree) {
					((Tree) widget).setItemCount(0);
				} else {
					((TreeItem) widget).setItemCount(0);
				}
			}
			// need to get children again because they might have been updated
			// through a callback to setChildCount.
			Item[] items = getChildren(widget);
			for (int i = 0; i < items.length; i++) {
				Item item = items[i];
				Object data = item.getData();
				if (data != null) {
					virtualRefreshChildCounts(item, data);
				}
			}
		}
	}

	/*
	 * To unmap elements correctly, we need to register a dispose listener with
	 * the item if the tree is virtual.
	 */
	protected void mapElement(Object element, final Widget item) {
		super.mapElement(element, item);
		// make sure to unmap elements if the tree is virtual
		if ((getTree().getStyle() & SWT.VIRTUAL) != 0) {
			// only add a dispose listener if item hasn't already on assigned
			// because it is reused
			if (item.getData(VIRTUAL_DISPOSE_KEY) == null) {
				item.setData(VIRTUAL_DISPOSE_KEY, Boolean.TRUE);
				item.addDisposeListener(new DisposeListener() {
					public void widgetDisposed(DisposeEvent e) {
						if (!treeIsDisposed) {
							Object data = item.getData();
							if (usingElementMap() && data != null) {
								unmapElement(data, item);
							}
						}
					}
				});
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ColumnViewer#getRowPartFromItem(org.eclipse.swt.widgets.Widget)
	 */
	protected ViewerRow getViewerRowFromItem(Widget item) {
		ViewerRow part = (ViewerRow) item.getData(ViewerRow.ROWPART_KEY);

		if (part == null) {
			part = new TreeViewerRow(((TreeItem) item));
		}

		return part;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.AbstractTreeViewer#internalInitializeTree(org.eclipse.swt.widgets.Control)
	 */
	protected void internalInitializeTree(Control widget) {
		if (contentProviderIsLazy) {
			if (widget instanceof Tree && widget.getData() != null) {
				virtualLazyUpdateChildCount(widget, 0);
				return;
			}
		}
		super.internalInitializeTree(tree);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.AbstractTreeViewer#updatePlus(org.eclipse.swt.widgets.Item,
	 *      java.lang.Object)
	 */
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
		preservingSelection(new Runnable() {
			public void run() {
				if (internalIsInputOrEmptyPath(parentOrTreePath)) {
					Tree tree = (Tree) getControl();
					if (index < tree.getItemCount()) {
						TreeItem item = tree.getItem(index);
						if (item.getData() != null) {
							disassociate(item);
						}
						item.dispose();
					}
				} else {
					Widget[] parentItems = internalFindItems(parentOrTreePath);
					for (int i = 0; i < parentItems.length; i++) {
						TreeItem parentItem = (TreeItem) parentItems[i];
						if (index < parentItem.getItemCount()) {
							TreeItem item = parentItem.getItem(index);
							if (item.getData() != null) {
								disassociate(item);
							}
							item.dispose();
						}
					}
				}
			}
		});
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.AbstractTreeViewer#handleTreeExpand(org.eclipse.swt.events.TreeEvent)
	 */
	protected void handleTreeExpand(TreeEvent event) {
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
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.AbstractTreeViewer#setContentProvider(org.eclipse.jface.viewers.IContentProvider)
	 */
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
	public void setHasChildren(Object elementOrTreePath, boolean hasChildren) {
		if (internalIsInputOrEmptyPath(elementOrTreePath)) {
			if (hasChildren) {
				virtualLazyUpdateChildCount(getTree(), getChildren(getTree()).length);
			} else {
				setChildCount(elementOrTreePath, 0);
			}
			return;
		}
		Widget[] items = internalFindItems(elementOrTreePath);
		for (int i = 0; i < items.length; i++) {
				TreeItem item = (TreeItem) items[i];
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
					}
				}
		}
	}

	/**
	 * Update the widget at index.
	 * @param widget
	 * @param index
	 */
	private void virtualLazyUpdateWidget(Widget widget, int index) {
		if (contentProviderIsTreeBased) {
			TreePath treePath;
			if (widget instanceof Item) {
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
	}

	/**
	 * Update the child count
	 * @param widget
	 * @param currentChildCount
	 */
	private void virtualLazyUpdateChildCount(Widget widget, int currentChildCount) {
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
	}
	
	/**
	 * Update the item with the current child count.
	 * @param item
	 * @param currentChildCount
	 */
	private void virtualLazyUpdateHasChildren(Item item, int currentChildCount) {
		if (contentProviderIsTreeBased) {
			TreePath treePath;
			treePath = getTreePathFromItem(item);
			if (currentChildCount == 0) {
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
	}

	private boolean internalIsInputOrEmptyPath(final Object elementOrTreePath) {
		if (elementOrTreePath.equals(getInput()))
			return true;
		if (!(elementOrTreePath instanceof TreePath))
			return false;
		return ((TreePath) elementOrTreePath).getSegmentCount() == 0;
	}
	
	protected void disassociate(Item item) {
		if (contentProviderIsLazy) {
			// avoid causing a callback:
			item.setText(" "); //$NON-NLS-1$
		}
		super.disassociate(item);
	}

}
