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
package org.eclipse.help.internal.protocols;
/**
 * Creates the URL objects according to the their type
 */
public class HelpURLFactory {
	/**
	 * HelpURLFactory constructor.
	 */
	public HelpURLFactory() {
		super();
	}
	public static HelpURL createHelpURL(String url) {
		if (url == null || url.length() == 0)
			return new PluginURL("", "");
		// Strip off the leading "/" and the query
		if (url.startsWith("/"))
			url = url.substring(1);
		String query = "";
		int indx = url.indexOf("?");
		if (indx != -1) {
			query = url.substring(indx + 1);
			url = url.substring(0, indx);
		} 
		
		return new PluginURL(url, query);
	}
}
