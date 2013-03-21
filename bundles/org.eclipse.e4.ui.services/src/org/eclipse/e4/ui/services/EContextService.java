/*******************************************************************************
 * Copyright (c) 2009, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.services;

import java.util.Collection;

import org.eclipse.core.commands.contexts.Context;

/**
 * @noimplement This interface is not intended to be implemented by clients.
 * @since 1.0
 */
public interface EContextService {
	
	/**
	 * Look up a {@link Context} with a given id. If no {@link Context} has the given id
	 * then <code>null</code> is returned
	 * 
	 * @param id the id of the {@link Context}, cannot be <code>null</code>
	 * @return the {@link Context} with the given id or <code>null</code>
	 */
	public Context getContext(String id);
	
	/**
	 * Adds the given {@link Context} id to the list of active contexts
	 * 
	 * @param id the id of the {@link Context}, cannot be <code>null</code>
	 */
	public void activateContext(String id);
	
	/**
	 * Removes the given {@link Context} id from the list of active {@link Context}s
	 * 
	 * @param id the id of the {@link Context}, cannot be <code>null</code>
	 */
	public void deactivateContext(String id);
	
	/**
	 * Returns the complete listing of {@link Context} ids that are active or
	 * <code>null</code> if there are no active {@link Context}s
	 * 
	 * @return the active {@link Context} ids or <code>null</code>
	 * @see IServiceConstants#ACTIVE_CONTEXTS
	 */
	public Collection<String> getActiveContextIds();
	
	/**
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public void deferUpdates(boolean defer);
}
