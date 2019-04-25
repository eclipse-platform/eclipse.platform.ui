/*******************************************************************************
 * Copyright (c) 2008, 2018 Matthew Hall and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 207858)
 *     Matthew Hall - bugs 226765, 239015, 222991, 263693, 263956, 226292,
 *                    266038
 *     Conrad Groth - Bug 371756
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.viewers;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

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
import org.eclipse.jface.databinding.swt.DisplayRealm;
import org.eclipse.jface.databinding.viewers.TreeStructureAdvisor;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
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
 * @param <E> type of the values that are provided by this object
 *
 * @since 1.2
 */
public abstract class ObservableCollectionTreeContentProvider<E> implements ITreeContentProvider {
	private Realm realm;

	private Display display;

	private IObservableValue<Viewer> viewerObservable;

	/**
	 * Element comparer used by the viewer (may be null).
	 */
	protected IElementComparer comparer;

	private IObservableFactory<Viewer, IObservableSet<E>> elementSetFactory;

	/**
	 * Interfaces for sending updates to the viewer.
	 */
	protected TreeViewerUpdater viewerUpdater;

	/**
	 * Observable set of all elements known to the content provider. Subclasses
	 * must add new elements to this set <b>before</b> adding them to the
	 * viewer, and must remove old elements from this set <b>after</b> removing
	 * them from the viewer.
	 */
	protected IObservableSet<E> knownElements;
	private IObservableSet<E> unmodifiableKnownElements;

	/**
	 * Observable set of known elements which have been realized in the viewer.
	 * Subclasses must add new elements to this set <b>after</b> adding them to
	 * the viewer, and must remove old elements from this set <b>before</b>
	 * removing them from the viewer.
	 */
	protected IObservableSet<E> realizedElements;
	private IObservableSet<E> unmodifiableRealizedElements;

	private IObservableFactory<? super E, ? extends IObservableCollection<E>> collectionFactory;

	private Map<E, TreeNode> elementNodes;

