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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ui.contexts.ContextActivationServiceEvent;
import org.eclipse.ui.contexts.IContextActivationService;
import org.eclipse.ui.contexts.IContextActivationServiceListener;

public abstract class AbstractContextActivationService
	implements IContextActivationService {
	private List contextActivationServiceListeners;

	protected AbstractContextActivationService() {
	}

	public void addContextActivationServiceListener(IContextActivationServiceListener contextActivationServiceListener) {
		if (contextActivationServiceListener == null)
			throw new NullPointerException();

		if (contextActivationServiceListeners == null)
			contextActivationServiceListeners = new ArrayList();

		if (!contextActivationServiceListeners.contains(contextActivationServiceListener))
			contextActivationServiceListeners.add(contextActivationServiceListener);
	}

	protected void fireContextActivationServiceChanged(ContextActivationServiceEvent contextActivationServiceEvent) {
		if (contextActivationServiceEvent == null)
			throw new NullPointerException();

		if (contextActivationServiceListeners != null)
			for (int i = 0; i < contextActivationServiceListeners.size(); i++)
				(
					(
						IContextActivationServiceListener) contextActivationServiceListeners
							.get(
						i)).contextActivationServiceChanged(
					contextActivationServiceEvent);
	}

	public void removeContextActivationServiceListener(IContextActivationServiceListener contextActivationServiceListener) {
		if (contextActivationServiceListener == null)
			throw new NullPointerException();

		if (contextActivationServiceListeners != null)
			contextActivationServiceListeners.remove(contextActivationServiceListener);
	}
}
