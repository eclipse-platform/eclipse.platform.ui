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
package org.eclipse.jface.text.templates;

import java.util.HashMap;
import java.util.Map;

/**
 * A singleton to keep track of all known context types.
 * 
 * @since 3.0
 */
public class ContextTypeRegistry {

	/** all known context types */
	private final Map fContextTypes= new HashMap();
	
	/**
	 * Adds a context type to the registry.
	 * 
	 * @param contextType the context type to add
	 */	
	public void addContextType(ContextType contextType) {
		fContextTypes.put(contextType.getName(), contextType);
	}
	
	/**
	 * Returns the context type if the name is valid, <code>null</code> otherwise.
	 * 
	 * @param name the name of the context type to retrieve
	 * @return the context type if <code>name</code> is valid, <code>null</code> otherwise
	 */
	public ContextType getContextType(String name) {
		return (ContextType) fContextTypes.get(name);
	}
}
