/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.contexts;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.ui.contexts.ContextActivationServiceEvent;
import org.eclipse.ui.contexts.IMutableContextActivationService;
import org.eclipse.ui.internal.util.Util;

public final class MutableContextActivationService
	extends AbstractContextActivationService
	implements IMutableContextActivationService {
	private Set activeContextIds = new HashSet();

	public MutableContextActivationService() {
	}

	public Set getActiveContextIds() {
		return Collections.unmodifiableSet(activeContextIds);
	}

	public void setActiveContextIds(Set activeContextIds) {
		activeContextIds = Util.safeCopy(activeContextIds, String.class);
		boolean contextActivationServiceChanged = false;
		Map contextEventsByContextId = null;

		if (!this.activeContextIds.equals(activeContextIds)) {
			this.activeContextIds = activeContextIds;
			fireContextActivationServiceChanged(
				new ContextActivationServiceEvent(this, true));
		}
	}
}
