/*******************************************************************************
 * Copyright (c) 2008, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.examples.ui.midi.adapters;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.commands.IDebugCommandRequest;
import org.eclipse.debug.core.commands.IEnabledStateRequest;
import org.eclipse.debug.core.commands.IStepOverHandler;

/**
 * Provides an example command handler for the step over action.
 * On execute, it simply returns a status that is opened in an
 * error dialog.
 */
public class MidiStepOverHandler implements IStepOverHandler {

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.commands.IDebugCommandHandler#canExecute(org.eclipse.debug.core.commands.IEnabledStateRequest)
	 */
	@Override
	public void canExecute(IEnabledStateRequest request) {
		// We could create a job here, schedule it, then return to be asynchronous
		request.setEnabled(request.getElements().length > 0);
		request.done();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.commands.IDebugCommandHandler#execute(org.eclipse.debug.core.commands.IDebugCommandRequest)
	 */
	@Override
	public boolean execute(IDebugCommandRequest request) {
		// We could create a job to do this work, schedule it, then return to be asynchronous
		// If running asynchronously, remember to return the enablement you want the action to have while this action is run
		request.setStatus(new Status(IStatus.WARNING, "org.eclipse.debug.examples.ui", "This is an example command handler overriding the default using an adapter on " + request.getElements()[0].getClass().getName())); //$NON-NLS-1$ //$NON-NLS-2$
		request.done();
		return true;
	}

}
