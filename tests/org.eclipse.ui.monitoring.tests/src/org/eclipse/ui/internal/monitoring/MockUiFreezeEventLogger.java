/*******************************************************************************
 * Copyright (C) 2014, Google Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marcus Eng (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.monitoring;

import org.eclipse.ui.monitoring.IUiFreezeEventLogger;
import org.eclipse.ui.monitoring.UiFreezeEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Captures and stores {@link UiFreezeEvent}s during JUnit tests.
 */
public class MockUiFreezeEventLogger implements IUiFreezeEventLogger {
	private static final List<UiFreezeEvent> loggedEvents =
			Collections.synchronizedList(new ArrayList<UiFreezeEvent>());

	@Override
	public void log(UiFreezeEvent event) {
		loggedEvents.add(event);
	}

	public List<UiFreezeEvent> getLoggedEvents() {
		return loggedEvents;
	}
}
