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

import org.eclipse.ui.commands.IKeyConfiguration;
import org.eclipse.ui.commands.IKeyConfigurationHandle;
import org.eclipse.ui.handles.NotDefinedException;
import org.eclipse.ui.internal.handles.Handle;

final class KeyConfigurationHandle extends Handle implements IKeyConfigurationHandle {

	KeyConfigurationHandle(String id) {
		super(id);
	}
	
	public IKeyConfiguration getKeyConfiguration()
		throws NotDefinedException {
		return (IKeyConfiguration) getObject();
	}

	public void define(Object object) {
		if (object == null)
			throw new NullPointerException();
		else if (!(object instanceof IKeyConfiguration))
			throw new IllegalArgumentException();
			
		super.define(object);
	}	
}
