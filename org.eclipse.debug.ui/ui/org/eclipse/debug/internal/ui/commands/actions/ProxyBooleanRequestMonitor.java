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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.debug.ui.commands.IBooleanStatusMonitor;

/**
 * Boolean request monitor that collects boolean results from a number of voters.
 * Request is cancelled when one voter votes false. Forwards the results to
 * any number of boolean request monitors.
 * 
 * @since 3.3
 *
 */
public class ProxyBooleanRequestMonitor extends AbstractRequestMonitor implements IBooleanStatusMonitor {
	
	private List fMonitors = new ArrayList();
	private int fNumVoters;
	private int fNumOfVotes = 0;
	private boolean fDone = false;
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.actions.provisional.IBooleanRequestMonitor#setResult(boolean)
	 */
	public synchronized void setResult(boolean result) {
		fNumOfVotes++;
		if (!isCanceled()) {
			if (!result) {
				setCanceled(true);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IProgressMonitor#done()
	 */
	public synchronized void done() {
		if (!fDone) {
			if (isCanceled() || fNumOfVotes == fNumVoters) {
				fDone = true;
				Iterator monitors = fMonitors.iterator();
				while (monitors.hasNext()) {
					IBooleanStatusMonitor monitor = (IBooleanStatusMonitor) monitors.next();
					monitor.setStatus(getStatus());
					if (isCanceled()) {
						monitor.setCanceled(true);
					} else {
						monitor.setResult(true);
					}
					monitor.done();
				}
				fMonitors.clear();			
			}
		}
	}
	
	/**
	 * Sets the number of voters required for this capability test.
	 * 
	 * @param numVoters
	 */
	void setNumVoters(int numVoters) {
		fNumVoters = numVoters;
	}

	/**
	 * Adds the given monitor to forward the result to when done.
	 * 
	 * @param monitor
	 */
	void addMonitor(IBooleanStatusMonitor monitor) {
		fMonitors.add(monitor);
	}
}
