/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.commands.api;

/**
 * <p>
 * An instance of <code>ICommandManagerEvent</code> describes changes to an 
 * instance of <code>ICommandManager</code>. 
 * </p>
 * <p>
 * This interface is not intended to be extended or implemented by clients.
 * </p>
 * <p>
 * <em>EXPERIMENTAL</em>
 * </p>
 * 
 * @since 3.0
 * @see ICommandManager
 * @see ICommandManagerListener#commandManagerChanged
 */
public interface ICommandManagerEvent {

	/**
	 * Returns the instance of <code>ICommandManager</code> that has changed.
	 *
	 * @return the instance of <code>ICommandManager</code> that has changed. 
	 *         Guaranteed not to be <code>null</code>.
	 */
	ICommandManager getCommandManager();

	/**
	 * TODO javadoc
	 */	
	boolean hasActiveKeyConfigurationIdChanged();

	/**
	 * TODO javadoc
	 */	
	boolean hasActiveLocaleChanged();	

	/**
	 * TODO javadoc
	 */	
	boolean hasActivePlatformChanged();	
	
	/**
	 * TODO javadoc
	 */	
	boolean haveActiveActivityIdsChanged();
	
	/**
	 * TODO javadoc
	 */	
	boolean haveActiveCommandIdsChanged();

	/**
	 * TODO javadoc
	 */		
	boolean haveDefinedCategoryIdsChanged();
	
	/**
	 * TODO javadoc
	 */		
	boolean haveDefinedCommandIdsChanged();

	/**
	 * TODO javadoc
	 */		
	boolean haveDefinedKeyConfigurationIdsChanged();	
	
	/**
	 * TODO javadoc
	 */	
	boolean haveEnabledCommandIdsChanged();	
}
