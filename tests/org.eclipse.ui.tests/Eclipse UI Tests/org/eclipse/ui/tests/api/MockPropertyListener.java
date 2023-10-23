/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
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
package org.eclipse.ui.tests.api;

import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.tests.harness.util.CallHistory;

public class MockPropertyListener implements IPropertyListener {
	private final CallHistory callTrace;

	private final Object sourceMask;

	private final int sourceId;

	/**
	 * @param source the event source that fires the event to this listener
	 * @param id the property id for the event
	 */
	public MockPropertyListener(Object source, int id) {
		sourceMask = source;
		sourceId = id;
		callTrace = new CallHistory(this);
	}

	/**
	 * @see IPropertyListener#propertyChanged(Object, int)
	 */
	@Override
	public void propertyChanged(Object source, int propId) {
		if (source == sourceMask && propId == sourceId) {
			callTrace.add("propertyChanged");
		}
	}

	public CallHistory getCallHistory() {
		return callTrace;
	}
}

