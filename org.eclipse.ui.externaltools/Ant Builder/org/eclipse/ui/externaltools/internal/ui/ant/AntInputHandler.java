package org.eclipse.ui.externaltools.internal.ui.ant;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.input.DefaultInputHandler;
import org.apache.tools.ant.input.InputRequest;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.externaltools.internal.core.ExternalToolsPlugin;

/**
 * The default input handler when using Ant within Eclipse.
 * This is the class that will responde to <input> requests from
 * within an Ant build file.
 */
public class AntInputHandler extends DefaultInputHandler {

	/**
	 * @see org.apache.tools.ant.input.InputHandler#handleInput(org.apache.tools.ant.input.InputRequest)
	 */
	public void handleInput(final InputRequest request) throws BuildException {
		String prompt = getPrompt(request);
       	String title= "Ant Input Request";
		IInputValidator validator= new IInputValidator() {
			int hitCount= -1;
			public String isValid(String value) {
				request.setInput(value);
				if (request.isInputValid()) {
					return null;
				} else {
					return "Invalid input";
				}
			}
		};

		IWorkbenchWindow window= ExternalToolsPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow();
		if (window == null) {
			throw new BuildException("Unable able to respond to <input> request");
		}
		
		Shell activeShell= window.getShell();
		InputDialog dialog= new InputDialog(activeShell, title, prompt, "", validator);
		if (dialog.open() != InputDialog.OK) {
			throw new BuildException("Unable able to respond to <input> request");
		}
	}
}