	private TreeStructureAdvisor<? super E> structureAdvisor;

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
			IObservableFactory<? super E, ? extends IObservableCollection<E>> collectionFactory,
			TreeStructureAdvisor<? super E> structureAdvisor) {
		this.structureAdvisor = structureAdvisor;
		display = Display.getDefault();
		realm = DisplayRealm.getRealm(display);
		viewerObservable = new WritableValue<>(realm);
		viewerUpdater = null;

		elementSetFactory = new IObservableFactory<Viewer, IObservableSet<E>>() {
			@Override
			public IObservableSet<E> createObservable(Viewer target) {
				return ObservableViewerElementSet.withComparer(realm, null, getElementComparer(target));
			}
		};
		knownElements = MasterDetailObservables.detailSet(viewerObservable, elementSetFactory, null);
		unmodifiableKnownElements = Observables.unmodifiableObservableSet(knownElements);

		Assert
				.isNotNull(collectionFactory,
						"Collection factory cannot be null"); //$NON-NLS-1$

		this.collectionFactory = collectionFactory;
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if (elementNodes != null && !elementNodes.isEmpty()) {
			// Ensure we flush any observable collection listeners
			@SuppressWarnings("unchecked")
			TreeNode[] oldNodes = new ObservableCollectionTreeContentProvider.TreeNode[elementNodes.size()];
			elementNodes.values().toArray(oldNodes);
			for (TreeNode oldNode : oldNodes)
				oldNode.dispose();
			elementNodes.clear();
			elementNodes = null;
		}

		setViewer(viewer);

		knownElements.clear();
		if (realizedElements != null)
			realizedElements.clear();

		if (newInput != null) {
			getElements(newInput);
		}

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
		if (viewer instanceof CheckboxTreeViewer)
			return new CheckboxTreeViewerUpdater((CheckboxTreeViewer) viewer);
		if (viewer instanceof AbstractTreeViewer)
			return new TreeViewerUpdater((AbstractTreeViewer) viewer);
		throw new IllegalArgumentException(
				"This content provider only works with AbstractTreeViewer"); //$NON-NLS-1$
	}

	@Override
	public Object getParent(Object element) {
		if (structureAdvisor != null) {
			@SuppressWarnings("unchecked")
			Object parentFromAdvisor = structureAdvisor.getParent((E) element);
			if (parentFromAdvisor != null) {
				return parentFromAdvisor;
			}
		}
		@SuppressWarnings("unchecked")
		TreeNode node = getExistingNode((E) element);
		if (node != null)
			return node.getParent();
		return null;
	}

	@Override
	public Object[] getElements(Object input) {
		return getChildren(input, true);
	}

	@Override
	public Object[] getChildren(Object element) {
		return getChildren(element, false);
	}

	@SuppressWarnings("unchecked")
	private Object[] getChildren(Object element, boolean input) {
		TreeNode node = getOrCreateNode((E) element, input);
		Object[] children = node.getChildren().toArray();
		for (Object childElement : children) {
			getOrCreateNode((E) childElement, false).addParent((E) element);
		}
		asyncUpdateRealizedElements();
		return children;
	}

	boolean asyncUpdatePending;
	Runnable asyncUpdateRunnable;

	private void asyncUpdateRealizedElements() {
		if (realizedElements == null)
			return;
		if (asyncUpdatePending)
			return;
		if (!realizedElements.equals(knownElements)) {
			if (asyncUpdateRunnable == null) {
				asyncUpdateRunnable = () -> {
					// If we've been disposed, exit early
					if (knownElements == null) {
						return;
					}
					asyncUpdatePending = false;
					if (realizedElements != null) {
						realizedElements.addAll(knownElements);
					}
				};
			}
			asyncUpdatePending = true;
			display.asyncExec(asyncUpdateRunnable);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean hasChildren(Object element) {
		if (structureAdvisor != null) {
			Boolean hasChildren = structureAdvisor.hasChildren((E) element);
			if (hasChildren != null) {
				return hasChildren.booleanValue();
			}
		}
		return getOrCreateNode((E) element, false).hasChildren();
	}

	protected TreeNode getOrCreateNode(E element) {
		return getOrCreateNode(element, false);
	}

	private TreeNode getOrCreateNode(E element, boolean input) {
		TreeNode node = getExistingNode(element);
		if (node == null) {
			node = new TreeNode(element);
			elementNodes.put(element, node);
		}
		// In case the input element is also a visible node in the tree.
		if (!input)
			knownElements.add(element);
		return node;
	}

	protected TreeNode getExistingNode(E element) {
		TreeNode node = elementNodes.get(element);
		return node;
	}

	protected boolean isViewerDisposed() {
		Viewer viewer = viewerObservable.getValue();
		return viewer == null || viewer.getControl() == null
				|| viewer.getControl().isDisposed();
	}

	@Override
	public void dispose() {
		if (elementNodes != null) {
			if (!elementNodes.isEmpty()) {
				@SuppressWarnings("unchecked")
				TreeNode[] nodes = new ObservableCollectionTreeContentProvider.TreeNode[elementNodes.size()];
				elementNodes.values().toArray(nodes);
				for (TreeNode node : nodes) {
					node.dispose();
				}
				elementNodes.clear();
			}
			elementNodes = null;
		}
		if (viewerObservable != null) {
			viewerObservable.dispose();
			viewerObservable = null;
		}
		viewerUpdater = null;
		comparer = null;
		knownElements = null;
		unmodifiableKnownElements = null;
		collectionFactory = null;
		asyncUpdateRunnable = null;
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
	public IObservableSet<E> getKnownElements() {
		return unmodifiableKnownElements;
	}

	/**
	 * Returns the set of known elements which have been realized in the viewer.
	 * Clients may track this set in order to perform custom actions on elements
	 * while they are known to be present in the viewer.
	 *
	 * @return the set of known elements which have been realized in the viewer.
	 * @since 1.3
	 */
	public IObservableSet<E> getRealizedElements() {
		if (realizedElements == null) {
			realizedElements = MasterDetailObservables.detailSet(
					viewerObservable, elementSetFactory, null);
			unmodifiableRealizedElements = Observables
					.unmodifiableObservableSet(realizedElements);
			asyncUpdateRealizedElements();
		}
		return unmodifiableRealizedElements;
	}

	/**
	 * Returns the set of all elements that would be removed from the known
	 * elements set if the given elements were removed as children of the given
	 * parent element.
	 *
	 * @param parent
	 *            the parent element of the elements being removed
	 * @param elementsToBeRemoved
	 *            the elements being removed
	 * @return the set of all elements that would be removed from the known
	 *         elements set
	 */
	protected Set<E> findPendingRemovals(E parent, Collection<? extends E> elementsToBeRemoved) {
		Set<E> result = ViewerElementSet.withComparer(comparer);
		Set<E> parents = ViewerElementSet.withComparer(comparer);
		parents.add(parent);
		accumulatePendingRemovals(result, parents, elementsToBeRemoved);
		return result;
	}

	private void accumulatePendingRemovals(Set<E> removals, Set<E> parents, Collection<? extends E> elementsToRemove) {
		for (E element : elementsToRemove) {
			TreeNode node = getExistingNode(element);
			if (node != null) {
				if (parents.containsAll(node.getParents())) {
					removals.add(element);
					parents.add(element);
					Collection<E> children = node.getChildren();
					accumulatePendingRemovals(removals, parents, children);
				}
			}
		}
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
	protected abstract IObservablesListener createCollectionChangeListener(E parentElement);

	/**
	 * Registers the change listener to receive change events for the specified
	 * observable collection.
	 *
	 * @param collection
	 *            the collection to observe for changes
	 * @param listener
	 *            the listener that will receive collection change events.
	 */
	protected abstract void addCollectionChangeListener(IObservableCollection<E> collection,
			IObservablesListener listener);

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
			IObservableCollection<E> collection, IObservablesListener listener);

	protected boolean equal(Object left, Object right) {
		if (comparer == null)
			return Objects.equals(left, right);
		return comparer.equals(left, right);
	}

	protected final class TreeNode {
		private E element;

		private E parent;
		private Set<E> parentSet;

		private IObservableCollection<E> children;

		private IObservablesListener listener;

		TreeNode(E element) {
			Assert.isNotNull(element, "element cannot be null"); //$NON-NLS-1$
			this.element = element;
		}

		Object getElement() {
			return element;
		}

		public void addParent(E newParent) {
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
			if (parentSet != null) {
				parentSet.remove(oldParent);
				if (parentSet.isEmpty())
					parentSet = null;
			}

			if (equal(parent, oldParent)) {
				if (parentSet == null) {
					parent = null;
				} else {
					parent = parentSet.iterator().next();
				}
			}

			if (parent == null) {
				dispose();
			}
		}

		private E getParent() {
			return parent;
		}

		public Set<E> getParents() {
			if (parentSet != null)
				return parentSet;
			if (parent != null)
				return Collections.singleton(parent);
			return Collections.EMPTY_SET;
		}

		private void initChildren() {
			if (children == null) {
				children = collectionFactory.createObservable(element);
				if (children == null) {
					listener = null;
					children = Observables.emptyObservableSet(realm);
				} else {
					Assert
							.isTrue(Objects.equals(realm, children.getRealm()),
									"Children observable collection must be on the Display realm"); //$NON-NLS-1$
					listener = createCollectionChangeListener(element);
					addCollectionChangeListener(children, listener);
					knownElements.addAll(children);
				}
			}
		}

		boolean hasChildren() {
			initChildren();
			return !children.isEmpty();
		}

		public Collection<E> getChildren() {
			initChildren();
			return children;
		}

		private void dispose() {
			if (element != null) {
				elementNodes.remove(element);
			}
			if (children != null && !children.isDisposed()) {
				for (E elem : children) {
					TreeNode child = getExistingNode(elem);
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