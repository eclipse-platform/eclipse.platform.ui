/*******************************************************************************
 *  Copyright (c) 2019-2020 Eclipse Foundation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     Eclipse Foundation - initial API and implementation
 *     Alexander Fedorov <alexander.fedorov@arsysop.ru> - ongoing support
 *******************************************************************************/
package org.eclipse.ui.internal.views.markers;

import org.eclipse.core.resources.IMarker;
import org.eclipse.ui.views.markers.MarkerItem;

/**
 * Defaults contract for {@linkplain MarkerItem}.
 *
 * @since 3.16
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface MarkerItemDefaults {

	/**
	 * {@linkplain MarkerItem} {@code location} default value constant.
	 *
	 * @see MarkerItem#getAttributeValue(String, String)
	 * @see IMarker#LOCATION
	 * @see MarkerItem#getLocation()
	 * @since 3.16
	 */
	String LOCATION_DEFAULT = ""; //$NON-NLS-1$

	/**
	 * The PATH_ATTRIBUTE is the tag for the attribute on a marker that can be used
	 * to supply the String for the path rather than using the path of the
	 * underlying resource.
	 *
	 * @see IMarker#getAttribute(java.lang.String)
	 * @since 3.16
	 */
	String PATH_ATTRIBUTE = "org.eclipse.ui.views.markers.path";//$NON-NLS-1$

	/**
	 * {@linkplain MarkerItem} {@code path} default value constant.
	 *
	 * @see MarkerItem#getPath()
	 * @since 3.16
	 */
	String PATH_DEFAULT = ""; //$NON-NLS-1$

}
