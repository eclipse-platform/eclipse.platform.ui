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

package org.eclipse.ui.internal.contexts.activationservice;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.ui.contexts.activationservice.ContextActivationServiceEvent;
import org.eclipse.ui.contexts.activationservice.ICompoundContextActiviationService;
import org.eclipse.ui.contexts.activationservice.IContextActivationService;
import org.eclipse.ui.contexts.activationservice.IContextActivationServiceListener;
import org.eclipse.ui.contexts.activationservice.IMutableContextActivationService;
import org.eclipse.ui.internal.util.Util;

public final class CompoundContextActivationService
	extends AbstractContextActivationService
	implements ICompoundContextActiviationService {
	private Set activeContextIds = new HashSet();

	private final IContextActivationServiceListener contextActivationServiceListener =
		new IContextActivationServiceListener() {
		public void contextActivationServiceChanged(ContextActivationServiceEvent contextActivationServiceEvent) {
			Set activeContextIds = new HashSet();

			for (Iterator iterator = contextActivationServices.iterator();
				iterator.hasNext();
				) {
				IMutableContextActivationService mutableContextActivationService =
					(IMutableContextActivationService) iterator.next();
				activeContextIds.addAll(
					mutableContextActivationService.getActiveContextIds());
			}

			setActiveContextIds(activeContextIds);
		}
	};
	private final HashSet contextActivationServices = new HashSet();

	public CompoundContextActivationService() {
	}

	public void addContextActivationService(IContextActivationService contextActivationService) {
		if (contextActivationService == null)
			throw new NullPointerException();

		contextActivationService.addContextActivationServiceListener(contextActivationServiceListener);
		contextActivationServices.add(contextActivationService);
	}

	public Set getActiveContextIds() {
		return Collections.unmodifiableSet(activeContextIds);
	}

	public void removeContextActivationService(IContextActivationService contextActivationService) {
		if (contextActivationService == null)
			throw new NullPointerException();

		contextActivationServices.remove(contextActivationService);
		contextActivationService.removeContextActivationServiceListener(contextActivationServiceListener);
	}

	private void setActiveContextIds(Set activeContextIds) {
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