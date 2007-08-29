/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.help.internal.webapp.servlet;

import javax.servlet.http.HttpServletRequest;

public class FilterUtils {
	
	public static String getRelativePathPrefix(HttpServletRequest req) {
		// append "../" to get to the webapp
		StringBuffer result = new StringBuffer(""); //$NON-NLS-1$
		String path = req.getPathInfo();
		if (path != null) {
			for (int i; 0 <= (i = path.indexOf('/')); path = path
					.substring(i + 1)) {
				result.append("../"); //$NON-NLS-1$
			}
		}
		return result.toString();
	}

}
