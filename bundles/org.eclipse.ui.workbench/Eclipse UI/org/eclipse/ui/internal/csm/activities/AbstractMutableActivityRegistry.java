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

package org.eclipse.ui.internal.csm.activities;

import java.util.List;

import org.eclipse.ui.internal.util.Util;

abstract class AbstractMutableActivityRegistry extends AbstractActivityRegistry implements IMutableActivityRegistry {

	protected AbstractMutableActivityRegistry() {
	}

	public void setActivityDefinitions(List activityDefinitions) {
		activityDefinitions = Util.safeCopy(activityDefinitions, IActivityDefinition.class);	
		
		if (!activityDefinitions.equals(this.activityDefinitions)) {
			this.activityDefinitions = activityDefinitions;			
			fireActivityRegistryChanged();
		}
	}
}
