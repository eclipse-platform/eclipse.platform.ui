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

import org.eclipse.ui.roles.IRole;
import org.eclipse.ui.roles.IRoleEvent;

final class RoleEvent implements IRoleEvent {

	private IRole role;

	RoleEvent(IRole role) {
		if (role == null)
			throw new NullPointerException();
		
		this.role = role;
	}

	public IRole getRole() {
		return role;
	}
}
