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

package org.eclipse.ui.views.internal.markers.tasks;

import org.eclipse.ui.views.internal.markers.MarkerRegistry;


class TaskRegistry extends MarkerRegistry {
	
	private static TaskRegistry instance = null;
	
	public static TaskRegistry getInstance() {
		if (instance == null) {
			instance = new TaskRegistry();
		}
		return instance;
	}
	
	private TaskRegistry() {
		super();
	}

}
