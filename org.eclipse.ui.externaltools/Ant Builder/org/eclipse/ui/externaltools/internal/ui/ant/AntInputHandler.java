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
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.externaltools.internal.core.ToolMessages;

/**
 * The default input handler when using Ant within Eclipse.
 * This is the class that will respond to <input> requests from
 * within an Ant build file.
 */
public class AntInputHandler extends DefaultInputHandler {
	
	/**
	 * @see org.apache.tools.ant.input.InputHandler#handleInput(org.apache.tools.ant.input.InputRequest)
	 */
	public void handleInput(InputRequest request) throws BuildException {
		BuildException[] problem= new BuildException[1];
		Runnable runnable= getHandleInputRunnable(request, problem);
		Display.getDefault().syncExec(runnable);
		if (problem[0] != null) {
			throw problem[0];
		}
	}
	
	protected Runnable getHandleInputRunnable(final InputRequest request, final BuildException[] problem) {
		return new Runnable() {
			public void run() {
				String prompt = getPrompt(request);
		       	String title= ToolMessages.getString("AntInputHandler.Ant_Input_Request_1"); //$NON-NLS-1$
				IInputValidator validator= new IInputValidator() {
					int hitCount= -1;
					public String isValid(String value) {
						request.setInput(value);
						if (request.isInputValid()) {
							return null;
						} else {
							return ToolMessages.getString("AntInputHandler.Invalid_input_2"); //$NON-NLS-1$
						}
					}
				};
		
				InputDialog dialog= new InputDialog(null, title, prompt, "", validator); //$NON-NLS-1$
				if (dialog.open() != InputDialog.OK) {
					problem[0]= new BuildException(ToolMessages.getString("AntInputHandler.Unable_able_to_respond_to_<input>_request_4")); //$NON-NLS-1$
				}
			}
		};
	}
}
