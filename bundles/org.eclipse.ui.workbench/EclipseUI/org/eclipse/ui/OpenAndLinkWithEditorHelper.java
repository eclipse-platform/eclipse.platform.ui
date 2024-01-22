/*******************************************************************************
 * Copyright (c) 2008, 2016 IBM Corporation and others.
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
package org.eclipse.ui;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.util.OpenStrategy;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredViewer;

/**
 * Helper for opening editors on the viewer's selection and link the selection
 * with the editor.
 *
 * @since 3.5
 */
public abstract class OpenAndLinkWithEditorHelper {

	private StructuredViewer viewer;

	private boolean isLinkingEnabled;

	private ISelection lastOpenSelection;

	private InternalListener listener;

	private final class InternalListener implements IOpenListener, ISelectionChangedListener, IDoubleClickListener {

		@Override
		public void open(OpenEvent event) {
			lastOpenSelection = event.getSelection();
			OpenAndLinkWithEditorHelper.this.open(lastOpenSelection, OpenStrategy.activateOnOpen());
		}

		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			final ISelection selection = event.getSelection();
			if (isLinkingEnabled && !selection.equals(lastOpenSelection) && viewer.getControl().isFocusControl())
				linkToEditor(selection);
			lastOpenSelection = null;
		}

		@Override
		public void doubleClick(DoubleClickEvent event) {
			if (!OpenStrategy.activateOnOpen())
				activate(event.getSelection());
		}

	}

	/**
	 * Creates a new helper for the given viewer.
	 *
	 * @param viewer the viewer
	 */
	public OpenAndLinkWithEditorHelper(StructuredViewer viewer) {
		Assert.isLegal(viewer != null);
		this.viewer = viewer;
		listener = new InternalListener();
		viewer.addPostSelectionChangedListener(listener);
		viewer.addOpenListener(listener);
		viewer.addDoubleClickListener(listener);
	}

	/**
	 * Sets whether editor that corresponds to the viewer's selection should be
	 * brought to front.
	 *
	 * @param enabled <code>true</code> to enable, <code>false</code> to disable
	 */
	public void setLinkWithEditor(boolean enabled) {
		isLinkingEnabled = enabled;
	}

	/**
	 * Disposes this helper.
	 * <p>
	 * Clients only need to call this method if their viewer has a longer life-cycle
	 * than this helper.
	 * </p>
	 */
	public void dispose() {
		viewer.removePostSelectionChangedListener(listener);
		viewer.removeOpenListener(listener);
		viewer.removeDoubleClickListener(listener);
		listener = null;
	}

	/**
	 * Tells to activate the editor that is open on the given selection.
	 * <p>
	 * <strong>Note:</strong> The implementation must not open a new editor.
	 * </p>
	 *
	 * @param selection the viewer's selection
	 * @since 3.5
	 */
	protected abstract void activate(ISelection selection);

	/**
	 * Tells to open an editor for the given selection.
	 *
	 * @param selection the viewer's selection
	 * @param activate  <code>true</code> if the editor should be activated,
	 *                  <code>false</code> otherwise
	 * @since 3.5
	 */
	protected abstract void open(ISelection selection, boolean activate);

	/**
	 * Tells to link the given selection to the editor that is open on the given
	 * selection but does nothing if no matching editor can be found.
	 * <p>
	 * The common implementation brings that editor to front but more advanced
	 * implementations may also select the given selection inside the editor.
	 * </p>
	 * <p>
	 * <strong>Note:</strong> The implementation must not open a new editor.
	 * </p>
	 * <p>
	 * The default implementation does nothing i.e. does not implement linking.
	 * </p>
	 *
	 * @param selection the viewer's selection
	 * @since 3.5, non-abstract since 4.3
	 */
	protected void linkToEditor(ISelection selection) {
	}

}
