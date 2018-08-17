/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
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
 *     Alexander Kurtakov <akurtako@redhat.com> - bug 458490
 *******************************************************************************/
package org.eclipse.core.tests.internal.registry.simple.utils;

import java.util.LinkedList;
import java.util.List;
import org.eclipse.core.runtime.*;

/**
 * Allows test cases to wait for event notification so they can make assertions on the event.
 * Similar to org.eclipse.core.tests.harness.TestRegistryChangeListener.
 * @since 3.2
 */
public class SimpleRegistryListener implements IRegistryChangeListener {

	private List<IRegistryChangeEvent> events = new LinkedList<>();

	@Override
	public synchronized void registryChanged(IRegistryChangeEvent newEvent) {
		events.add(newEvent);
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
			return events.remove(0);
		try {
			wait(timeout);
		} catch (InterruptedException e) {
			// nothing to do
		}
		return events.isEmpty() ? null : events.remove(0);
	}

	public void register(IExtensionRegistry registry) {
		registry.addRegistryChangeListener(this);
	}

	public void unregister(IExtensionRegistry registry) {
		registry.removeRegistryChangeListener(this);
	}
}
