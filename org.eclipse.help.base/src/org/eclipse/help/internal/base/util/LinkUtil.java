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
 
package org.eclipse.help.internal.base.util;
 

public class LinkUtil {

	/*
	 * Strings any parameters off the given href. If there is an anchor reference
	 * after the parameters it will be maintained. If null is passed in, null is returned.
	 */
	public static String stripParams(String href) {
		if (href == null)
			return null;
		int index = href.indexOf('?');
		if (index != -1) {
			String param = href.substring(index);
			href = href.substring(0, index);
			if ((index = param.indexOf('#')) != -1)
				href = href + param.substring(index);
		}
		return href;
	}
}
