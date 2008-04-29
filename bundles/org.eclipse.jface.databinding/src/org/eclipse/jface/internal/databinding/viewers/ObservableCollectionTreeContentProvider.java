/*******************************************************************************
 * Copyright (c) 2008 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 207858)
 *     Matthew Hall - bug 226765
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.viewers;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.IObservableCollection;
import org.eclipse.core.databinding.observable.IObservablesListener;
import org.eclipse.core.databinding.observable.Observables;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.masterdetail.IObservableFactory;
import org.eclipse.core.databinding.observable.masterdetail.MasterDetailObservables;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.viewers.TreeStructureAdvisor;
import org.eclipse.jface.util.Util;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.IElementComparer;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Display;

/**
 * NON-API - Abstract base class for {@link ITreeContentProvider}s which use an
 * {@link IObservableFactory observable collection factory} to provide the
 * elements of a tree. Each observable collection obtained from the factory is
 * observed such that changes in the collection are reflected in the viewer.
 * 
 * @since 1.2
 */
public abstract class ObservableCollectionTreeContentProvider implements
		ITreeContentProvider {
	private Realm realm;

	private IObservableValue viewerObservable;

	/**
	 * Interfaces for sending updates to the viewer.
	 */
	protected TreeViewerUpdater viewerUpdater;

	/**
	 * Element comparer used by the viewer (may be null).
	 */
	protected IElementComparer comparer;

	private IObservableSet knownElements;
	private IObservableSet unmodifiableKnownElements;

	private IObservableFactory /* <IObservableCollection> */collectionFactory;

	private Map /* <Object element, TreeNode node> */elementNodes;

	private TreeStructureAdvisor structureAdvisor;

	/**
	 * Constructs an ObservableCollectionTreeContentProvider using the given
	 * parent provider and collection factory.
	 * 
	 * @param collectionFactory
	 *            observable factory that produces an IObservableList of
	 *            children for a given parent element.
	 * @param structureAdvisor
	 */
	protected ObservableCollectionTreeContentProvider(
			IObservableFactory collectionFactory,
			TreeStructureAdvisor structureAdvisor) {
		this.structureAdvisor = structureAdvisor;
		realm = SWTObservables.getRealm(Display.getDefault());
		viewerObservable = new WritableValue(realm);
		viewerUpdater = null;

		// Known elements is a detail set of viewerObservable, so that when we
		// get the viewer instance we can swap in a set that uses its
		// IElementComparer, if any.
		IObservableFactory knownElementsFactory = new IObservableFactory() {
			public IObservable createObservable(Object target) {
				return ObservableViewerElementSet.withComparer(realm, null,
						getElementComparer((Viewer) target));
			}
		};
		knownElements = MasterDetailObservables.detailSet(viewerObservable,
				knownElementsFactory, null);
		unmodifiableKnownElements = Observables
				.unmodifiableObservableSet(knownElements);

		Assert
				.isNotNull(collectionFactory,
						"Collection factory cannot be null"); //$NON-NLS-1$
		this.collectionFactory = collectionFactory;
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if (elementNodes != null && !elementNodes.isEmpty()) {
			// Ensure we flush any observable collection listeners
			TreeNode[] oldNodes = new TreeNode[elementNodes.size()];
			elementNodes.values().toArray(oldNodes);
			for (int i = 0; i < oldNodes.length; i++)
				oldNodes[i].dispose();
			elementNodes.clear();
			elementNodes = null;
		}

		setViewer(viewer);
	}

	private void setViewer(Viewer viewer) {
		viewerUpdater = createViewerUpdater(viewer);
		comparer = getElementComparer(viewer);
		elementNodes = ViewerElementMap.withComparer(comparer);
		viewerObservable.setValue(viewer); // (clears knownElements)
	}

	private static IElementComparer getElementComparer(Viewer viewer) {
		if (viewer instanceof StructuredViewer)
			return ((StructuredViewer) viewer).getComparer();
		return null;
	}

	private static TreeViewerUpdater createViewerUpdater(Viewer viewer) {
		if (viewer instanceof AbstractTreeViewer)
			return new TreeViewerUpdater((AbstractTreeViewer) viewer);
		throw new IllegalArgumentException(
				"This content provider only works with AbstractTreeViewer"); //$NON-NLS-1$
	}

	public Object getParent(Object element) {
		if (structureAdvisor != null) {
			Object parentFromAdvisor = structureAdvisor.getParent(element);
			if (parentFromAdvisor != null) {
				return parentFromAdvisor;
			}
		}
		TreeNode node = getExistingNode(element);
		if (node != null)
			return node.getParent();
		return null;
	}

	public Object[] getElements(Object input) {
		return getChildren(input);
	}

	public Object[] getChildren(Object element) {
		Object[] children = getOrCreateNode(element).getChildren();
		for (int i = 0; i < children.length; i++)
			getOrCreateNode(children[i]).addParent(element);
		return children;
	}

	public boolean hasChildren(Object element) {
		if (structureAdvisor != null) {
			Boolean hasChildren = structureAdvisor.hasChildren(element);
			if (hasChildren != null) {
				return hasChildren.booleanValue();
			}
		}
		return getOrCreateNode(element).hasChildren();
	}

	protected TreeNode getOrCreateNode(Object element) {
		TreeNode node = getExistingNode(element);
		if (node == null) {
			node = new TreeNode(element);
		}
		return node;
	}

	protected TreeNode getExistingNode(Object element) {
		TreeNode node = (TreeNode) elementNodes.get(element);
		return node;
	}

	protected boolean isViewerDisposed() {
		Viewer viewer = (Viewer) viewerObservable.getValue();
		return viewer == null || viewer.getControl() == null
				|| viewer.getControl().isDisposed();
	}

	public void dispose() {
		if (elementNodes != null) {
			if (!elementNodes.isEmpty()) {
				TreeNode[] nodes = new TreeNode[elementNodes.size()];
				elementNodes.values().toArray(nodes);
				for (int i = 0; i < nodes.length; i++) {
					nodes[i].dispose();
				}
				elementNodes.clear();
			}
			elementNodes = null;
		}
		if (viewerObservable != null) {
			viewerObservable.setValue(null);
			viewerObservable.dispose();
			viewerObservable = null;
		}
		viewerUpdater = null;
		comparer = null;
		knownElements = null;
		unmodifiableKnownElements = null;
		collectionFactory = null;
	}

	/**
	 * Returns the set of elements known to this content provider. Label
	 * providers may track this set if they need to be notified about additions
	 * before the viewer sees the added element, and notified about removals
	 * after the element was removed from the viewer. This is intended for use
	 * by label providers, as it will always return the items that need labels.
	 * 
	 * @return unmodifiable observable set of items that will need labels
	 */
	public IObservableSet getKnownElements() {
		return unmodifiableKnownElements;
	}

	/**
	 * Returns a listener which, when a collection change event is received,
	 * updates the tree viewer through the {@link #viewerUpdater} field, and
	 * maintains the adds and removes parents from the appropriate tree nodes.
	 * 
	 * @param parentElement
	 *            the element that is the parent element of all elements in the
	 *            observable collection.
	 * @return a listener which updates the viewer when change events occur.
	 */
	protected abstract IObservablesListener createCollectionChangeListener(
			Object parentElement);

	/**
	 * Registers the change listener to receive change events for the specified
	 * observable collection.
	 * 
	 * @param collection
	 *            the collection to observe for changes
	 * @param listener
	 *            the listener that will receive collection change events.
	 */
	protected abstract void addCollectionChangeListener(
			IObservableCollection collection, IObservablesListener listener);

	/**
	 * Unregisters the change listener from receving change events for the
	 * specified observable collection.
	 * 
	 * @param collection
	 *            the collection to stop observing.
	 * @param listener
	 *            the listener to remove
	 */
	protected abstract void removeCollectionChangeListener(
			IObservableCollection collection, IObservablesListener listener);

	protected final class TreeNode {
		private Object element;

		private Object parent;
		private Set parentSet;

		private IObservableCollection children;

		private IObservablesListener listener;

		TreeNode(Object element) {
			Assert.isNotNull(element, "element cannot be null"); //$NON-NLS-1$
			this.element = element;
			knownElements.add(element);
			elementNodes.put(element, this);
		}

		Object getElement() {
			return element;
		}

		private boolean equal(Object left, Object right) {
			if (comparer == null)
				return Util.equals(left, right);
			return comparer.equals(left, right);
		}

		public void addParent(Object newParent) {
			if (parent == null) {
				parent = newParent;
			} else if (!equal(parent, newParent)) {
				if (parentSet == null) {
					parentSet = ViewerElementSet.withComparer(comparer);
					parentSet.add(parent);
				}
				parentSet.add(newParent);
			}
		}

		public void removeParent(Object oldParent) {
			if (parentSet != null)
				parentSet.remove(oldParent);

			if (equal(parent, oldParent)) {
				if (parentSet == null || parentSet.isEmpty()) {
					parent = null;
				} else {
					Iterator iterator = parentSet.iterator();
					parent = iterator.next();
					iterator.remove();
				}
			}

			if (parentSet != null && parentSet.isEmpty())
				parentSet = null;

			if (parent == null) {
				dispose();
			}
		}

		private Object getParent() {
			return parent;
		}

		private void initChildren() {
			if (children == null) {
				children = (IObservableCollection) collectionFactory
						.createObservable(element);
				if (children == null) {
					listener = null;
					children = Observables.emptyObservableSet(realm);
				} else {
					Assert
							.isTrue(Util.equals(realm, children.getRealm()),
									"Children observable collection must be on the Display realm"); //$NON-NLS-1$
					listener = createCollectionChangeListener(element);
					addCollectionChangeListener(children, listener);
				}
			}
		}

		boolean hasChildren() {
			initChildren();
			return !children.isEmpty();
		}

		Object[] getChildren() {
			initChildren();
			return children.toArray();
		}

		private void dispose() {
			if (element != null) {
				elementNodes.remove(element);
				knownElements.remove(element);
			}
			if (children != null) {
				for (Iterator iterator = children.iterator(); iterator
						.hasNext();) {
					TreeNode child = getExistingNode(iterator.next());
					if (child != null)
						child.removeParent(element);
				}
				if (listener != null)
					removeCollectionChangeListener(children, listener);
				children.dispose();
				children = null;
			}
			element = null;
			parent = null;
			if (parentSet != null) {
				parentSet.clear();
				parentSet = null;
			}
		}
	}
}