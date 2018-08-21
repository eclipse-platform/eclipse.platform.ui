/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ant.tests.core.support.inputHandlers;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.input.DefaultInputHandler;
import org.apache.tools.ant.input.InputRequest;

/**
 * A test input handler when using Ant within Eclipse. This is the class that will respond to <input> requests from within an Ant build file. If the
 * build is occurring in Ant 1.6.0 and the -noinput option has been specified this input handler will fail.
 */
public class AntTestInputHandler extends DefaultInputHandler {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.tools.ant.input.InputHandler#handleInput(org.apache.tools.ant.input.InputRequest)
	 */
	@Override
	public void handleInput(InputRequest request) throws BuildException {
		if (System.getProperty("eclipse.ant.noInput") != null) { //$NON-NLS-1$
			throw new BuildException("Unable to respond to input request likely as a result of specifying the -noinput command"); //$NON-NLS-1$
		}
		request.setInput("testing handling input requests"); //$NON-NLS-1$
	}
}
