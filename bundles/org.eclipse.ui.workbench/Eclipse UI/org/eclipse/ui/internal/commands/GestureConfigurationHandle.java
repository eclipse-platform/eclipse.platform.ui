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

package org.eclipse.ui.internal.commands;

import org.eclipse.ui.commands.IGestureConfiguration;
import org.eclipse.ui.commands.IGestureConfigurationHandle;
import org.eclipse.ui.handles.NotDefinedException;
import org.eclipse.ui.internal.handles.Handle;

final class GestureConfigurationHandle extends Handle implements IGestureConfigurationHandle {

	GestureConfigurationHandle(String id) {
		super(id);
	}
	
	public IGestureConfiguration getGestureConfiguration()
		throws NotDefinedException {
		return (IGestureConfiguration) getObject();
	}

	public void define(Object object) {
		if (object == null)
			throw new NullPointerException();
		else if (!(object instanceof IGestureConfiguration))
			throw new IllegalArgumentException();
			
		super.define(object);
	}	
}
