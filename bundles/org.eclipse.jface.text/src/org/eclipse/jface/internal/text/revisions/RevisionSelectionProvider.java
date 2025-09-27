/*******************************************************************************
 * Copyright (c) 2006, 2016 IBM Corporation and others.
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
package org.eclipse.jface.internal.text.revisions;

import org.eclipse.core.runtime.ListenerList;

import org.eclipse.jface.viewers.IPostSelectionProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;

import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.revisions.Revision;

/**
 * A selection provider for annotate revisions. Selections of a revision can currently happen in
 * following ways - note that this list may be changed in the future:
 * <ul>
 * <li>when the user clicks the revision ruler with the mouse</li>
 * <li>when the caret is moved to a revision's line (only on post-selection)</li>
 * </ul>
 * <p>
 * Calling {@link #setSelection(ISelection)} will set the current sticky revision on the ruler.
 * </p>
 *
 * @since 3.2
 */
public final class RevisionSelectionProvider implements ISelectionProvider {

	/**
	 * Post selection listener on the viewer that remembers the selection provider it is registered
	 * with.
	 */
	private final class PostSelectionListener implements ISelectionChangedListener {
		private final IPostSelectionProvider fPostProvider;

		public PostSelectionListener(IPostSelectionProvider postProvider) {
			postProvider.addPostSelectionChangedListener(this);
			fPostProvider= postProvider;
		}

		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			ISelection selection= event.getSelection();
			if (selection instanceof ITextSelection ts) {
				int offset= ts.getOffset();
				setSelectedRevision(fPainter.getRevision(offset));
			}

		}

		public void dispose() {
			fPostProvider.removePostSelectionChangedListener(this);
		}
	}

	private final RevisionPainter fPainter;

	private final ListenerList<ISelectionChangedListener> fListeners= new ListenerList<>();

	/**
	 * The text viewer once we are installed, <code>null</code> if not installed.
	 */
	private ITextViewer fViewer;
	/**
	 * The selection listener on the viewer, or <code>null</code>.
	 */
	private PostSelectionListener fSelectionListener;
	/**
	 * The last selection, or <code>null</code>.
	 */
	private Revision fSelection;
	/**
	 * Incoming selection changes are ignored while sending out events.
	 *
	 * @since 3.3
	 */
	private boolean fIgnoreEvents= false;

	/**
	 * Creates a new selection provider.
	 *
	 * @param painter the painter that the created provider interacts with
	 */
	RevisionSelectionProvider(RevisionPainter painter) {
		fPainter= painter;
	}

	@Override
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		fListeners.add(listener);
	}

	@Override
	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		fListeners.remove(listener);
	}

	@Override
	public ISelection getSelection() {
		if (fSelection == null)
			return StructuredSelection.EMPTY;
		return new StructuredSelection(fSelection);
	}

	@Override
	public void setSelection(ISelection selection) {
		if (fIgnoreEvents)
			return;
		if (selection instanceof IStructuredSelection) {
			Object first= ((IStructuredSelection) selection).getFirstElement();
			if (first instanceof Revision)
				fPainter.handleRevisionSelected((Revision) first);
			else if (first instanceof String)
				fPainter.handleRevisionSelected((String) first);
			else if (selection.isEmpty())
				fPainter.handleRevisionSelected((Revision) null);
		}
	}

	/**
	 * Installs the selection provider on the viewer.
	 *
	 * @param viewer the viewer on which we listen to for post selection events
	 */
	void install(ITextViewer viewer) {
		uninstall();
		fViewer= viewer;
		if (fViewer != null) {
			ISelectionProvider provider= fViewer.getSelectionProvider();
			if (provider instanceof IPostSelectionProvider postProvider) {
				fSelectionListener= new PostSelectionListener(postProvider);
			}
		}
	}

	/**
	 * Uninstalls the selection provider.
	 */
	void uninstall() {
		fViewer= null;
		if (fSelectionListener != null) {
			fSelectionListener.dispose();
			fSelectionListener= null;
		}
	}

	/**
	 * Private protocol used by {@link RevisionPainter} to signal selection of a revision.
	 *
	 * @param revision the selected revision, or <code>null</code> for none
	 */
	void revisionSelected(Revision revision) {
		setSelectedRevision(revision);
	}

	/**
	 * Updates the currently selected revision and sends out an event if it changed.
	 *
	 * @param revision the newly selected revision or <code>null</code> for none
	 */
	private void setSelectedRevision(Revision revision) {
		if (revision != fSelection) {
			fSelection= revision;
			fireSelectionEvent();
		}
	}

	private void fireSelectionEvent() {
		fIgnoreEvents= true;
		try {
			ISelection selection= getSelection();
			SelectionChangedEvent event= new SelectionChangedEvent(this, selection);

			for (ISelectionChangedListener listener : fListeners) {
				listener.selectionChanged(event);
			}
		} finally {
			fIgnoreEvents= false;
		}
	}
}
