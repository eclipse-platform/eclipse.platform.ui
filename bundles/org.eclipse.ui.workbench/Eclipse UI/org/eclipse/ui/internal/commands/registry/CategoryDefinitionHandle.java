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

package org.eclipse.ui.internal.commands.registry;

import org.eclipse.ui.commands.ICategoryDefinition;
import org.eclipse.ui.commands.ICategoryDefinitionHandle;
import org.eclipse.ui.handles.NotDefinedException;
import org.eclipse.ui.internal.handles.Handle;

final class CategoryDefinitionHandle extends Handle implements ICategoryDefinitionHandle {

	CategoryDefinitionHandle(String id) {
		super(id);
	}
	
	public ICategoryDefinition getCategoryDefinition()
		throws NotDefinedException {
		return (ICategoryDefinition) getObject();
	}

	public void define(Object object) {
		if (object == null)
			throw new NullPointerException();
		else if (!(object instanceof ICategoryDefinition))
			throw new IllegalArgumentException();
			
		super.define(object);
	}	
}
