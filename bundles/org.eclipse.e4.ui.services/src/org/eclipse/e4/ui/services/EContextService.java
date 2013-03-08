/*******************************************************************************
 * Copyright (c) 2009,2013 IBM Corporation and others.
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
 * @noimplement
 */
public interface EContextService {
	public Context getContext(String id);
	
	public void activateContext(String id);
	
	public void deactivateContext(String id);
	
	public Collection<String> getActiveContextIds();
	
	/**
	 * @noreference
	 */
	public void deferUpdates(boolean defer);
}
