/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.help.base;

/**
 * Producer capable of generating or otherwise obtaining AbstractHelpScopes
 * for filtering the help system.  Scopes can be defined either by adding a
 * 'scope' or 'scopeProducer' element to the 
 * <code>"org.eclipse.help.base.scope"</code> extension point.
 * 
 * The 'scopeProducer' allows for runtime scopes to be created.
 * 
 * 
 * @since 3.6
 */
public interface IHelpScopeProducer {
	
	/**
	 * Obtains a list of IScopeHandles, which in turn will contain
	 * AbstracHelpScope implementations.  Use this method to create
	 * or load AbstractHelpScopes at runtime.
	 * 
	 * @return an array of the contributed scope handles. May not be <code>null</code>.
	 */
	public IScopeHandle[] getScopeHandles();
}
