/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.runtime.preferences;

import java.util.Map;

/**
 * Preference Transfers are used to describe the relationship between the
 * preference tree and a data set when importing/exporting preferences.
 * <p>
 * For instance, a client is able to create a preference transfer describing
 * which preference nodes/keys should be used when exporting the
 * "Key Bindings" preferences. When the export happens, the tree is
 * trimmed and only the applicable preferences will be exported.
 * </p>
 * 
 * @since 3.1
 */
public interface IPreferenceTransfer {

	/**
	 * Return an array of scopes that this preference transfer is applicable for.
	 * <p>
	 * For example: 
	 * <code>new String[] {"instance", "configuration"};</code> or
	 * <code>new String[] {InstanceScope.SCOPE, ConfigurationScope.SCOPE};</code>
	 * </p>
	 * 
	 * @return the array of scopes
	 */
	public String[] getScopes();
	
	/**
	 * Return a mapping which defines the nodes and keys that this transfer
	 * applies to. 
	 * <p>
	 * The keys in the table are Strings and describe the node path. The values are 
	 * an optional String array describing the list of applicable keys in that node. 
	 * If the value is null then the whole node is considered applicable.
	 * </p>
	 * <p>
	 * key: <code>String</code> (node)<br>
	 * value: <code>String[]</code> or <code>null</code> (preference keys)<br>
	 * </p>
	 * <p>
	 * For example:
	 * <pre>
	 * "org.eclipse.core.resources" -> null
	 * "org.eclipse.ui" -> ["DEFAULT_PERSPECTIVE_LOCATION", "SHOW_INTRO_ON_STARTUP"]
	 * </pre>
	 * </p>
	 * 
	 * @return the mapping table
	 */
	public Map getMapping();

}
