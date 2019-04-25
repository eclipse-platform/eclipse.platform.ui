/*******************************************************************************
 * Copyright (c) 2007, 2015 Matthew Hall and others.
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
 *     Matthew Hall - bugs 226765, 222991, 226292, 266038, 283351
 *******************************************************************************/

package org.eclipse.jface.databinding.viewers;

import java.util.Set;

import org.eclipse.core.databinding.observable.IObservableCollection;
import org.eclipse.core.databinding.observable.IObservablesListener;
import org.eclipse.core.databinding.observable.list.IListChangeListener;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.ListChangeEvent;
import org.eclipse.core.databinding.observable.list.ListDiffVisitor;
import org.eclipse.core.databinding.observable.masterdetail.IObservableFactory;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.jface.internal.databinding.viewers.ObservableCollectionTreeContentProvider;
import org.eclipse.jface.internal.databinding.viewers.ViewerElementSet;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * An {@link ITreeContentProvider} for use with an {@link AbstractTreeViewer},
 * which uses the provided {@link IObservableFactory list factory} to obtain the
 * elements of a tree. Object of this class listen for changes to each
 * {@link IObservableList} created by the factory, and will insert and remove
 * viewer elements to reflect the observed changes.
 *
 * @param <E> type of the values that are provided by this object
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @since 1.2
 */
public class ObservableListTreeContentProvider<E> implements ITreeContentProvider {
	private final ObservableCollectionTreeContentProvider<E> impl;

	private static class Impl<E> extends ObservableCollectionTreeContentProvider<E> {
		private Viewer viewer;

		public Impl(IObservableFactory<? super E, ? extends IObservableList<E>> listFactory,
				TreeStructureAdvisor<? super E> structureAdvisor) {
			super(listFactory, structureAdvisor);
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			this.viewer = viewer;
			super.inputChanged(viewer, oldInput, newInput);
		}

		private class ListChangeListener implements IListChangeListener<E> {
			final E parentElement;

			public ListChangeListener(E parentElement) {
				this.parentElement = parentElement;
			}

			@Override
			public void handleListChange(ListChangeEvent<? extends E> event) {
				if (isViewerDisposed())
					return;

				// Determine which elements are being added and removed
				final Set<E> localKnownElementAdditions = ViewerElementSet.withComparer(comparer);
				final Set<E> localKnownElementRemovals = ViewerElementSet.withComparer(comparer);
				final boolean[] suspendRedraw = new boolean[] { false };
				event.diff.accept(new ListDiffVisitor<E>() {
					@Override
					public void handleAdd(int index, E element) {
						localKnownElementAdditions.add(element);
					}

					@Override
					public void handleRemove(int index, E element) {
						localKnownElementRemovals.add(element);
					}

					@Override
					public void handleMove(int oldIndex, int newIndex, E element) {
						suspendRedraw[0] = true;
						// does not affect known elements
					}

					@Override
					public void handleReplace(int index, E oldElement, E newElement) {
						suspendRedraw[0] = true;
						super.handleReplace(index, oldElement, newElement);
					}
				});
				localKnownElementRemovals.removeAll(event.getObservableList());

				Set<E> knownElementAdditions = ViewerElementSet.withComparer(comparer);
				knownElementAdditions.addAll(localKnownElementAdditions);
				knownElementAdditions.removeAll(knownElements);

				Set<E> knownElementRemovals = findPendingRemovals(parentElement, localKnownElementRemovals);
				knownElementRemovals.retainAll(knownElements);

				knownElements.addAll(knownElementAdditions);
				if (realizedElements != null) {
					realizedElements.removeAll(knownElementRemovals);
				}

				for (E element : localKnownElementAdditions) {
					getOrCreateNode(element).addParent(parentElement);
				}

				if (suspendRedraw[0])
					viewer.getControl().setRedraw(false);
				try {
					event.diff.accept(new ListDiffVisitor<Object>() {
						@Override
						public void handleAdd(int index, Object child) {
							viewerUpdater.insert(parentElement, child, index);
						}

						@Override
						public void handleRemove(int index, Object child) {
							viewerUpdater.remove(parentElement, child, index);
						}

						@Override
						public void handleReplace(int index, Object oldChild,
								Object newChild) {
							viewerUpdater.replace(parentElement, oldChild,
									newChild, index);
						}

						@Override
						public void handleMove(int oldIndex, int newIndex,
								Object child) {
							viewerUpdater.move(parentElement, child, oldIndex,
									newIndex);
						}
					});
				} finally {
					if (suspendRedraw[0])
						viewer.getControl().setRedraw(true);
				}

				for (E element : localKnownElementRemovals) {
					TreeNode node = getExistingNode(element);
					if (node != null) {
						node.removeParent(parentElement);
					}
				}

				if (realizedElements != null) {
					realizedElements.addAll(knownElementAdditions);
				}
				knownElements.removeAll(knownElementRemovals);
			}
		}

