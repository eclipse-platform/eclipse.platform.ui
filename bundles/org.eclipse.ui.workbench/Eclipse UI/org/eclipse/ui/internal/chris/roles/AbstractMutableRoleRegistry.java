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

import java.util.List;

import org.eclipse.ui.internal.roles.api.IMutableRoleRegistry;
import org.eclipse.ui.internal.roles.api.IRoleDefinition;
import org.eclipse.ui.internal.util.Util;

abstract class AbstractMutableRoleRegistry extends AbstractRoleRegistry implements IMutableRoleRegistry {

	protected AbstractMutableRoleRegistry() {
	}

	public void setRoleDefinitions(List roleDefinitions) {
		roleDefinitions = Util.safeCopy(roleDefinitions, IRoleDefinition.class);	
		
		if (!roleDefinitions.equals(this.roleDefinitions)) {
			this.roleDefinitions = roleDefinitions;			
			fireRoleRegistryChanged();
		}
	}
}
