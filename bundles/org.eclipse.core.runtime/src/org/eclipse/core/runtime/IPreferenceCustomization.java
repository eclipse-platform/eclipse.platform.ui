/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.runtime;

/**
 * Interface used to aid in default preference value customization.
 * Clients who extend the <code>org.eclipse.core.runtime.preferences</code> 
 * extension point are able to specify a class within a <code>customization</code>
 * element which implements this interface. 
 * <p>
 * Clients may implement this interface.
 * </p>
 * @since 3.0
 */
public interface IPreferenceCustomization {

	/**
	 * 
	 * TODO
	 */
	public void initializeDefaultPreferences();

}