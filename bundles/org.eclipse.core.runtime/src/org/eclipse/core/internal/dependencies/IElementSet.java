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
package org.eclipse.core.internal.dependencies;

import java.util.Collection;

/**
 * Not to be implemented by clients.
 */
public interface IElementSet {
	public IDependencySystem getSystem();
	/** @return false if there is at least one version that is a singleton. */
	public boolean allowsConcurrency();
	/**
	 * Returns the unique id for this element set.
	 */
	public Object getId();
	/**
	 * Is this a root element set? 	
	  */
	public boolean isRoot();
	/**
	 * Returns all elements available in this element set.
	 */
	public Collection getAvailable();
	/**
	 * Returns all elements sets required this element set.
	 */
	public Collection getRequired();
	/**
	 * Returns all elements sets requiring this element set.
	 */
	public Collection getRequiring();
	/**
	 * Returns all elements currently resolved in this element set.
	 */			
	public Collection getResolved();
	/**
	 * Returns all elements currently satisfied in this element set.
	 */			
	public Collection getSatisfied();
	/**
	 * Returns all elements currently selected in this element set.
	 */	
	public Collection getSelected();
}