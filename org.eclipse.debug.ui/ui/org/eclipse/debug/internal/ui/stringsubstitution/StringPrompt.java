/*******************************************************************************
 * Copyright (c) 2000, 2004 Matt Conway and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Matt Conway - initial implementation
 *     IBM Corporation - integration and code cleanup
 *******************************************************************************/
package org.eclipse.debug.internal.ui.stringsubstitution;

import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;

/**
 * Prompts the user to input a string and expands to the value entered
 */
public class StringPrompt extends PromptingResolver {
	
	/**
	 * Prompts the user to input a string.
	 * @see PromptExpanderBase#prompt()
	 */
	public void prompt() {
		InputDialog dialog = new InputDialog(null, StringSubstitutionMessages.getString("StringPromptExpander.0"), dialogMessage, lastValue == null ? defaultValue : lastValue, null); //$NON-NLS-1$
		int dialogResult = dialog.open();
		if (dialogResult == Window.OK) {
			dialogResultString = dialog.getValue();
		}
	}

}
