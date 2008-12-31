/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
		for (int i= 0; i < actions.length; i++)
			Assert.isNotNull(actions[i]);

		System.arraycopy(actions, 0, fActions, 0, actions.length);

		ISelectionProvider selectionProvider= editor.getSelectionProvider();
		if (selectionProvider instanceof IPostSelectionProvider)
			((IPostSelectionProvider)selectionProvider).addPostSelectionChangedListener(this);

		update();
	}

	/*
	 * @see org.eclipse.ui.texteditor.IUpdate#update()
	 */
	public void update() {
		for (int i= 0; i < fActions.length; i++) {
			if (fActions[i] instanceof IUpdate)
				((IUpdate) fActions[i]).update();
		}
		IAction action= getEnabledAction();
		setEnabled(getEnabledAction() != null);
		if (action == null)
			return;
		setText(action.getText());
		setToolTipText(action.getToolTipText());
	}

	/*
	 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
	 * @since 3.3
	 */
	public void selectionChanged(SelectionChangedEvent event) {
		update();
	}

	/*
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		IAction action= getEnabledAction();
		if (action != null)
			action.run();
	}

	/**
	 * Returns the first enabled action, or <code>null</code> if none is
	 * enabled.
	 *
	 * @return the first enabled action, or <code>null</code> if none is
	 *         enabled
	 */
	private IAction getEnabledAction() {
		for (int i= 0; i < fActions.length; i++) {
			if (fActions[i].isEnabled())
				return fActions[i];
		}
		return null;
	}
}
