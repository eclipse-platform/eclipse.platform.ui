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

package org.eclipse.ui.commands;

public final class ActionServiceEvent {

	private IActionService actionService;

	public ActionServiceEvent(IActionService actionService) {
		if (actionService == null)
			throw new NullPointerException();

		this.actionService = actionService;
	}

	public IActionService getActionService() {
		return actionService;
	}
}
