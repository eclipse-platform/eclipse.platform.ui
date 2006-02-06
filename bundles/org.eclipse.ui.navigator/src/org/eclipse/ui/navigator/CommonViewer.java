/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.navigator;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.navigator.internal.DelegateTreeViewerSorter;
import org.eclipse.ui.navigator.internal.NavigatorContentService;
import org.eclipse.ui.navigator.internal.dnd.CommonNavigatorDragAdapter;
import org.eclipse.ui.navigator.internal.dnd.CommonNavigatorDropAdapter;
import org.eclipse.ui.part.PluginTransfer;

/**
 * 
 * Provides the Tree Viewer for the Common Navigator. Content and labels are
 * provided by an instance of {@link INavigatorContentService}&nbsp; which uses
 * the ID supplied in the constructor
 * {@link CommonViewer#CommonViewer(String, Composite, int)} or through
 * {@link NavigatorContentServiceFactory#createContentService(String, org.eclipse.jface.viewers.StructuredViewer)}.
 * 
 * <p>
 * Clients may extend this class.
 * </p>
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * 
 * @since 3.2
 */
public class CommonViewer extends TreeViewer {

	private final NavigatorContentService contentService;

	/**
	 * <p>
	 * Initializes the content provider, label provider, and drag and drop
	 * support. Should not be called by clients -- this method is invoked when
	 * the constructor is invoked.
	 * </p>
	 */
	protected void init() {
		setUseHashlookup(true);
		setContentProvider(contentService.createCommonContentProvider());
		DecoratingLabelProvider decoratingProvider = new DecoratingLabelProvider(
				contentService.createCommonLabelProvider(), PlatformUI
						.getWorkbench().getDecoratorManager()
						.getLabelDecorator());
		setLabelProvider(decoratingProvider);
		initDragAndDrop();

	}

	protected void removeWithoutRefresh(Object[] elements) {
		super.remove(elements);
	}

	/**
	 * Returns the sorted and filtered set of children of the given element. The
	 * resulting array must not be modified, as it may come directly from the
	 * model's internal state.
	 * 
	 * @param parent
	 *            the parent element
	 * @return a sorted and filtered array of child elements
	 */
	protected Object[] getSortedChildren(Object parent) {
		Object[] result = getFilteredChildren(parent);
		ViewerSorter sorter = getSorter();
		if (sorter != null) {
			// be sure we're not modifying the original array from the model
			result = (Object[]) result.clone();
			// safe caste due to setSorter();
			((TreeViewerSorter) sorter).sort(this, parent, result);

		}
		return result;
	}

	/**
	 * Adds the given child elements to this viewer as children of the given
	 * parent element.
	 * <p>
	 * EXPERIMENTAL. Not to be used except by JDT. This method was added to
	 * support JDT's explorations into grouping by working sets, which requires
	 * viewers to support multiple equal elements. See bug 76482 for more
	 * details. This support will likely be removed in Eclipse 3.2 in favour of
	 * proper support for multiple equal elements.
	 * </p>
	 * 
	 * @param widget
	 *            the widget for the parent element
	 * @param parentElement
	 *            the parent element
	 * @param childElements
	 *            the child elements to add
	 * @since 3.1
	 */
	protected void internalAdd(Widget widget, Object parentElement,
			Object[] childElements) {

		// optimization!
		// if the widget is not expanded we just invalidate the subtree
		if (widget instanceof Item) {
			Item ti = (Item) widget;
			if (!getExpanded(ti)) {
				boolean needDummy = isExpandable(parentElement);
				boolean haveDummy = false;
				// remove all children
				Item[] items = getItems(ti);
				for (int i = 0; i < items.length; i++) {
					if (items[i].getData() != null) {
						disassociate(items[i]);
						items[i].dispose();
					} else {
						if (needDummy && !haveDummy) {
							haveDummy = true;
						} else {
							items[i].dispose();
						}
					}
				}
				// append a dummy if necessary
				if (needDummy && !haveDummy)
					newItem(ti, SWT.NULL, -1);
				return;
			}
		}

		if (childElements.length > 0) {
			Object[] filtered = filter(childElements);
			if (getSorter() != null)
				((TreeViewerSorter) getSorter()).sort(this, parentElement,
						filtered);
			createAddedElements(widget, parentElement, filtered);
		}
	}

	/**
	 * See if element is the data of one of the elements in items.
	 * 
	 * @param items
	 * @param element
	 * @return <code>true</code> if the element matches.
	 */
	private boolean itemExists(Item[] items, Object element) {
		if (usingElementMap())// if we can do a constant time lookup find it
			return findItem(element) != null;
		for (int i = 0; i < items.length; i++) {
			if (items[i].getData().equals(element))
				return true;
		}
		return false;
	}

