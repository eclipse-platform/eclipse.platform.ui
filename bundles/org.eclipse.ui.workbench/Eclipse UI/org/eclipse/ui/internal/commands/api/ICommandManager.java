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

import java.util.Set;

/**
 * <p>
 * An instance of <code>ICommandManager</code> can be used to obtain instances
 * of <code>ICommand</code>, as well as manage whether or not those instances
 * are active or inactive, enabled or disabled.
 * </p> 
 * <p>
 * This interface is not intended to be extended or implemented by clients.
 * </p>
 * <p>
 * <em>EXPERIMENTAL</em>
 * </p>
 * 
 * @since 3.0
 * @see CommandManagerFactory
 * @see ICommand
 * @see ICommandManagerListener
 */
public interface ICommandManager {

	/**
	 * Registers an instance of <code>ICommandManagerListener</code> to listen 
	 * for changes to attributes of this instance.
	 *
	 * @param commandManagerListener the instance of 
	 *                               <code>ICommandManagerListener</code> to 
	 * 						         register. Must not be <code>null</code>. 
	 *                               If an attempt is made to register an 
	 *                               instance of 
	 *                               <code>ICommandManagerListener</code> 
	 *                               which is already registered with this 
	 *                               instance, no operation is performed.
	 */		
	void addCommandManagerListener(ICommandManagerListener commandManagerListener);

	/**
	 * TODO javadoc
	 */
	Set getActiveActivityIds();

	/**
	 * <p>
	 * Returns the set of identifiers to active commands. This set is not 
	 * necessarily a subset of the set of identifiers to defined commands.
	 * </p>
	 * <p>
	 * Notification is set to all registered listeners if this attribute 
	 * changes.
	 * </p>
	 *
	 * @return the set of identifiers to active commands. This set may be 
	 *         empty, but is guaranteed not to be <code>null</code>. If this set 
	 *         is not empty, it is guaranteed to only contain instances of 
	 *         <code>String</code>.
	 */	
	Set getActiveCommandIds();	
	
	/**
	 * TODO javadoc
	 */
	String getActiveKeyConfigurationId();

	/**
	 * TODO javadoc
	 */
	String getActiveLocale();
	
	/**
	 * TODO javadoc
	 */
	String getActivePlatform();

	/**
	 * Returns a handle to a category given an identifier.
	 *
	 * @param categoryId an identifier. Must not be <code>null</code>
	 * @return           a handle to a category.
	 */	
	ICategory getCategory(String categoryId);	
	
	/**
	 * Returns a handle to a command given an identifier.
	 *
	 * @param commandId an identifier. Must not be <code>null</code>
	 * @return          a handle to a command.
	 */	
	ICommand getCommand(String commandId);

	/**
	 * <p>
	 * Returns the set of identifiers to defined categories.
	 * </p>
	 * <p>
	 * Notification is set to all registered listeners if this attribute 
	 * changes.
	 * </p>
	 *
	 * @return the set of identifiers to defined categories. This set may be 
	 *         empty, but is guaranteed not to be <code>null</code>. If this set 
	 *         is not empty, it is guaranteed to only contain instances of 
	 *         <code>String</code>.
	 */	
	Set getDefinedCategoryIds();	
	
	/**
	 * <p>
	 * Returns the set of identifiers to defined commands.
	 * </p>
	 * <p>
	 * Notification is set to all registered listeners if this attribute 
	 * changes.
	 * </p>
	 *
	 * @return the set of identifiers to defined commands. This set may be 
	 *         empty, but is guaranteed not to be <code>null</code>. If this set 
	 *         is not empty, it is guaranteed to only contain instances of 
	 *         <code>String</code>.
	 */	
	Set getDefinedCommandIds();
	
	/**
	 * <p>
	 * Returns the set of identifiers to defined key configurations.
	 * </p>
	 * <p>
	 * Notification is set to all registered listeners if this attribute 
	 * changes.
	 * </p>
	 *
	 * @return the set of identifiers to defined key configurations. This set 
	 * 		   may be empty, but is guaranteed not to be <code>null</code>. If 
	 * 		   this set is not empty, it is guaranteed to only contain instances 
	 * 		   of <code>String</code>.
	 */	
	Set getDefinedKeyConfigurationIds();	

	/**
	 * <p>
	 * Returns the set of identifiers to enabled commands. This set is not 
	 * necessarily a subset of the set of identifiers to defined commands.
	 * </p>
	 * <p>
	 * Notification is set to all registered listeners if this attribute 
	 * changes.
	 * </p>
	 *
	 * @return the set of identifiers to enabled commands. This set may be 
	 *         empty, but is guaranteed not to be <code>null</code>. If this set 
	 *         is not empty, it is guaranteed to only contain instances of 
	 *         <code>String</code>.
	 */	
	Set getEnabledCommandIds();
	
	/**
	 * Returns a handle to a key configuration given an identifier.
	 *
	 * @param keyConfigurationId an identifier. Must not be <code>null</code>
	 * @return          	     a handle to a key configuration.
	 */	
	IKeyConfiguration getKeyConfiguration(String keyConfigurationId);	
	
	/**
	 * Unregisters an instance of <code>ICommandManagerListener</code> 
	 * listening for changes to attributes of this instance.
	 *
	 * @param commandManagerListener the instance of 
	 *                               <code>ICommandManagerListener</code> to 
	 * 						         unregister. Must not be <code>null</code>. 
	 *                               If an attempt is made to unregister an 
	 *                               instance of 
	 *                               <code>ICommandManagerListener</code> 
	 *                               which is not already registered with this 
	 *                               instance, no operation is performed.
	 */
	void removeCommandManagerListener(ICommandManagerListener commandManagerListener);

	/**
	 * Sets the set of identifiers to active activities. 
	 *
	 * @param activeActivityIds the set of identifiers to active activities. 
	 *                          This set may be empty, but it must not be 
	 *                          <code>null</code>. If this set is not empty, it 
	 *                          must only contain instances of 
	 *                          <code>String</code>.	
	 */
	void setActiveActivityIds(Set activeActivityIds);	
	
	/**
	 * Sets the set of identifiers to active commands. 
	 *
	 * @param activeCommandIds the set of identifiers to active commands. 
	 *                         This set may be empty, but it must not be 
	 *                         <code>null</code>. If this set is not empty, it 
	 *                         must only contain instances of 
	 *                         <code>String</code>.	
	 */
	void setActiveCommandIds(Set activeCommandIds);

	/**
	 * TODO javadoc
	 */
	void setActiveKeyConfigurationId(String activeKeyConfigurationId);	

	/**
	 * TODO javadoc
	 */
	void setActiveLocale(String activeLocale);	
	
	/**
	 * TODO javadoc
	 */
	void setActivePlatform(String activePlatform);	
	
	/**
	 * Sets the set of identifiers to enabled commands. 
	 *
	 * @param enabledCommandIds the set of identifiers to enabled commands. 
	 *                          This set may be empty, but it must not be 
	 *                          <code>null</code>. If this set is not empty, it 
	 *                          must only contain instances of 
	 *                          <code>String</code>.	
	 */
	void setEnabledCommandIds(Set enabledCommandIds);		
}
