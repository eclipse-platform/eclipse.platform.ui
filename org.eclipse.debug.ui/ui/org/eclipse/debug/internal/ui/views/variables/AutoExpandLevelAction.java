/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.variables;

import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.PlatformUI;

/**
 * Action used to set auto expand level in the variables view.
 * 
 * @since 3.4
 */
public class AutoExpandLevelAction extends Action {

		private VariablesView fView;
		
		class NumberValidator implements IInputValidator {

			public String isValid(String input) {
				try {
					int i= Integer.parseInt(input);
					if (i < 0)
						return VariablesViewMessages.AutoExpandLevelAction_0; 

				} catch (NumberFormatException x) {
					return VariablesViewMessages.AutoExpandLevelAction_1; 
				}

				return null;
			}
		}
		
		AutoExpandLevelAction(VariablesView view) {
			fView = view;
			setText(VariablesViewMessages.AutoExpandLevelAction_2);
			PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IDebugHelpContextIds.VARIABLES_AUTO_EXPAND);
		}

		public void run() {
			InputDialog dialog = new InputDialog(fView.getSite().getShell(),
					VariablesViewMessages.AutoExpandLevelAction_3, VariablesViewMessages.AutoExpandLevelAction_4,
					Integer.toString(fView.getAutoExpandLevel()),
					new NumberValidator());
			int open = dialog.open();
			if (open == Window.OK) {
				fView.setAutoExpandLevel(Integer.parseInt(dialog.getValue()));
			}
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.action.Action#runWithEvent(org.eclipse.swt.widgets.Event)
		 */
		public void runWithEvent(Event event) {
			run();
		}
		
		
}
