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

package org.eclipse.jface.action;

public class ContextResolver {

	private static ContextResolver instance;

	public static ContextResolver getInstance() {
		if (instance == null)
			instance = new ContextResolver();
			
		return instance;	
	}

	private IContextResolver contextResolver;

	private ContextResolver() {
		super();
	}
	
	public IContextResolver getContextResolver() {
		return contextResolver;
	}
	
	public void setContextResolver(IContextResolver contextResolver) {
		this.contextResolver = contextResolver;
	}
}
