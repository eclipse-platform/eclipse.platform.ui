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
 * Container class for associating AbstractHelpScopes with an ID
 * 
 * @since 3.6
 */
public interface IScopeHandle {

	/**
	 * Get the AbstractHelpScope associated with this handle
	 * 
	 * @return AbstractHelpScope
	 */
	public AbstractHelpScope getScope();
	
	/**
	 * Get the String ID associated with this handle
	 * 
	 * @return ID
	 */
	public String getId();
}
