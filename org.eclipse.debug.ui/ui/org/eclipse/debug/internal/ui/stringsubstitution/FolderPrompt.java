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

import org.eclipse.swt.widgets.DirectoryDialog;

/**
 * Prompts the user to choose a folder and expands the selection
 */
public class FolderPrompt extends PromptingResolver {

	/**
	 * Prompts the user to choose a folder.
	 * @see PromptExpanderBase#prompt()
	 */
	public void prompt() {
		DirectoryDialog dialog = new DirectoryDialog(getShell());
		dialog.setText(dialogMessage);
		dialog.setFilterPath(lastValue == null ? defaultValue : lastValue);
		dialogResultString = dialog.open();
	}

}
