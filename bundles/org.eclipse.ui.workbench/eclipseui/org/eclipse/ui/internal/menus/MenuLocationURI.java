/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.menus;

import org.eclipse.ui.internal.util.Util;

/**
 * Basic implementation of the java.net.URI api. This is needed because the java
 * 'foundation' doesn't contain the actual <code>java.net.URI</code> class.
 * <p>
 * The expected format for URI Strings managed by this class is:
 * </p>
 * <p>
 * "[scheme]:[path]?[query]"
 * </p>
 * <p>
 * with the 'query' format being "[id1]=[val1]&amp;[id2]=[val2]..."
 * </p>
 *
 * @since 3.3
 */
public class MenuLocationURI {

	private String rawString;

	public MenuLocationURI(String uriDef) {
		rawString = uriDef;
	}

	/**
	 * @return The query part of the uri (i.e. the part after the '?').
	 */
	public String getQuery() {
		// Trim off the scheme
		String[] vals = Util.split(rawString, '?');
		return vals.length > 1 ? vals[1] : Util.ZERO_LENGTH_STRING;
	}

	/**
	 * @return The scheme part of the uri (i.e. the part before the ':').
	 */
	public String getScheme() {
		String[] vals = Util.split(rawString, ':');
		return vals[0];
	}

	/**
	 * @return The path part of the uri (i.e. the part between the ':' and the '?').
	 */
	public String getPath() {
		// Trim off the scheme
		String[] vals = Util.split(rawString, ':');
		if (vals.length < 2)
			return null;

		// Now, trim off any query
		vals = Util.split(vals[1], '?');
		return vals.length == 0 ? Util.ZERO_LENGTH_STRING : vals[0];
	}

	@Override
	public String toString() {
		return rawString;
	}

	/**
	 * @return the full URI definition string
	 */
	public String getRawString() {
		return rawString;
	}
}
