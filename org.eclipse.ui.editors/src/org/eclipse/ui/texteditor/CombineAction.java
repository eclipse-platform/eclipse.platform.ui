/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.texteditor;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;

import org.eclipse.jface.text.Assert;


/**
 * Combines a list of actions, taking the personality of the first that is
 * enabled.
 * 
 * @since 3.1
 */
final class CombineAction extends Action implements IUpdate {

	/**
	 * The actions.
	 */
	private final IAction[] fActions;

	/**
	 * Creates an action combining the two given actions.
	 * 
	 * @param actions the list of actions
	 */
	public CombineAction(IAction[] actions) {
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
			update(fActions[i]);
		}
		IAction action= getEnabledAction();
		
		if (action == null) {
			setEnabled(false);
			return;
		}
		setEnabled(true);
		setText(action.getText());
		setToolTipText(action.getToolTipText());
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
	 * Updates <code>action</code> if it implements <code>IUpdate</code>.
	 * 
	 * @param action the action to update
	 */
	private void update(IAction action) {
		if (action instanceof IUpdate)
			((IUpdate) action).update();
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
