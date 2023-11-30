/*******************************************************************************
 * Copyright (c) 2020 ArSysOp and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alexander Fedorov <alexander.fedorov@arsysop.ru> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.internal.workspace.markers;

import org.eclipse.core.resources.IMarker;

/**
 * The name attribute is the tag for the attribute on a marker that can be used
 * to supply the String for the name rather than using the name of the
 * underlying resource.
 *
 * @see IMarker#getAttribute(java.lang.String)
 */
public final class NameAttribute {

	private static final String key = "org.eclipse.ui.views.markers.name"; //$NON-NLS-1$

	/**
	 * Returns the key used to retrieve the name attribute
	 *
	 * @return the key
	 */
	public String key() {
		return key;
	}

}
