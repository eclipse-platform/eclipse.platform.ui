package org.eclipse.ant.tests.core.support.inputHandlers;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.input.DefaultInputHandler;
import org.apache.tools.ant.input.InputRequest;

/**
 * A test input handler when using Ant within Eclipse.
 * This is the class that will respond to <input> requests from
 * within an Ant build file.
 */
public class AntTestInputHandler extends DefaultInputHandler {
	
	/**
	 * @see org.apache.tools.ant.input.InputHandler#handleInput(org.apache.tools.ant.input.InputRequest)
	 */
	public void handleInput(InputRequest request) throws BuildException {
		request.setInput("testing handling input requests");
	}
}
