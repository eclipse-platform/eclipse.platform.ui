/*******************************************************************************
 * Copyright (c) 2000, 2003 Matt Conway and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Matt Conway - initial implementation
 *     IBM Corporation - integration and code cleanup
 *******************************************************************************/
package org.eclipse.debug.ui.launchVariables;

import java.text.MessageFormat;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationsMessages;

/**
 * Base implementation for variable expanders that prompt the user
 * for their value.
 */
abstract class PromptExpanderBase extends DefaultVariableExpander {

	/**
	 * A hint that helps the user choose their input. If a prompt
	 * hint is provider the user will be prompted:
	 * 	Please input a value for <code>promptHint</code>
	 */
	protected String promptHint = null;
	/**
	 * The prompt displayed to the user.
	 */
	protected String dialogMessage = null;
	/**
	 * The default value selected when the prompt is displayed
	 */
	protected String defaultValue = null;
	/**
	 * The last value chosen by the user for this variable 
	 */
	protected String lastValue = null;
	/**
	 * The result returned from the prompt dialog
	 */
	protected String dialogResultString = null;
	
	/**
	 * Prompts the user for input and returns a string representation of
	 * the user's selection.
	 */
	public String getText(String varTag, String varValue, ExpandVariableContext context) throws CoreException {
		String varText = null;
		setupDialog(varValue);

		DebugUIPlugin.getStandardDisplay().syncExec(new Runnable() {
			public void run() {
				prompt();
			}
		});
		if (dialogResultString != null) {
			varText = dialogResultString;
			lastValue = dialogResultString;
		}
		return varText;
	}
	
	/**
	 * Presents the user with the appropriate prompt for the variable to be expanded
	 * and sets the <code>dialogResultString</code> based on the user's selection.
	 */
	public abstract void prompt();

	/**
	 * Initializes values displayed when the user is prompted. If
	 * a prompt hint and default value are supplied in the given
	 * variable value, these are extracted for presentation
	 * 
	 * @param varValue the value of the variable from which the prompt
	 * hint and default value will be extracted
	 */
	protected void setupDialog(String varValue) {
		promptHint = null;
		defaultValue = null;
		dialogResultString = null;
		if (varValue != null) {
			int idx = varValue.indexOf(':');
			if (idx != -1) {
				promptHint = varValue.substring(0, idx);
				defaultValue = varValue.substring(idx + 1);
			} else {
				promptHint = varValue;
			}
		}

		if (promptHint != null) {
			dialogMessage = MessageFormat.format(LaunchConfigurationsMessages.getString("PromptExpanderBase.Please_input_a_value_for_{0}_1"), new String[] {promptHint}); //$NON-NLS-1$
		} else {
			dialogMessage = LaunchConfigurationsMessages.getString("PromptExpanderBase.Please_input_a_value_2"); //$NON-NLS-1$
		}
	}
}
