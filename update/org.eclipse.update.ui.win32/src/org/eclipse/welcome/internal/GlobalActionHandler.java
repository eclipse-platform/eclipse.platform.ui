/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.welcome.internal;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.ui.*;
import org.eclipse.ui.IWorkbenchActionConstants;

/**
 *
 */
public class GlobalActionHandler {
	private boolean active = false;
	private Combo combo;
	private ControlListener controlListener;

	private class ControlListener implements Listener {
		public void handleEvent(Event event) {
			switch (event.type) {
				case SWT.Activate :
					active = true;
					updateActionsEnableState();
					break;
				case SWT.Deactivate :
					active = false;
					updateActionsEnableState();
					break;
				default :
					break;
			}
		}
	}

	private class GlobalAction extends Action {
		private String id;

		public GlobalAction(String id) {
			this.id = id;
		}

		public void run() {
			doGlobalAction(id);
		}
	}

	private GlobalAction cutAction;
	private GlobalAction copyAction;
	private GlobalAction pasteAction;
	private GlobalAction deleteAction;
	private GlobalAction selectAllAction;
	private GlobalAction undoAction;

	public GlobalActionHandler(IActionBars actionBar, Combo combo) {
		this.combo = combo;
		makeActions();
		actionBar.setGlobalActionHandler(
			IWorkbenchActionConstants.CUT,
			cutAction);
		actionBar.setGlobalActionHandler(
			IWorkbenchActionConstants.COPY,
			copyAction);
		actionBar.setGlobalActionHandler(
			IWorkbenchActionConstants.PASTE,
			pasteAction);
		actionBar.setGlobalActionHandler(
			IWorkbenchActionConstants.DELETE,
			deleteAction);
		actionBar.setGlobalActionHandler(
			IWorkbenchActionConstants.SELECT_ALL,
			selectAllAction);
		actionBar.setGlobalActionHandler(
			IWorkbenchActionConstants.UNDO,
			undoAction);
		controlListener = new ControlListener();
		combo.addListener(SWT.Activate, controlListener);
		combo.addListener(SWT.Deactivate, controlListener);
	}

	public void dispose() {
		if (!combo.isDisposed()) {
			combo.removeListener(SWT.Activate, controlListener);
			combo.removeListener(SWT.Deactivate, controlListener);
		}
	}

	private void makeActions() {
		cutAction = new GlobalAction(IWorkbenchActionConstants.CUT);
		copyAction = new GlobalAction(IWorkbenchActionConstants.COPY);
		pasteAction = new GlobalAction(IWorkbenchActionConstants.PASTE);
		deleteAction = new GlobalAction(IWorkbenchActionConstants.DELETE);
		selectAllAction =
			new GlobalAction(IWorkbenchActionConstants.SELECT_ALL);
		undoAction = new GlobalAction(IWorkbenchActionConstants.UNDO);
	}

	private void updateActionsEnableState() {
		if (!active) {
			cutAction.setEnabled(false);
			copyAction.setEnabled(false);
			pasteAction.setEnabled(false);
			deleteAction.setEnabled(false);
			selectAllAction.setEnabled(false);
			undoAction.setEnabled(false);
		} else {
			cutAction.setEnabled(true);
			copyAction.setEnabled(true);
			pasteAction.setEnabled(true);
			deleteAction.setEnabled(true);
			selectAllAction.setEnabled(true);
			undoAction.setEnabled(true);
		}
	}

	private void doGlobalAction(String id) {
		if (id.equals(IWorkbenchActionConstants.CUT))
			combo.cut();
		else if (id.equals(IWorkbenchActionConstants.COPY))
			combo.copy();
		else if (id.equals(IWorkbenchActionConstants.PASTE))
			combo.paste();
		else if (id.equals(IWorkbenchActionConstants.DELETE))
			doDelete();
		else if (id.equals(IWorkbenchActionConstants.SELECT_ALL))
			doSelectAll();
	}

	private void doDelete() {
		String text = combo.getText();
		Point selection = combo.getSelection();
		if (selection.x == selection.y)
			return;
		String left = text.substring(0, selection.x);
		String right = text.substring(selection.y + 1);
		combo.setText(left + right);
	}

	private void doSelectAll() {
		String text = combo.getText();
		Point newSelection = new Point(0, text.length());
		combo.setSelection(newSelection);
	}
}