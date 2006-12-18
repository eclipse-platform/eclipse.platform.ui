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

import org.eclipse.debug.core.commands.IBooleanCollector;
import org.eclipse.debug.internal.core.commands.StatusCollector;

/**
 * Boolean collector that collects boolean results from a number of voters.
 * Request is cancelled when one voter votes false. Forwards the results to
 * any number of boolean collectors.
 * 
 * @since 3.3
 *
 */
public class ProxyBooleanCollector extends StatusCollector implements IBooleanCollector {
	
	private List fMonitors = new ArrayList();
	private int fNumVoters;
	private int fNumOfVotes = 0;
	private boolean fDone = false;
	private boolean fEnabled = true;
	
	public synchronized void setResult(boolean result) {
		fNumOfVotes++;
		if (fEnabled) {
			fEnabled = result;
		}
	}

	public synchronized void done() {
		if (!fDone) {
			if (!fEnabled || fNumOfVotes == fNumVoters) {
				fDone = true;
				Iterator monitors = fMonitors.iterator();
				while (monitors.hasNext()) {
					IBooleanCollector monitor = (IBooleanCollector) monitors.next();
					monitor.setStatus(getStatus());
					monitor.setResult(fEnabled);
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
	void addMonitor(IBooleanCollector monitor) {
		fMonitors.add(monitor);
	}
	
	/**
	 * Returns whether this command is still enabled.
	 * 
	 * @return whether enabled
	 */
	synchronized boolean isEnabled() {
		return fEnabled;
	}
}
