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

package org.eclipse.ui.internal.chris.roles;

import org.eclipse.ui.roles.IRoleActivationService;
import org.eclipse.ui.roles.IRoleActivationServiceEvent;

final class RoleActivationServiceEvent implements IRoleActivationServiceEvent {

	private IRoleActivationService roleActivationService;

	RoleActivationServiceEvent(IRoleActivationService roleActivationService) {
		if (roleActivationService == null)
			throw new NullPointerException();
		
		this.roleActivationService = roleActivationService;
	}

	public IRoleActivationService getRoleActivationService() {
		return roleActivationService;
	}
}
