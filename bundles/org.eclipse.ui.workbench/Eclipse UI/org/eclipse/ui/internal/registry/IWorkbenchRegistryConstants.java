/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.registry;

/**
 * Interface containing various registry constants (tag and attribute names).
 * 
 * @since 3.1
 */
public interface IWorkbenchRegistryConstants {

/* ***** Common constants ***** */
	
	/**
	 * Id attribute.  Value <code>id</code>.
	 */
	public static final String ATT_ID = "id"; //$NON-NLS-1$
    
    /**
     * Name attribute.  Value <code>name</code>.
     */
    public static final String ATT_NAME = "name"; //$NON-NLS-1$
    
    /**
     * Icon attribute.  Value <code>icon</code>.
     */
    public static final String ATT_ICON = "icon"; //$NON-NLS-1$
    
    /**
     * Value attribute.  Value <code>value</code>.
     */
    public static final String ATT_VALUE = "value"; //$NON-NLS-1$

	/**
	 * Class attribute.  Value <code>class</code>.
	 */
	public static final String ATT_CLASS = "class"; //$NON-NLS-1$
	
    /**
     * Perspective default attribute.  Value <code>default</code>.
     */
    public static final String ATT_DEFAULT = "default";//$NON-NLS-1$

	/**
	 * Description element.  Value <code>description</code>.
	 */
    public static final String TAG_DESCRIPTION = "description"; //$NON-NLS-1$
	
	/**
	 * Product id attribute.  Value <code>productId</code>.
	 */
	public static final String ATT_PRODUCTID = "productId"; //$NON-NLS-1$
    
    /**
     * Category tag.  Value <code>category</code>.
     */
    public static final String TAG_CATEGORY = "category";//$NON-NLS-1$
	
/* ***** org.eclipse.ui.activitySupport constants ***** */
	
	/**
	 * Advisor tag.  Value <code>triggerPointAdvisor</code>.
	 */
	public static final String TAG_TRIGGERPOINTADVISOR = "triggerPointAdvisor"; //$NON-NLS-1$

	/**
	 * Advisor id attribute.  Value <code>triggerPointAdvisorId</code>.
	 */
	public static final String ATT_ADVISORID = "triggerPointAdvisorId"; //$NON-NLS-1$
	
	/**
	 * Advisor to product binding element.  Value <code>triggerPointAdvisorProductBinding</code>. 
	 */
	public static final String TAG_ADVISORPRODUCTBINDING = "triggerPointAdvisorProductBinding"; //$NON-NLS-1$
    
    /**
     * Trigger point tag.  Value <code>triggerPoint</code>.
     */
    public static final String TAG_TRIGGERPOINT = "triggerPoint"; //$NON-NLS-1$
    
    /**
     * Trigger point hint tag.  Value <code>hint</code>.
     */
    public static final String TAG_HINT = "hint"; //$NON-NLS-1$


/* ***** org.eclipse.ui.views constants ***** */
    
    /**
     * View tag.  Value <code>view</code>.
     */
    public static final String TAG_VIEW = "view";//$NON-NLS-1$

    /**
     * Sticky view tag.  Value <code>stickyView</code>.
     */
    public static final String TAG_STICKYVIEW = "stickyView";//$NON-NLS-1$

    /**
     * Sticky view location attribute.  Value <code>location</code>.
     */
    public static final String ATT_LOCATION = "location"; //$NON-NLS-1$

    /**
     * Sticky view closable attribute.  Value <code>closable</code>.
     */
    public static final String ATT_CLOSEABLE = "closeable"; //$NON-NLS-1$    

    /**
     * Sticky view moveable attribute.  Value <code>moveable</code>.
     */
    public static final String ATT_MOVEABLE = "moveable"; //$NON-NLS-1$

    /**
     * View accelerator attribute.  Value <code>accelerator</code>.
     */
    public static final String ATT_ACCELERATOR = "accelerator"; //$NON-NLS-1$

    /**
     * View ratio attribute.  Value <code>fastViewWidthRatio</code>.
     */
    public static final String ATT_RATIO = "fastViewWidthRatio"; //$NON-NLS-1$

    /**
     * View multiple attribute.  Value <code>allowMultiple</code>.
     */
    public static final String ATT_MULTIPLE = "allowMultiple"; //$NON-NLS-1$

    /**
     * View parent category attribute.  Value <code>parentCategory</code>.
     */
    public static final String ATT_PARENT = "parentCategory"; //$NON-NLS-1$
    
/* ***** org.eclipse.ui.perspectives constants ***** */

    /**
     * Perspective singleton attribute.  Value <code>singleton</code>.
     */
    public static final String ATT_SINGLETON = "singleton";//$NON-NLS-1$

    /**
     * Perspective fixed attribute.  Value <code>fixed</code>.
     */
    public static final String ATT_FIXED = "fixed";//$NON-NLS-1$

/* ***** org.eclipse.ui.editors constants ***** */
    
    /**
     * Editor contributor class attribute.  Value <code>contributorClass</code>.
     */
    public static final String ATT_EDITOR_CONTRIBUTOR = "contributorClass"; //$NON-NLS-1$

    /**
     * Editor command attribute.  Value <code>command</code>.
     */
    public static final String ATT_COMMAND = "command";//$NON-NLS-1$

    /**
     * Editor launcher attribute.  Value <code>launcher</code>.
     */
    public static final String ATT_LAUNCHER = "launcher";//$NON-NLS-1$

    /**
     * Editor extensions attribute.  Value <code>extensions</code>.
     */
    public static final String ATT_EXTENSIONS = "extensions";//$NON-NLS-1$

    /**
     * Editor filenames attribute.  Value <code>filenames</code>.
     */
    public static final String ATT_FILENAMES = "filenames";//$NON-NLS-1$
	
	/**
	 * Editor content type binding tag.  Value <code>contentTypeBinding</code>.
	 */
	public static final String TAG_CONTENT_TYPE_BINDING = "contentTypeBinding"; //$NON-NLS-1$

	/**
	 * Editor content type id binding attribute.  Value <code>contentTypeId</code>.
	 */
	public static final String ATT_CONTENT_TYPE_ID = "contentTypeId"; //$NON-NLS-1$

    /**
     * Browser support tag.  Value <code>support</code>.
     */
    public static final String TAG_SUPPORT = "support"; //$NON-NLS-1$

    /**
     * Editor management strategy attribute.  Value <code>matchingStrategy</code>.
     */
    public static final String ATT_MATCHING_STRATEGY = "matchingStrategy"; //$NON-NLS-1$
}