	/**
	 * Create the new elements in the parent widget. If the child already exists
	 * do nothing.
	 * 
	 * @param widget
	 * @param elements
	 *            Sorted list of elements to add.
	 */
	private void createAddedElements(Widget widget, Object parent,
			Object[] elements) {

		if (elements.length == 1) {
			if (equals(elements[0], widget.getData()))
				return;
		}

		// safe caste due to setSorter();
		TreeViewerSorter sorter = (TreeViewerSorter) getSorter();
		Item[] items = getChildren(widget);

		// As the items are sorted already we optimize for a
		// start position
		int lastInsertion = 0;

		// Optimize for the empty case
		if (items.length == 0) {
			for (int i = 0; i < elements.length; i++) {
				createTreeItem(widget, elements[i], -1);
			}
			return;
		}

		for (int i = 0; i < elements.length; i++) {
			boolean newItem = true;
			Object element = elements[i];
			int index;
			if (getSorter() == null) {
				if (itemExists(items, element)) {
					refresh(element);
					newItem = false;
				}
				index = -1;
			} else {
				lastInsertion = insertionPosition(items, lastInsertion, sorter,
						parent, element);
				// As we are only searching the original array we keep track of
				// those positions only
				if (lastInsertion == items.length)
					index = -1;
				else {// See if we should just refresh
					while (lastInsertion < items.length
							&& sorter.compare(this, parent, element,
									items[lastInsertion].getData()) == 0) {
						// As we cannot assume the sorter is consistent with
						// equals() - therefore we can
						// just check against the item prior to this index (if
						// any)
						if (items[lastInsertion].getData().equals(element)) {
							// refresh the element in case it has new children
							refresh(element);
							newItem = false;
						}
						lastInsertion++;// We had an insertion so increment
					}
					// Did we get to the end?
					if (lastInsertion == items.length)
						index = -1;
					else
						index = lastInsertion + i; // Add the index as the
					// array is growing
				}
			}
			if (newItem)
				createTreeItem(widget, element, index);
		}
	}

	/**
	 * Returns the index where the item should be inserted. It uses sorter to
	 * determine the correct position, if sorter is not assigned, returns the
	 * index of the element after the last.
	 * 
	 * @param items
	 *            the items to search
	 * @param lastInsertion
	 *            the start index to start search for position from this allows
	 *            optimising search for multiple elements that are sorted
	 *            themself.
	 * @param element
	 *            element to find position for.
	 * @return the index to use when inserting the element.
	 * 
	 */

	private int insertionPosition(Item[] items, int lastInsertion,
			TreeViewerSorter sorter, Object parent, Object element) {

		int size = items.length;
		if (getSorter() == null)
			return size;
		int min = lastInsertion, max = size - 1;

		while (min <= max) {
			int mid = (min + max) / 2;
			Object data = items[mid].getData();
			int compare = sorter.compare(this, parent, data, element);
			if (compare == 0) {
				return mid;// Return if we already match
			}
			if (compare < 0)
				min = mid + 1;
			else
				max = mid - 1;
		}
		return min;

	}

	// End of pulled down code

	/**
	 * <p>
	 * Adds DND support to the Navigator. Uses hooks into the extensible
	 * framework for DND.
	 * </p>
	 * <p>
	 * By default, the following Transfer types are supported:
	 * <ul>
	 * <li>LocalSelectionTransfer.getInstance(),
	 * <li>PluginTransfer.getInstance(),
	 * <li>FileTransfer.getInstance(), and
	 * <li>ResourceTransfer.getInstance()
	 * </ul>
	 * </p>
	 * 
	 * @see CommonNavigatorDragAdapter
	 * @see CommonNavigatorDropAdapter
	 */
	protected void initDragAndDrop() {
		/* Handle Drag and Drop */
		int operations = DND.DROP_COPY | DND.DROP_MOVE;
		Transfer[] transfers = new Transfer[] {
		// TODO Removed LocalSelectTransfer and ResourceTransfer to break
				// dependency on ide and resources plugins.
				// LocalSelectionTransfer.getInstance(),
				PluginTransfer.getInstance(), FileTransfer.getInstance(),
		// ResourceTransfer.getInstance()
		};
		addDragSupport(operations, transfers, new CommonNavigatorDragAdapter(
				this));
		addDropSupport(operations, transfers, new CommonNavigatorDropAdapter(
				this));

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.AbstractTreeViewer#createTreeItem(org.eclipse.swt.widgets.Widget,
	 *      java.lang.Object, int)
	 */
	protected void createTreeItem(Widget parent, final Object element, int index) {
		try {
			super.createTreeItem(parent, element, index);
		} catch (Exception ex) {
			ex.printStackTrace();
		} catch (Error e) {
			e.printStackTrace();
		}

	}

	/*
	 * @see ContentViewer#handleLabelProviderChanged(LabelProviderChangedEvent)
	 */
	protected void handleLabelProviderChanged(LabelProviderChangedEvent event) {

		Object[] changed = event.getElements();
		if (changed != null) {
			ArrayList others = new ArrayList();
			for (int i = 0; i < changed.length; i++) {
				Object curr = changed[i];
				// TODO Resource Mapper removed. Perhaps this would be a good
				// chance to use ResourceMapping?
				// if (curr instanceof IResource) {
				// fResourceToItemsMapper.resourceChanged((IResource) curr);
				// } else {
				others.add(curr);
				// }
			}
			if (others.isEmpty()) {
				return;
			}
			event = new LabelProviderChangedEvent((IBaseLabelProvider) event
					.getSource(), others.toArray());
		}
		super.handleLabelProviderChanged(event);
	}

