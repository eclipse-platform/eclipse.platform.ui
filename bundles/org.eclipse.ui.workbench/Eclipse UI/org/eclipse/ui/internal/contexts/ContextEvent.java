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

import org.eclipse.ui.contexts.IContext;
import org.eclipse.ui.contexts.IContextEvent;
import org.eclipse.ui.internal.util.Util;

public final class ContextEvent implements IContextEvent {

	private IContext context;

	public ContextEvent(IContext context)
		throws IllegalArgumentException {		
		super();
		
		if (context == null)
			throw new IllegalArgumentException();
		
		this.context = context;
	}

	public boolean equals(Object object) {
		if (!(object instanceof ContextEvent))
			return false;

		ContextEvent contextEvent = (ContextEvent) object;	
		return Util.equals(context, contextEvent.context);
	}
	
	public IContext getContext() {
		return context;
	}
}
