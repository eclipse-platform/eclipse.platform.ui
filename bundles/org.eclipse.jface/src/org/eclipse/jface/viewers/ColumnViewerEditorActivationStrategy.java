/*******************************************************************************
 * Copyright (c) 2006, 2014 IBM Corporation and others.
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
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 *     											 - fix for bug 187817
 *******************************************************************************/

package org.eclipse.jface.viewers;

import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;

/**
 * This class is responsible to determine if a cell selection event is triggers
 * an editor activation. Implementors can extend and overwrite to implement
 * custom editing behavior
 *
 * @since 3.3
 */
public class ColumnViewerEditorActivationStrategy {
	private final ColumnViewer viewer;

	private KeyListener keyboardActivationListener;

	/**
	 * @param viewer
	 *            the viewer the editor support is attached to
	 */
	public ColumnViewerEditorActivationStrategy(ColumnViewer viewer) {
		this.viewer = viewer;
	}

	/**
	 * @param event
	 *            the event triggering the action
	 * @return <code>true</code> if this event should open the editor
	 */
	protected boolean isEditorActivationEvent(
			ColumnViewerEditorActivationEvent event) {
		boolean singleSelect = viewer.getStructuredSelection().size() == 1;
		boolean isLeftMouseSelect = event.eventType == ColumnViewerEditorActivationEvent.MOUSE_CLICK_SELECTION && ((MouseEvent)event.sourceEvent).button == 1;

		return singleSelect && (isLeftMouseSelect
				|| event.eventType == ColumnViewerEditorActivationEvent.PROGRAMMATIC
				|| event.eventType == ColumnViewerEditorActivationEvent.TRAVERSAL);
	}

	/**
	 * @return the cell holding the current focus
	 */
	private ViewerCell getFocusCell() {
		return viewer.getColumnViewerEditor().getFocusCell();
	}

	/**
	 * @return the viewer
	 */
	public ColumnViewer getViewer() {
		return viewer;
	}

	/**
	 * Enable activation of cell editors by keyboard
	 *
	 * @param enable
	 *            <code>true</code> to enable
	 */
	public void setEnableEditorActivationWithKeyboard(boolean enable) {
		if (enable) {
			if (keyboardActivationListener == null) {
				keyboardActivationListener = new KeyListener() {

					@Override
					public void keyPressed(KeyEvent e) {
						ViewerCell cell = getFocusCell();

						if (cell != null) {
							viewer
									.triggerEditorActivationEvent(new ColumnViewerEditorActivationEvent(
											cell, e));
						}
					}

					@Override
					public void keyReleased(KeyEvent e) {

					}

				};
				viewer.getControl().addKeyListener(keyboardActivationListener);
			}
		} else if (keyboardActivationListener != null) {
			viewer.getControl().removeKeyListener(
					keyboardActivationListener);
			keyboardActivationListener = null;
		}
	}

}
