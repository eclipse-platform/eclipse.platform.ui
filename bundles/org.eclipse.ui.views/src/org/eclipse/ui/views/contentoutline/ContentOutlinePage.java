/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
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
package org.eclipse.ui.views.contentoutline;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.part.Page;

/**
 * An abstract base class for content outline pages.
 * <p>
 * Clients who are defining an editor may elect to provide a corresponding
 * content outline page. This content outline page will be presented to the
 * user via the standard Content Outline View (the user decides whether their
 * workbench window contains this view) whenever that editor is active.
 * This class should be subclassed.
 * </p>
 * <p>
 * Internally, each content outline page consists of a standard tree viewer;
 * selections made in the tree viewer are reported as selection change events
 * by the page (which is a selection provider). The tree viewer is not created
 * until <code>createPage</code> is called; consequently, subclasses must extend
 * <code>createControl</code> to configure the tree viewer with a proper content
 * provider, label provider, and input element.
 * </p>
 * <p>Subclasses may provide a hint for constructing the tree viewer
 * using {@link #getTreeStyle()}.</p>
 * <p>
 * Note that those wanting to use a control other than internally created
 * <code>TreeViewer</code> will need to implement
 * <code>IContentOutlinePage</code> directly rather than subclassing this class.
 * </p>
 */
public abstract class ContentOutlinePage extends Page implements IContentOutlinePage, ISelectionChangedListener {
	private final ListenerList<ISelectionChangedListener> selectionChangedListeners = new ListenerList<>();

	private TreeViewer treeViewer;

	/**
	 * Create a new content outline page.
	 */
	protected ContentOutlinePage() {
		super();
	}

	@Override
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		selectionChangedListeners.add(listener);
	}

	/**
	 * The <code>ContentOutlinePage</code> implementation of this
	 * <code>IContentOutlinePage</code> method creates a tree viewer. Subclasses
	 * must extend this method configure the tree viewer with a proper content
	 * provider, label provider, and input element.
	 *
	 * @param parent the parent composite
	 */
	@Override
	public void createControl(Composite parent) {
		treeViewer = new TreeViewer(parent, getTreeStyle());
		treeViewer.addSelectionChangedListener(this);
	}

	/**
	 * A hint for the styles to use while constructing the TreeViewer.
	 * <p>Subclasses may override.</p>
	 *
	 * @return the tree styles to use. By default, SWT.MULTI | SWT.H_SCROLL |
	 *         SWT.V_SCROLL
	 * @since 3.6
	 */
	protected int getTreeStyle() {
		return SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL;
	}

	/**
	 * Fires a selection changed event.
	 *
	 * @param selection the new selection
	 */
	protected void fireSelectionChanged(ISelection selection) {
		// create an event
		final SelectionChangedEvent event = new SelectionChangedEvent(this,
				selection);

		// fire the event
		for (final ISelectionChangedListener l : selectionChangedListeners) {
			SafeRunner.run(new SafeRunnable() {
				@Override
				public void run() {
					l.selectionChanged(event);
				}
			});
		}
	}

	@Override
	public Control getControl() {
		if (treeViewer == null) {
			return null;
		}
		return treeViewer.getControl();
	}

	@Override
	public ISelection getSelection() {
		if (treeViewer == null) {
			return StructuredSelection.EMPTY;
		}
		return treeViewer.getSelection();
	}

	/**
	 * Returns this page's tree viewer.
	 *
	 * @return this page's tree viewer, or <code>null</code> if
	 *   <code>createControl</code> has not been called yet
	 */
	protected TreeViewer getTreeViewer() {
		return treeViewer;
	}

	@Override
	public void init(IPageSite pageSite) {
		super.init(pageSite);
		pageSite.setSelectionProvider(this);
	}

	@Override
	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		selectionChangedListeners.remove(listener);
	}

	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		fireSelectionChanged(event.getSelection());
	}

	/**
	 * Sets focus to a part in the page.
	 */
	@Override
	public void setFocus() {
		treeViewer.getControl().setFocus();
	}

	@Override
	public void setSelection(ISelection selection) {
		if (treeViewer != null) {
			treeViewer.setSelection(selection);
		}
	}
}
