/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.editors.quickdiff;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;

import org.eclipse.jface.text.Assert;

import org.eclipse.ui.texteditor.IUpdate;


/**
 * Combines a list of actions, taking the personality of the first that is
 * enabled.
 *
 * @since 3.1
 */
public final class CompositeRevertAction extends Action implements IUpdate {

	/**
	 * The actions.
	 */
	private final IAction[] fActions;

	/**
	 * Creates an action combining the two given actions.
	 *
	 * @param actions the list of actions
	 */
	public CompositeRevertAction(IAction[] actions) {
		fActions= new IAction[actions.length];
		for (int i= 0; i < actions.length; i++) {
			Assert.isNotNull(actions[i]);
		}
		System.arraycopy(actions, 0, fActions, 0, actions.length);
		update(); // take personality of the first action
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

		if (action == null)
			return;
		setText(action.getText());
		setToolTipText(action.getToolTipText());
	}


	public boolean isEnabled() {
		update();
		return getEnabledAction() != null;
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
