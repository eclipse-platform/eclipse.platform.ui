/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.runtime;

import java.util.LinkedList;
import java.util.List;
import org.eclipse.core.runtime.*;

/**
 * Allows test cases to wait for event notification so they can make assertions on the event.  
 */
public class TestRegistryChangeListener implements IRegistryChangeListener {
	private List events = new LinkedList();;
	private String xpNamespace;
	private String xpId;
	private String extNamespace;
	private String extId;

	/**
	 * Creates a new listener. The parameters allow filtering events based on extension point/extension's 
	 * namespaces/ids.	 * 
	 */
	public TestRegistryChangeListener(String xpNamespace, String xpId, String extNamespace, String extId) {
		if (xpId != null && xpNamespace == null)
			throw new IllegalArgumentException();
		if (extId != null && extNamespace == null)
			throw new IllegalArgumentException();
		if (xpId == null && extId != null)
			throw new IllegalArgumentException();
		this.xpNamespace = xpNamespace;
		this.xpId = xpId;
		this.extNamespace = extNamespace;
		this.extId = extId;		
	}

	/**
	 * @see IRegistryChangeListener#registryChanged
	 */
	public synchronized void registryChanged(IRegistryChangeEvent newEvent) {
		if (xpId != null) {
			if (extId != null) {
				if (newEvent.getExtensionDelta(xpNamespace, xpId, extNamespace + '.' + extId) == null)
					return;
			} else if (newEvent.getExtensionDeltas(xpNamespace, xpId).length == 0)
				return;
		}
		this.events.add(newEvent);
		notify();
	}

	/**
	 * Returns the first event that is received, blocking for at most <code>timeout</code> milliseconds.
	 * Returns <code>null</code> if a event was not received for the time allowed.
	 * 
	 * @param timeout the maximum time to wait in milliseconds. If zero, this method will 
	 * block until an event is received 
	 * @return the first event received, or <code>null</code> if none was received
	 */
	public synchronized IRegistryChangeEvent getEvent(long timeout) {
		if (!events.isEmpty())
			return (IRegistryChangeEvent) events.remove(0);
		try {
			wait(timeout);
		} catch (InterruptedException e) {
			// who cares?
		}
		return events.isEmpty() ? null : (IRegistryChangeEvent) events.remove(0);
	}

	public void register() {
		Platform.getExtensionRegistry().addRegistryChangeListener(this, xpNamespace);
	}

	public void unregister() {
		Platform.getExtensionRegistry().removeRegistryChangeListener(this);
	}

}