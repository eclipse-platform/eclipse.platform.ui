/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
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
 * A registry for context types. Editor implementors will usually instantiate a
 * registry and configure the context types available in their editor.
 * 
 * @since 3.0
 */
public class ContextTypeRegistry {

	/** all known context types */
	private final Map fContextTypes= new HashMap();
	
	/**
	 * Adds a context type to the registry. If there already is a context type
	 * with the same ID registered, it is replaced. 
	 * 
	 * @param contextType the context type to add
	 */	
	public void addContextType(TemplateContextType contextType) {
		fContextTypes.put(contextType.getId(), contextType);
	}
	
	/**
	 * Returns the context type if the id is valid, <code>null</code> otherwise.
	 * 
	 * @param id the id of the context type to retrieve
	 * @return the context type if <code>name</code> is valid, <code>null</code> otherwise
	 */
	public TemplateContextType getContextType(String id) {
		return (TemplateContextType) fContextTypes.get(id);
	}

	/**
	 * Returns an iterator over all registered context types.
	 * 
	 * @return an iterator over all registered context types
	 */
	public Iterator contextTypes() {
		return fContextTypes.values().iterator();
	}
}