		@Override
		protected IObservablesListener createCollectionChangeListener(E parentElement) {
			return new ListChangeListener(parentElement);
		}

		@Override
		protected void addCollectionChangeListener(IObservableCollection<E> collection, IObservablesListener listener) {
			IObservableList<E> list = (IObservableList<E>) collection;
			@SuppressWarnings("unchecked")
			IListChangeListener<E> listListener = (IListChangeListener<E>) listener;
			list.addListChangeListener(listListener);
		}

		@Override
		protected void removeCollectionChangeListener(IObservableCollection<E> collection,
				IObservablesListener listener) {
			IObservableList<E> list = (IObservableList<E>) collection;
			@SuppressWarnings("unchecked")
			IListChangeListener<E> listListener = (IListChangeListener<E>) listener;
			list.removeListChangeListener(listListener);
		}
	}

	/**
	 * Constructs an ObservableListTreeContentProvider using the given list
	 * factory. Must be called from the display thread.
	 *
	 * @param listFactory
	 *            observable factory that produces an IObservableList of
	 *            children for a given parent element. Observable lists created
	 *            by this factory must be on the realm of the current display.
	 * @param structureAdvisor
	 *            an advisor that will be consulted from the implementations of
	 *            the {@link #getParent(Object)} and
	 *            {@link #hasChildren(Object)} methods, or <code>null</code> if
	 *            no advisor is available. It is recommended that clients pass a
	 *            non-null advisor if they can provide additional structural
	 *            information about the tree.
	 */
	public ObservableListTreeContentProvider(IObservableFactory<? super E, ? extends IObservableList<E>> listFactory,
			TreeStructureAdvisor<? super E> structureAdvisor) {
		impl = new Impl<>(listFactory, structureAdvisor);
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		impl.inputChanged(viewer, oldInput, newInput);
	}

	@Override
	public Object[] getElements(Object inputElement) {
		return impl.getElements(inputElement);
	}

	@Override
	public boolean hasChildren(Object element) {
		return impl.hasChildren(element);
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		return impl.getChildren(parentElement);
	}

	@Override
	public Object getParent(Object element) {
		return impl.getParent(element);
	}

	/**
	 * Disposes of this content provider. This is called by the viewer when a
	 * content provider is replaced, or when the viewer itself is disposed.
	 * <p>
	 * The viewer should not be updated during this call, as it is in the
	 * process of being disposed.
	 * </p>
	 * <p>
	 * <em>Note:</em> Data binding content providers become unusable on
	 * disposal.
	 * </p>
	 */
	@Override
	public void dispose() {
		impl.dispose();
	}

	/**
	 * Returns the set of elements known to this content provider. Label
	 * providers may track this set if they need to be notified about additions
	 * before the viewer sees the added element, and notified about removals
	 * after the element was removed from the viewer. This is intended for use
	 * by label providers, as it will always return the items that need labels.
	 *
	 * @return readableSet of items that will need labels
	 */
	public IObservableSet<E> getKnownElements() {
		return impl.getKnownElements();
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
		return impl.getRealizedElements();
	}
}