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

import org.eclipse.ui.contexts.IContextDefinition;
import org.eclipse.ui.contexts.IContextDefinitionHandle;
import org.eclipse.ui.handles.NotDefinedException;
import org.eclipse.ui.internal.handles.Handle;

final class ContextDefinitionHandle extends Handle implements IContextDefinitionHandle {

	ContextDefinitionHandle(String id) {
		super(id);
	}
	
	public IContextDefinition getContextDefinition()
		throws NotDefinedException {
		return (IContextDefinition) getObject();
	}
		
	public void define(Object object) {
		if (object == null)
			throw new NullPointerException();
		else if (!(object instanceof IContextDefinition))
			throw new IllegalArgumentException();
			
		super.define(object);
	}
}