	protected void handleDispose(DisposeEvent event) {
		super.handleDispose(event);
		dispose();
	}

	/* (non-Javadoc) Method declared on StructuredViewer. */
	protected void setSelectionToWidget(List v, boolean reveal) {
		if (v == null) {
			setSelection(new ArrayList(0));
			return;
		}
		int size = v.size();
		List newSelection = new ArrayList(size);
		for (int i = 0; i < size; ++i) {
			// Use internalExpand since item may not yet be created. See
			// 1G6B1AR.
			Widget w = internalExpand(v.get(i), reveal);
			if (w instanceof Item) {
				newSelection.add(w);
			}
		}
		setSelection(newSelection);

		// // Although setting the selection in the control should reveal it,
		// // setSelection may be a no-op if the selection is unchanged,
		// // so explicitly reveal the first item in the selection here.
		// // See bug 100565 for more details.
		// if (reveal && newSelection.size() > 0) {
		// showItem((Item) newSelection.get(0));
		// }
	}

	/**
	 * <p>
	 * Constructs the Tree Viewer for the Common Navigator and the corresponding
	 * NavigatorContentService. The NavigatorContentService will provide the
	 * Content Provider and Label Provider -- these need not be supplied by
	 * clients.
	 * <p>
	 * For the valid bits to supply in the style mask (aStyle), see
	 * documentation provided by {@link TreeViewer}.
	 * </p>
	 * 
	 * @param aViewerId
	 *            An id tied to the extensions that is used to focus specific
	 *            content to a particular instance of the Common Navigator
	 * @param aParent
	 *            A Composite parent to contain the actual SWT widget
	 * @param aStyle
	 *            A style mask that will be used to create the TreeViewer
	 *            Composite.
	 */
	public CommonViewer(String aViewerId, Composite aParent, int aStyle) {
		super(aParent, aStyle);
		contentService = new NavigatorContentService(aViewerId, this);
		init();
	}

	public boolean isExpandable(Object element) {

		return getFilteredChildren(element).length != 0;
	}

	/**
	 * <p>
	 * Disposes of the NavigatorContentService, which will dispose the Content
	 * and Label providers.
	 * </p>
	 */
	public void dispose() {
	
		if (contentService != null)
			contentService.dispose();
	
	}

	/**
	 * Sets this viewer's sorter and triggers refiltering and resorting of this
	 * viewer's element. Passing <code>null</code> turns sorting off.
	 * 
	 * @param sorter
	 *            a viewer sorter, or <code>null</code> if none
	 */
	public void setSorter(ViewerSorter sorter) {
		if (sorter != null && sorter instanceof CommonViewerSorter)
			((CommonViewerSorter) sorter).setContentService(contentService);
	
		if (sorter == null || sorter instanceof TreeViewerSorter)
			super.setSorter(sorter);
		else /* we wrap the sorter for convenience (now we can always cast to TreeViewerSorter) */
			super.setSorter(new DelegateTreeViewerSorter(sorter));
	}

	/**
	 * <p>
	 * The {@link INavigatorContentService}provides the hook into the framework
	 * to provide content from the various extensions.
	 * </p>
	 * 
	 * @return The {@link INavigatorContentService}that was created when the
	 *         viewer was created.
	 */
	public INavigatorContentService getNavigatorContentService() {
		return contentService;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.AbstractTreeViewer#add(java.lang.Object, java.lang.Object[])
	 */
	public void add(Object parentElement, Object[] childElements) {
		// TODO Auto-generated method stub
		super.add(parentElement, childElements);
	}

	/**
	 * <p>
	 * Removals are handled by refreshing the parents of each of the given
	 * elements. The parents are determined via calls ot the contentProvider.
	 * </p>
	 * 
	 * @see org.eclipse.jface.viewers.AbstractTreeViewer#remove(java.lang.Object[])
	 */
	public void remove(Object[] elements) {
		super.remove(elements);
		ITreeContentProvider contentProvider = (ITreeContentProvider) getContentProvider();
		for (int i = 0; i < elements.length; i++) {
			super.internalRefresh(contentProvider.getParent(elements[i]));
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.StructuredViewer#refresh(boolean)
	 */
	public void refresh(boolean updateLabels) {
		// TODO Auto-generated method stub
		super.refresh(updateLabels);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.StructuredViewer#refresh(java.lang.Object, boolean)
	 */
	public void refresh(Object element, boolean updateLabels) {
		// TODO Auto-generated method stub
		super.refresh(element, updateLabels);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.StructuredViewer#refresh(java.lang.Object)
	 */
	public void refresh(Object element) {
		// TODO Auto-generated method stub
		super.refresh(element);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.StructuredViewer#update(java.lang.Object, java.lang.String[])
	 */
	public void update(Object element, String[] properties) {
		// TODO Auto-generated method stub
		super.update(element, properties);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return contentService.getViewerId();
	}
	
}
