/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
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

package org.eclipse.ant.internal.core.ant;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.input.InputHandler;
import org.apache.tools.ant.input.InputRequest;

/**
 * A input handler that does nothing with input requests Used to ensure we do not block while retrieving targets from an Ant buildfile that has an
 * input task in the top level implicit target
 */
public class NullInputHandler implements InputHandler {

	public NullInputHandler() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.tools.ant.input.InputHandler#handleInput(org.apache.tools.ant.input.InputRequest)
	 */
	@Override
	public void handleInput(InputRequest request) throws BuildException {
		// do nothing
	}
}
