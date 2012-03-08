/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.runtime.model;

/**
 * An object which represents a named URL in a component or configuration
 * manifest.
 * <p>
 * This class may be instantiated and further subclassed.
 * </p>
 * @deprecated In Eclipse 3.0 the runtime was refactored and all 
 * non-essential elements removed.  This class provides facilities primarily intended
 * for tooling.  As such it has been removed and no directly substitutable API provided.
 * This API will be deleted in a future release. See bug 370248 for details.
 */
public class URLModel extends PluginModelObject {
	// DTD properties (included in install manifest)
	private String url = null;

	/**
	 * Returns the URL specification.
	 *
	 * @return the URL specification or <code>null</code>.
	 */
	public String getURL() {
		return url;
	}

	/**
	 * Sets the URL specification.
	 * This object must not be read-only.
	 *
	 * @param value the URL specification.
	 *		May be <code>null</code>.
	 */
	public void setURL(String value) {
		assertIsWriteable();
		url = value;
	}

}
