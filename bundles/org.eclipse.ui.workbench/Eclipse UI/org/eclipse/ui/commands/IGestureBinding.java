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
 * JAVADOC
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
public interface IGestureBinding {

	/**
	 * JAVADOC
	 * 
	 * @return
	 */	
	String getCommandId();

	/**
	 * JAVADOC
	 * 
	 * @return
	 */	
	String getGestureConfigurationId();

	/**
	 * JAVADOC
	 * 
	 * @return
	 */	
	String getContextId();

	/**
	 * JAVADOC
	 * 
	 * @return
	 */	
	GestureSequence getGestureSequence();
	
	/**
	 * JAVADOC
	 * 
	 * @return
	 */	
	String getLocale();
	
	/**
	 * JAVADOC
	 * 
	 * @return
	 */	
	String getPlatform();
	
	/**
	 * JAVADOC
	 * 
	 * @return
	 */	
	String getPluginId();		
}
