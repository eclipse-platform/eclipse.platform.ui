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
package org.eclipse.ui.externaltools.internal.variable;

import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;

/**
 * Prompts the user to choose a folder and expands the selection
 */
public class FolderPromptExpander extends PromptExpanderBase {

	public FolderPromptExpander() {
		super();
	}
	
	/**
	 * Prompts the user to choose a folder.
	 * @see PromptExpanderBase#prompt()
	 */
	public void prompt() {
		DirectoryDialog dialog = new DirectoryDialog(Display.getDefault().getActiveShell());
		dialog.setText(dialogMessage);
		dialog.setFilterPath(lastValue == null ? defaultValue : lastValue);
		dialogResultString = dialog.open();
	}

}
