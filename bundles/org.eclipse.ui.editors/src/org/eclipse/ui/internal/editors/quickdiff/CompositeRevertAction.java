/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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
package org.eclipse.ui.internal.editors.quickdiff;

import org.eclipse.core.runtime.Assert;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.IPostSelectionProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;

import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.IUpdate;


/**
 * Combines a list of actions, taking the personality of the first that is
 * enabled.
 *
 * @since 3.1
 */
public final class CompositeRevertAction extends Action implements IUpdate, ISelectionChangedListener {

	/**
	 * The actions.
	 */
	private final IAction[] fActions;

	/**
	 * Creates an action combining the two given actions.
	 *
	 * @param editor the editor
	 * @param actions the list of actions
	 */
	public CompositeRevertAction(ITextEditor editor, IAction[] actions) {
		fActions= new IAction[actions.length];
		for (IAction action : actions) {
			Assert.isNotNull(action);
		}

		System.arraycopy(actions, 0, fActions, 0, actions.length);

		ISelectionProvider selectionProvider= editor.getSelectionProvider();
		if (selectionProvider instanceof IPostSelectionProvider) {
			((IPostSelectionProvider)selectionProvider).addPostSelectionChangedListener(this);
		}

		update();
	}

	@Override
	public void update() {
		for (IAction fAction : fActions) {
			if (fAction instanceof IUpdate) {
				((IUpdate) fAction).update();
			}
		}
		IAction action= getEnabledAction();
		setEnabled(getEnabledAction() != null);
		if (action == null) {
			return;
		}
		setText(action.getText());
		setToolTipText(action.getToolTipText());
	}

	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		update();
	}

	@Override
	public void run() {
		IAction action= getEnabledAction();
		if (action != null) {
			action.run();
		}
	}

	/**
	 * Returns the first enabled action, or <code>null</code> if none is
	 * enabled.
	 *
	 * @return the first enabled action, or <code>null</code> if none is
	 *         enabled
	 */
	private IAction getEnabledAction() {
		for (IAction fAction : fActions) {
			if (fAction.isEnabled()) {
				return fAction;
			}
		}
		return null;
	}
}
