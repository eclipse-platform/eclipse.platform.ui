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
import java.util.Iterator;
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
		fContextTypes.put(contextType.getId(), contextType);
	}
	
	/**
	 * Returns the context type if the id is valid, <code>null</code> otherwise.
	 * 
	 * @param id the id of the context type to retrieve
	 * @return the context type if <code>name</code> is valid, <code>null</code> otherwise
	 */
	public ContextType getContextType(String id) {
		return (ContextType) fContextTypes.get(id);
	}

	/**
	 * Returns the id of the default context type.
	 * 
	 * @return the id of the default context type
	 */
	public String getDefaultTypeId() {
		for (Iterator it= fContextTypes.keySet().iterator(); it.hasNext();) {
			return (String) it.next();
		}
		return null;
	}

	/**
	 * Returns all identifiers of registered context types.
	 * 
	 * @return all identifiers of registered context types
	 */
	public String[] getTypeIds() {
		return (String[]) fContextTypes.keySet().toArray(new String[0]);
	}
}
