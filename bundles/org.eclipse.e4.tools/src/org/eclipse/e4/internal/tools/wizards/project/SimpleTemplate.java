/*******************************************************************************
 * Copyright (c) 2014 TwelveTone LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Steven Spungin <steven@spungin.tv> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.internal.tools.wizards.project;

import java.util.Map;

/**
 * A <em>very</em> basic template processor.<br />
 * Any value between pairs of @@ will be substituted by the value in a map.<br />
 * Any values not in the map will be replaced by an empty string.
 * <br /> <br />
 * Behavior is undefined if keys or values contain the @@ marker.
 * @author Steven Spungin
 *
 */
public class SimpleTemplate {

	public static String process(String source, Map<String, String> substitutionMap) {
		for (String key : substitutionMap.keySet()) {
			source = source.replaceAll("@@" + key + "@@", substitutionMap.get(key));
		}
		source = source.replaceAll("@@.*?@@", "");
		return source;
	}
}
