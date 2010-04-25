/*******************************************************************************
 * Copyright (c) 2005, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.navigator.extensions;

/**
 * 
 * Constants for Extension Point managers and descriptors.
 */
public interface INavigatorContentExtPtConstants {

	/** */
	String TAG_ACTION_PROVIDER = "actionProvider"; //$NON-NLS-1$

	/** */
	String TAG_NAVIGATOR_CONTENT = "navigatorContent"; //$NON-NLS-1$

	/** */
	String TAG_COMMON_WIZARD = "commonWizard"; //$NON-NLS-1$

	/** */
	String TAG_ENABLEMENT = "enablement"; //$NON-NLS-1$

	/** */
	String TAG_TRIGGER_POINTS = "triggerPoints"; //$NON-NLS-1$

	/** */
	String TAG_POSSIBLE_CHILDREN = "possibleChildren"; //$NON-NLS-1$

	/** */
	String TAG_DUPLICATE_CONTENT_FILTER = "duplicateContentFilter"; //$NON-NLS-1$	

	/** */
	String TAG_COMMON_FILTER = "commonFilter"; //$NON-NLS-1$

	/** */
	String TAG_COMMON_SORTER = "commonSorter"; //$NON-NLS-1$
	
	/** */
	String TAG_COMMON_DROP_ADAPTER = "dropAssistant"; //$NON-NLS-1$	 
	
	/** */
	String TAG_POSSIBLE_DROP_TARGETS = "possibleDropTargets"; //$NON-NLS-1$	 
	
	/** */
	String TAG_OVERRIDE = "override"; //$NON-NLS-1$	

	/** */
	String TAG_INITIAL_ACTIVATION = "initialActivation"; //$NON-NLS-1$	

	/** */
	String TAG_FILTER_EXPRESSION = "filterExpression"; //$NON-NLS-1$

	/** */
	String TAG_PARENT_EXPRESSION = "parentExpression"; //$NON-NLS-1$ 

	/** */
	String ATT_ID = "id"; //$NON-NLS-1$

	/** */
	String ATT_NAME = "name"; //$NON-NLS-1$	 

	/** */
	String ATT_DESCRIPTION = "description"; //$NON-NLS-1$	  

	/** */
	String ATT_CLASS = "class"; //$NON-NLS-1$ 

	/** */
	String ATT_PRIORITY = "priority"; //$NON-NLS-1$

	/** */
	String ATT_APPEARS_BEFORE = "appearsBefore"; //$NON-NLS-1$	
	
	/** */
	String ATT_ICON = "icon"; //$NON-NLS-1$

	/** */
	String ATT_WIZARD_ID = "wizardId"; //$NON-NLS-1$

	/** */
	String ATT_TYPE = "type"; //$NON-NLS-1$		

	/** */
	String ATT_ACTIVE_BY_DEFAULT = "activeByDefault"; //$NON-NLS-1$

	/** */
	String ATT_VISIBLE_IN_UI = "visibleInUI"; //$NON-NLS-1$

	/** */
	String ATT_SORT_ONLY = "sortOnly"; //$NON-NLS-1$

	/** */
	String ATT_PROVIDES_SAVEABLES = "providesSaveables"; //$NON-NLS-1$
	
	/** */
	String ATT_CONTENT_PROVIDER = "contentProvider"; //$NON-NLS-1$

	/** */
	String ATT_LABEL_PROVIDER = "labelProvider"; //$NON-NLS-1$

	/** */
	String ATT_VIEWER_FILTER = "viewerFilter"; //$NON-NLS-1$

	/** */
	String ATT_ACTION_PROVIDER = "actionProvider"; //$NON-NLS-1$  

	/** */
	String ATT_DEPENDS_ON = "dependsOn"; //$NON-NLS-1$	
	
	/** */
	String ATT_OVERRIDES = "overrides"; //$NON-NLS-1$	
	
	/** */
	String ATT_POLICY = "policy"; //$NON-NLS-1$
	
	/** */
	String ATT_SUPPRESSED_EXT_ID = "suppressedExtensionId"; //$NON-NLS-1$  
	
	/** */
	String ATT_MENU_GROUP_ID = "menuGroupId"; //$NON-NLS-1$
	
	/** */
	String ATT_ASSOCIATED_EXTENSION_ID = "associatedExtensionId"; //$NON-NLS-1$


}
