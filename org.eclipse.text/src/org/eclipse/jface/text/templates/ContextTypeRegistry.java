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

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * A singleton to keep track of all known context types.
 * 
 * @since 3.0
 */
public class ContextTypeRegistry {

	/** the singleton */
	private static ContextTypeRegistry fInstance;
	
	/** all known context types */
	private final Map fContextTypes= new HashMap();
	
	/**
	 * Returns the single instance of this class.
	 * 
	 * @return the singleton registry
	 */
	public static ContextTypeRegistry getInstance() {
		if (fInstance == null)
			fInstance= new ContextTypeRegistry();
			
		return fInstance;	
	}

	/**
	 * Adds a context type to the registry.
	 * 
	 * @param contextType the context type to add
	 */	
	public void add(ContextType contextType) {
		fContextTypes.put(contextType.getName(), contextType);
	}
	
	/**
	 * Removes a context type from the registry.
	 * 
	 * @param contextType the context type to remove
	 */
	public void remove(ContextType contextType) {
		fContextTypes.remove(contextType.getName());
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
	
	/**
	 * Returns an iterator over the registered context type names.
	 * 
	 * @return an iterator over all registered context type names
	 */
	public Iterator iterator() {
		return Collections.unmodifiableMap(fContextTypes).keySet().iterator();	
	}

	private ContextTypeRegistry() {
		/*
		 * What we should probably do: 
		 * - have an extension point for template contexts
		 * - add contributed contexts here.
		 * - the id of contributions is the key into the registry
		 */
		
		// XXX bootstrap with java and javadoc context types
//		add(new JavaContextType());
//		add(new JavaDocContextType());
		
//		CodeTemplateContextType.registerContextTypes(this);
	}

}
