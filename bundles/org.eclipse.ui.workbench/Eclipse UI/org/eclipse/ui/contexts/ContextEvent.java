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

package org.eclipse.ui.contexts;

import org.eclipse.ui.internal.commands.util.Util;

/**
 * <p>
 * TODO javadoc
 * </p>
 * <p>
 * This class is not intended to be extended by clients.
 * </p>
 * <p>
 * <em>EXPERIMENTAL</em>
 * </p>
 * 
 * @since 3.0
 */
public class ContextEvent {

	private IContext context;

	/**
	 * TODO javadoc
	 * 
	 * @param context
	 * @throws IllegalArgumentException
	 */	
	public ContextEvent(IContext context)
		throws IllegalArgumentException {		
		super();
		
		if (context == null)
			throw new IllegalArgumentException();
		
		this.context = context;
	}

	/**
	 * TODO javadoc
	 * 
	 * @param object
	 */		
	public boolean equals(Object object) {
		if (!(object instanceof ContextEvent))
			return false;

		ContextEvent contextEvent = (ContextEvent) object;	
		return Util.equals(context, contextEvent.context);
	}
	
	/**
	 * TODO javadoc
	 * 
	 * @return
	 */		
	public IContext getContext() {
		return context;
	}
}
