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

package org.eclipse.ui.activities;

/**
 * An instance of this interface represents a binding between two activities.
 * The parent-child relationship can be interpreted as 'parent requires child'.
 * The enablement of the parent should require the enablement of the child. 
 * 
 * <p>
 * This interface is not intended to be extended or implemented by clients.
 * </p>
 * 
 * @since 3.0
 * @see IActivity
 */
public interface IActivityActivityBinding extends Comparable {

	/**
	 * Returns the identifier of the child activity represented in this
	 * binding.  The enablement of this activity is a prerequisit for the 
	 * enablement of the parent.
	 * 
	 * @return the identifier of the child activity represented in this
	 *         binding. Guaranteed not to be <code>null</code>.
	 */
	String getChildActivityId();

	/**
	 * Returns the identifier of the parent activity represented in this
	 * binding.  The enablement of this activity requires the enablement of the 
	 * child.
	 * 
	 * @return the identifier of the parent activity represented in this
	 *         binding. Guaranteed not to be <code>null</code>.
	 */
	String getParentActivityId();
}
