/*******************************************************************************
 * Copyright (c) 2006, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.commands.actions;

import org.eclipse.debug.core.commands.IEnabledStateRequest;
import org.eclipse.debug.internal.core.commands.DebugCommandRequest;

/**
 * Boolean collector that collects boolean results from a number of voters.
 * Request is cancelled when one voter votes false.
 * 
 * @since 3.3
 *
 */
public class UpdateActionsRequest extends DebugCommandRequest implements IEnabledStateRequest {
	
	private IEnabledTarget[] fActions;
	private boolean fEnabled = false;
	
	public UpdateActionsRequest(Object[] elements, IEnabledTarget[] actions) {
		super(elements);
		fActions = actions;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.actions.provisional.IBooleanRequestMonitor#setResult(boolean)
	 */
	public synchronized void setEnabled(boolean result) {
		fEnabled = result;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IProgressMonitor#done()
	 */
	public synchronized void done() {
		if (!isCanceled()) {
			for (int i = 0; i < fActions.length; i++) {
				fActions[i].setEnabled(fEnabled);
			}
		}
	}

}
