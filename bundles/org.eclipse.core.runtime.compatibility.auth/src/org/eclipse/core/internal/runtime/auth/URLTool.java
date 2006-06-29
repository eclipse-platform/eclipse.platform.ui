/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.runtime.auth;

import java.net.MalformedURLException;
import java.net.URL;
import org.eclipse.core.runtime.Assert;

/**
 * A utility for manipulating <code>URL</code>s.
 */
public class URLTool {

	/**
	 * Returns the parent URL of the given URL, or <code>null</code> if the
	 * given URL is the root.
	 * <table>
	 * <caption>Example</caption>
	 * <tr>
	 *   <th>Given URL</th>
	 *   <th>Parent URL</th>
	 * <tr>
	 *   <td>"http://hostname/"</td>
	 *   <td>null</td>
	 * <tr>
	 *   <td>"http://hostname/folder/file</td>
	 *   <td>"http://hostname/folder/</td>
	 * </table>
	 *
	 * @param url a URL
	 * @return    the parent of the given URL
	 */
	public static URL getParent(URL url) {
		String file = url.getFile();
		int len = file.length();
		if (len == 0 || len == 1 && file.charAt(0) == '/')
			return null;
		int lastSlashIndex = -1;
		for (int i = len - 2; lastSlashIndex == -1 && i >= 0; --i) {
			if (file.charAt(i) == '/')
				lastSlashIndex = i;
		}
		if (lastSlashIndex == -1)
			file = ""; //$NON-NLS-1$
		else
			file = file.substring(0, lastSlashIndex + 1);

		try {
			url = new URL(url.getProtocol(), url.getHost(), url.getPort(), file);
		} catch (MalformedURLException e) {
			Assert.isTrue(false, e.getMessage());
		}
		return url;
	}
}
