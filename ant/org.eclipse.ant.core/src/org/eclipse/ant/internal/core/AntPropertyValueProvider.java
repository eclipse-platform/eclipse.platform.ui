/*******************************************************************************
 * Copyright (c) 2003 Thierry Lach and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Thierry Lach (thierry.lach@bbdodetroit.com) - initial API and implementation for bug 40502
 *     IBM Corporation - added eclipse.running property
 *******************************************************************************/
package org.eclipse.ant.internal.core;

import java.net.URL;

import org.eclipse.ant.core.AntCorePlugin;
import org.eclipse.ant.core.IAntPropertyValueProvider;
import org.eclipse.core.runtime.Platform;

/**
 * Dynamic provider for Ant properties.
 * 
 * Provides the dynamic values for the following Ant properties:
 * 
 * <ul>
 * <li><code>eclipse.home</code> - set to the Eclipse installation directory</li>
 * </ul>
 * * <ul>
 * <li><code>eclipse.running</code> - set (to "true") when Eclipse is running</li>
 * </ul>
 * 
 * @since 3.0
 */
public class AntPropertyValueProvider implements IAntPropertyValueProvider {
	/**
	 * Returns the dynamic property values for Ant properties.
	 * 
	 * @see org.eclipse.ant.core.IAntPropertyProvider#getPropertyValue(java.lang.String)
	 */
	public String getAntPropertyValue(String propertyName) {
		String value = null;
		if ("eclipse.running".equals(propertyName)){ //$NON-NLS-1$
			return "true"; //$NON-NLS-1$
		} else if ("eclipse.home".equals(propertyName)) { //$NON-NLS-1$
			try {
				value = Platform.resolve(new URL("platform:/base/")).getPath(); //$NON-NLS-1$
				if (value.endsWith("/")) { //$NON-NLS-1$
				    value = value.substring(0, value.length() - 1);
				}
			} catch (Exception e) {
				AntCorePlugin.log(e);
			}
		}
		return value;
	}
}
