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
package org.eclipse.ui.activities;

/**
 * <p>
 * This interface is not intended to be extended or implemented by clients.
 * </p>
 * <em>EXPERIMENTAL</em>
 * @since 3.1
 */
public interface ITriggerPoint {

	/**
	 * The interactive hint key.  Value <code>"interactive"</code>.
	 */
	public static final String HINT_INTERACTIVE = "interactive"; //$NON-NLS-1$
	
	/**
	 * Return the id of this trigger point.
	 * 
	 * @return the id
	 */
	String getId();

	/**
	 * Return the hint with the given key defined on this trigger point.
	 * 
	 * @param key the key
	 * @return the hint
	 */
	String getStringHint(String key);
	
	
	/**
	 * Return the hint with the given key defined on this trigger point.
	 * 
	 * @param key the key
	 * @return the hint
	 */	
	boolean getBooleanHint(String key);
}
