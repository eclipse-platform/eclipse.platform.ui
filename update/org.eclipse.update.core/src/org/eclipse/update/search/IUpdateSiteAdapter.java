/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.search;

import java.net.*;

/**
 * This interface wraps an update site URL and adds 
 * a presentation label. It is used to encapsulate sites that need
 * to be visited during the update search.
 */

public interface IUpdateSiteAdapter {
	/**
	 * Returns the presentation string that can be used
	 * for this site.
	 * @return the update site label
	 */
	public String getLabel();
	/**
	 * Returns the URL of the update site.
	 * @return the URL of the update site.
	 */
	public URL getURL();
}