/*******************************************************************************
 * Copyright (c) 2006, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.navigator;

/**
 * Provides information about a <b>commonFilter</b> extension.
 *  
 * @since 3.2
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 * 
 */
public interface ICommonFilterDescriptor {
	
	/**
	 * 
	 * @return An identifier used to determine whether the filter is visible.
	 * 
	 */
	String getId();

	/**
	 * 
	 * @return A translated name to identify the filter
	 */
	String getName();

	/**
	 * 
	 * @return A translated description to explain to the user what the defined
	 *         filter will hide from the view.
	 */
	String getDescription();

	/**
	 * 
	 * @return Indicates the filter should be in an "Active" state by default.
	 */
	boolean isActiveByDefault();
}
