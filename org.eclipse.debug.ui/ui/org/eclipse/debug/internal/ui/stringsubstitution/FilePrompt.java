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

import org.eclipse.swt.widgets.FileDialog;

/**
 * Prompts the user to choose a file and expands the selection
 */
public class FilePrompt extends PromptingResolver {

	/**
	 * Prompts the user to choose a file
	 * @see PromptExpanderBase#prompt()
	 */
	public void prompt() {
		FileDialog dialog = new FileDialog(getShell());
		dialog.setText(dialogMessage);
		dialog.setFileName(lastValue == null ? defaultValue : lastValue);
		dialogResultString = dialog.open();
	}

}
