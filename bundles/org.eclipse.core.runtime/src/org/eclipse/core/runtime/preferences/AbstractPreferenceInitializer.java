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
package org.eclipse.core.runtime.preferences;

/**
 * Abstract class used to aid in default preference value initialization.
 * Clients who extend the <code>org.eclipse.core.runtime.preferences</code> 
 * extension point are able to specify a class within an <code>initializer</code>
 * element. 
 * 
 * @since 3.0
 */
public abstract class AbstractPreferenceInitializer {

	/**
	 * Default constructor for the class.
	 */
	public AbstractPreferenceInitializer() {
		super();
	}

	/**
	 * This method is called by the preference initializer to initialize default
	 * preference values. Clients should get the correct node for their 
	 * bundle and then set the default values on it. For example:
	 * <pre>
	 *			public void initializeDefaultPreferences() {
	 *				Preferences node = new DefaultScope().getNode("my.bundle.id");
	 *				node.put(key, value);
	 *			}
	 * </pre>
	 */
	public abstract void initializeDefaultPreferences();

}