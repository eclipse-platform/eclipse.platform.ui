/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.commands.actions;

import org.eclipse.debug.core.commands.IBooleanCollector;
import org.eclipse.debug.internal.core.commands.StatusCollector;
import org.eclipse.jface.action.IAction;

/**
 * Boolean collector that collects boolean results from a number of voters.
 * Request is cancelled when one voter votes false.
 * 
 * @since 3.3
 *
 */
public class BooleanCollector extends StatusCollector implements IBooleanCollector {
	
	private IAction fAction;
	private int fNumVoters;
	private int fNumOfVotes = 0;
	private boolean fEnabled = true;
	
	public BooleanCollector(IAction action, int numVoters) {
		fAction = action;
		fNumVoters = numVoters;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.actions.provisional.IBooleanRequestMonitor#setResult(boolean)
	 */
	public void setResult(boolean result) {
		fNumOfVotes++;
		if (fEnabled) {
			fEnabled = result;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IProgressMonitor#done()
	 */
	public void done() {
		if (!fEnabled) {
			fAction.setEnabled(false);
		} else {
			fAction.setEnabled(fNumOfVotes == fNumVoters);
		} 
	}

}
