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

package org.eclipse.ui.commands;

/**
 * <p>
 * TODO javadoc
 * </p>
 * <p>
 * This interface is not intended to be extended or implemented by clients.
 * </p>
 * <p>
 * <em>EXPERIMENTAL</em>
 * </p>
 * 
 * @since 3.0
 */
public interface IKeyBinding {

	/**
	 * TODO javadoc
	 * 
	 * @return
	 */	
	String getCommandId();

	/**
	 * TODO javadoc
	 * 
	 * @return
	 */	
	String getKeyConfigurationId();

	/**
	 * TODO javadoc
	 * 
	 * @return
	 */	
	String getContextId();

	/**
	 * TODO javadoc
	 * 
	 * @return
	 */	
	IKeySequence getKeySequence();
	
	/**
	 * TODO javadoc
	 * 
	 * @return
	 */	
	String getLocale();
	
	/**
	 * TODO javadoc
	 * 
	 * @return
	 */	
	String getPlatform();
	
	/**
	 * TODO javadoc
	 * 
	 * @return
	 */	
	String getPluginId();		
}
