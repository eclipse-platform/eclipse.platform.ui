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
     * Value attribute.  Value <code>value</code>.
     */
    public static final String ATT_VALUE = "value"; //$NON-NLS-1$

	/**
	 * Class attribute.  Value <code>class</code>.
	 */
	public static final String ATT_CLASS = "class"; //$NON-NLS-1$
	
	/**
	 * Description element.  Value <code>description</code>.
	 */
    public static final String TAG_DESCRIPTION = "description"; //$NON-NLS-1$
	
	/**
	 * Product id attribute.  Value <code>productId</code>.
	 */
	public static final String ATT_PRODUCTID = "productId"; //$NON-NLS-1$
	
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
}
