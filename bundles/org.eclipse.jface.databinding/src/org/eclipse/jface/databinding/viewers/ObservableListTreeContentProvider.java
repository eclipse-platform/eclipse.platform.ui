/*******************************************************************************
 * Copyright (c) 2007-2008 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 207858)
 *     Matthew Hall - bugs 226765, 222991, 226292, 266038
 *******************************************************************************/

package org.eclipse.jface.databinding.viewers;

import java.util.Iterator;
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
 * @noextend This class is not intended to be subclassed by clients.
 * @since 1.2
 */
public class ObservableListTreeContentProvider implements ITreeContentProvider {
	private final ObservableCollectionTreeContentProvider impl;

	private static class Impl extends ObservableCollectionTreeContentProvider {
		private Viewer viewer;

		public Impl(IObservableFactory listFactory,
				TreeStructureAdvisor structureAdvisor) {
			super(listFactory, structureAdvisor);
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			this.viewer = viewer;
			super.inputChanged(viewer, oldInput, newInput);
		}

		private class ListChangeListener implements IListChangeListener {
			final Object parentElement;

			public ListChangeListener(Object parentElement) {
				this.parentElement = parentElement;
			}

			public void handleListChange(ListChangeEvent event) {
				if (isViewerDisposed())
					return;

				// Determine which elements are being added and removed
				final Set localKnownElementAdditions = ViewerElementSet
						.withComparer(comparer);
				final Set localKnownElementRemovals = ViewerElementSet
						.withComparer(comparer);
				final boolean[] suspendRedraw = new boolean[] { false };
				event.diff.accept(new ListDiffVisitor() {
					public void handleAdd(int index, Object element) {
						localKnownElementAdditions.add(element);
					}

					public void handleRemove(int index, Object element) {
						localKnownElementRemovals.add(element);
					}

					public void handleMove(int oldIndex, int newIndex,
							Object element) {
						suspendRedraw[0] = true;
						// does not affect known elements
					}

					public void handleReplace(int index, Object oldElement,
							Object newElement) {
						suspendRedraw[0] = true;
						super.handleReplace(index, oldElement, newElement);
					}
				});
				localKnownElementRemovals.removeAll(event.getObservableList());

				Set knownElementAdditions = ViewerElementSet
						.withComparer(comparer);
				knownElementAdditions.addAll(localKnownElementAdditions);
				knownElementAdditions.removeAll(knownElements);

				Set knownElementRemovals = findPendingRemovals(parentElement,
						localKnownElementRemovals);
				knownElementRemovals.retainAll(knownElements);

				knownElements.addAll(knownElementAdditions);
				if (realizedElements != null) {
					realizedElements.removeAll(knownElementRemovals);
				}

				for (Iterator it = localKnownElementAdditions.iterator(); it
						.hasNext();) {
					getOrCreateNode(it.next()).addParent(parentElement);
				}

				if (suspendRedraw[0])
					viewer.getControl().setRedraw(false);
				try {
					event.diff.accept(new ListDiffVisitor() {
						public void handleAdd(int index, Object child) {
							viewerUpdater.insert(parentElement, child, index);
						}

						public void handleRemove(int index, Object child) {
							viewerUpdater.remove(parentElement, child, index);
						}

						public void handleReplace(int index, Object oldChild,
								Object newChild) {
							viewerUpdater.replace(parentElement, oldChild,
									newChild, index);
						}

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

				for (Iterator it = localKnownElementRemovals.iterator(); it
						.hasNext();) {
					TreeNode node = getExistingNode(it.next());
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

		protected IObservablesListener createCollectionChangeListener(
				Object parentElement) {
			return new ListChangeListener(parentElement);
		}

		protected void addCollectionChangeListener(
				IObservableCollection collection, IObservablesListener listener) {
			IObservableList list = (IObservableList) collection;
			IListChangeListener listListener = (IListChangeListener) listener;
			list.addListChangeListener(listListener);
		}

		protected void removeCollectionChangeListener(
				IObservableCollection collection, IObservablesListener listener) {
			IObservableList list = (IObservableList) collection;
			IListChangeListener listListener = (IListChangeListener) listener;
			list.removeListChangeListener(listListener);
		}
	}

	/**
	 * Constructs an ObservableListTreeContentProvider using the given list
	 * factory.
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
	public ObservableListTreeContentProvider(IObservableFactory listFactory,
			TreeStructureAdvisor structureAdvisor) {
		impl = new Impl(listFactory, structureAdvisor);
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		impl.inputChanged(viewer, oldInput, newInput);
	}

	public Object[] getElements(Object inputElement) {
		return impl.getElements(inputElement);
	}

	public boolean hasChildren(Object element) {
		return impl.hasChildren(element);
	}

	public Object[] getChildren(Object parentElement) {
		return impl.getChildren(parentElement);
	}

	public Object getParent(Object element) {
		return impl.getParent(element);
	}

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
	public IObservableSet getKnownElements() {
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
	public IObservableSet getRealizedElements() {
		return impl.getRealizedElements();
	}
}