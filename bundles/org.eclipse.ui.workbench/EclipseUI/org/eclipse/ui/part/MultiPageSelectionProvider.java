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
package org.eclipse.ui.part;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.IPostSelectionProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorPart;

/**
 * Manages the current selection in a multi-page editor by tracking the active
 * nested editor within the multi-page editor. When the selection changes,
 * notifications are sent to all registered listeners.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed. The base
 * implementation of <code>MultiPageEditor.init</code> creates an instance of
 * this class.
 * </p>
 *
 * @noextend This class is not intended to be subclassed by clients.
 */
public class MultiPageSelectionProvider implements IPostSelectionProvider {

	/**
	 * Registered selection changed listeners (element type:
	 * <code>ISelectionChangedListener</code>).
	 */
	private ListenerList<ISelectionChangedListener> listeners = new ListenerList<>();

	/**
	 * Registered post selection changed listeners.
	 */
	private ListenerList<ISelectionChangedListener> postListeners = new ListenerList<>();

	/**
	 * The multi-page editor.
	 */
	private MultiPageEditorPart multiPageEditor;

	/**
	 * Creates a selection provider for the given multi-page editor.
	 *
	 * @param multiPageEditor the multi-page editor
	 */
	public MultiPageSelectionProvider(MultiPageEditorPart multiPageEditor) {
		Assert.isNotNull(multiPageEditor);
		this.multiPageEditor = multiPageEditor;
	}

	@Override
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		listeners.add(listener);
	}

	/**
	 * Adds a listener for post selection changes in this multi page selection
	 * provider.
	 *
	 * @param listener a selection changed listener
	 * @since 3.2
	 */
	@Override
	public void addPostSelectionChangedListener(ISelectionChangedListener listener) {
		postListeners.add(listener);
	}

	/**
	 * Notifies all registered selection changed listeners that the editor's
	 * selection has changed. Only listeners registered at the time this method is
	 * called are notified.
	 *
	 * @param event the selection changed event
	 */
	public void fireSelectionChanged(final SelectionChangedEvent event) {
		fireEventChange(event, listeners);
	}

	/**
	 * Notifies all post selection changed listeners that the editor's selection has
	 * changed.
	 *
	 * @param event the event to propogate.
	 * @since 3.2
	 */
	public void firePostSelectionChanged(final SelectionChangedEvent event) {
		fireEventChange(event, postListeners);
	}

	private void fireEventChange(final SelectionChangedEvent event,
			ListenerList<ISelectionChangedListener> listenersList) {
		for (final ISelectionChangedListener l : listenersList) {
			SafeRunner.run(new SafeRunnable() {
				@Override
				public void run() {
					l.selectionChanged(event);
				}
			});
		}
	}

	/**
	 * Returns the multi-page editor.
	 *
	 * @return the multi-page editor.
	 */
	public MultiPageEditorPart getMultiPageEditor() {
		return multiPageEditor;
	}

	@Override
	public ISelection getSelection() {
		IEditorPart activeEditor = multiPageEditor.getActiveEditor();
		if (activeEditor != null) {
			ISelectionProvider selectionProvider = activeEditor.getSite().getSelectionProvider();
			if (selectionProvider != null) {
				return selectionProvider.getSelection();
			}
		}
		return StructuredSelection.EMPTY;
	}

	@Override
	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		listeners.remove(listener);
	}

	/**
	 * Removes a listener for post selection changes in this multi page selection
	 * provider.
	 *
	 * @param listener a selection changed listener
	 * @since 3.2
	 */
	@Override
	public void removePostSelectionChangedListener(ISelectionChangedListener listener) {
		postListeners.remove(listener);
	}

	@Override
	public void setSelection(ISelection selection) {
		IEditorPart activeEditor = multiPageEditor.getActiveEditor();
		if (activeEditor != null) {
			ISelectionProvider selectionProvider = activeEditor.getSite().getSelectionProvider();
			if (selectionProvider != null) {
				selectionProvider.setSelection(selection);
			}
		}
	}
}
