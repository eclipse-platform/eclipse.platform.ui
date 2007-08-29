/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.actions;


import java.util.List;

import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchShortcutExtension;
import org.eclipse.debug.internal.ui.stringsubstitution.SelectedResourceManager;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;

/**
 * Launch shortcut action (proxy to a launch shortcut extension)
 */
public class LaunchShortcutAction extends Action {
	
	private String fMode;
	private LaunchShortcutExtension fShortcut; 


	/**
	 * Constructor for LaunchShortcutAction.
	 */
	public LaunchShortcutAction(String mode, LaunchShortcutExtension shortcut) {
		super(shortcut.getLabel(), shortcut.getImageDescriptor());
		fMode = mode;
		fShortcut = shortcut;
		updateEnablement();
	}
	
	/**
	 * Runs with either the active editor or workbench selection.
	 * 
	 * @see IAction#run()
	 */
	public void run() {
		IStructuredSelection ss = SelectedResourceManager.getDefault().getCurrentSelection();
		Object o = ss.getFirstElement();
		if(o instanceof IEditorPart) {
			fShortcut.launch((IEditorPart) o, fMode);
		}
		else {
			fShortcut.launch(ss, fMode);
		}
	}
	
	/**
	 * Since these actions are re-created each time the run/debug as menu is
	 * filled, the enablement of this action is static.
	 */
	private void updateEnablement() {
		boolean enabled = false;
		IStructuredSelection ss = SelectedResourceManager.getDefault().getCurrentSelection();
		Object o = ss.getFirstElement();
		if(o instanceof IEditorPart) {
			enabled = true;
		}
		else {
			try {
				// check enablement logic, if any
				Expression expression = fShortcut.getShortcutEnablementExpression();
				if (expression == null) {
					enabled = !ss.isEmpty();
				} else {
					List list = ss.toList();
					IEvaluationContext context = new EvaluationContext(null, list);
					context.addVariable("selection", list); //$NON-NLS-1$
					enabled = fShortcut.evalEnablementExpression(context, expression);
				}
			} catch (CoreException e) {}
		}
		setEnabled(enabled);
	}

}
