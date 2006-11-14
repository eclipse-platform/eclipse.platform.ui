/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.core;

/**
 * This interface provides a repository for the names of <code>IConfigurationElement</code> child node ids.
 * @since 3.3
 */
public interface IConfigurationElementConstants {

	/**
	 * The id node name for a configuration element
	 */
	public static final String ID = "id"; //$NON-NLS-1$
	
	/**
	 * the name node name for a configuration element
	 */
	public static final String NAME = "name"; //$NON-NLS-1$
	
	/**
	 * the category node name for a configuration element
	 */
	public static final String CATEGORY = "category"; //$NON-NLS-1$
	
	/**
	 * the launchMode node name for a configuration element
	 */
	public static final String LAUNCH_MODE = "launchMode"; //$NON-NLS-1$
	
	/**
	 * the after node name for a configuration element
	 */
	public static final String AFTER = "after"; //$NON-NLS-1$
	
	/**
	 * the placement node name for a configuration element
	 */
	public static final String PLACEMENT = "placement"; //$NON-NLS-1$
	
	/**
	 * the associated launch delegate node name for s configuration element
	 */
	public static final String ASSOCIATED_DELEGATE = "associatedDelegate"; //$NON-NLS-1$
	
	/**
	 * the label node name for a configuration element
	 */
	public static final String LABEL = "label"; //$NON-NLS-1$
	
	/**
	 * the description node name for a configuration element
	 */
	public static final String DESCRIPTION = "description"; //$NON-NLS-1$
	
	/**
	 * the helpContextId node name for a configuration element
	 */
	public static final String HELP_CONTEXT_ID = "helpContextId"; //$NON-NLS-1$
	
	/**
	 * the icon node name for a configuration element
	 */
	public static final String ICON = "icon"; //$NON-NLS-1$
	
	/**
	 * the public node name for a configuration element
	 */
	public static final String PUBLIC = "public"; //$NON-NLS-1$
	
	/**
	 * the perspective node name for a configuration element
	 */
	public static final String PERSPECTIVE = "perspective"; //$NON-NLS-1$
	
	/**
	 * the modes node name for a configuration element
	 */
	public static final String MODES = "modes"; //$NON-NLS-1$
	
	/**
	 * the modesCombination node name for a configuraiton element 
	 */
	public static final String MODE_COMBINATION = "modeCombination"; //$NON-NLS-1$
	
	/**
	 * the mode node name for a configuration element
	 */
	public static final String MODE = "mode"; //$NON-NLS-1$
	
	/**
	 * the type node name for a configuration element
	 */
	public static final String TYPE = "type"; //$NON-NLS-1$
	
	/**
	 * the option node name for a configuration element
	 */
	public static final String OPTIONS = "options"; //$NON-NLS-1$
	
	/**
	 * the delegate node name for a configuration element
	 */
	public static final String DELEGATE = "delegate"; //$NON-NLS-1$
	
	/**
	 * the delegatename node name for a configuration element
	 */
	public static final String DELEGATE_NAME = "delegateName"; //$NON-NLS-1$
	
	/**
	 * the group node name for a configuration element
	 */
	public static final String GROUP = "group"; //$NON-NLS-1$
	
	/**
	 * the class node name for a configuration element
	 */
	public static final String CLASS = "class"; //$NON-NLS-1$
	
	/**
	 * the sourcePathComputerId node name for a configuration element
	 */
	public static final String SOURCE_PATH_COMPUTER = "sourcePathComputerId"; //$NON-NLS-1$
	
	/**
	 * the delegateDescription node name for a configuraiton element
	 */
	public static final String DELEGATE_DESCRIPTION = "delegateDescription"; //$NON-NLS-1$
	
	/**
	 * the sourceLocatorId node name for a configuration element
	 */
	public static final String SOURCE_LOCATOR = "sourceLocatorId"; //$NON-NLS-1$
	
	/**
	 * the migrationDelegate node name for a configuration element
	 */
	public static final String MIGRATION_DELEGATE = "migrationDelegate";  //$NON-NLS-1$
}
