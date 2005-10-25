/*******************************************************************************
 * Copyright (c) 2000, 2005 Matt Conway and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
		InputDialog dialog = new InputDialog(null, StringSubstitutionMessages.StringPromptExpander_0, dialogMessage, defaultValue == null ? lastValue : defaultValue, null); 
		int dialogResult = dialog.open();
		if (dialogResult == Window.OK) {
			dialogResultString = dialog.getValue();
		}
	}

}
